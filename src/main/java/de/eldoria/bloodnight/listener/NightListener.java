package de.eldoria.bloodnight.listener;

import de.eldoria.bloodnight.config.Configuration;
import de.eldoria.bloodnight.events.BloodNightBeginEvent;
import de.eldoria.bloodnight.events.BloodNightEndEvent;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.TimeSkipEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.plugin.PluginManager;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class NightListener implements Listener, Runnable {

    private final Configuration configuration;
    /**
     * Map contains for every active world a boolean if it is currently night.
     */
    private final Map<String, Boolean> timeState = new HashMap<>();
    /**
     * A set containing all world where a blood night is acctive.
     */
    private final Set<World> bloodWorlds = new HashSet<>();
    /**
     * A set of all worlds which are observed by the plugin.
     */
    private final Set<World> observedWorlds = new HashSet<>();

    private final Map<UUID, ConsistencyCache> playerConsistencyMap = new HashMap<>();

    private final PluginManager pluginManager = Bukkit.getPluginManager();
    private final ThreadLocalRandom rand = ThreadLocalRandom.current();

    public NightListener(Configuration configuration) {
        this.configuration = configuration;
        configuration.getNightSettings().getWorlds().stream()
                .map(Bukkit::getWorld)
                .filter(Objects::nonNull)
                .forEach(observedWorlds::add);
    }

    @EventHandler
    public void onWorldUnload(WorldUnloadEvent event) {
        observedWorlds.remove(event.getWorld());
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        if (configuration.getNightSettings().getWorlds().contains(event.getWorld().getName())) {
            observedWorlds.add(event.getWorld());
            calcualteWorldState(event.getWorld());
        }
    }

    /**
     * Recalulate time state for immediate impact
     *
     * @param event time skip event
     */
    @EventHandler
    public void onTimeSkip(TimeSkipEvent event) {
        if (observedWorlds.contains(event.getWorld())) {
            calcualteWorldState(event.getWorld());
        }
    }

    /**
     * Check if a day becomes a night.
     */
    @Override
    public void run() {
        for (World observedWorld : observedWorlds) {
            calcualteWorldState(observedWorld);
        }
    }

    private void calcualteWorldState(World world) {
        boolean current = isNight(world);
        boolean old = timeState.getOrDefault(world.getName(), false);

        if (current == old) return;

        timeState.put(world.getName(), current);

        if (current) {
            // A new night has begun.
            initializeBloodNight(world);
            return;
        }

        if (bloodWorlds.contains(world)) {
            // A blood night has ended.
            resolveBloodNight(world);
        }
    }

    private void resolveBloodNight(World world) {
        pluginManager.callEvent(new BloodNightEndEvent(world));
        bloodWorlds.remove(world);
        for (Player player : world.getPlayers()) {
            disableBloodNightForPlayer(player);
        }
    }

    private void initializeBloodNight(World world) {
        int val = rand.nextInt(101);
        switch (configuration.getNightSelection().getNightSelectionType()) {
            case RANDOM:
                if (configuration.getNightSelection().getProbability() > val) return;
                break;
            case MOON_PHASE:
                int moonPhase = getMoonPhase(world);
                if (!configuration.getNightSelection().getPhases().containsKey(moonPhase)) return;
                if (configuration.getNightSelection().getPhases().get(moonPhase) > val) return;
                break;
        }
        // A new blood night has begun.
        pluginManager.callEvent(new BloodNightBeginEvent(world));
        bloodWorlds.add(world);

        for (Player player : world.getPlayers()) {
            enableBloodNightForPlayer(player);
        }

    }

    @EventHandler
    public void onPlayerWorldChange(PlayerChangedWorldEvent event) {
        if (isBloodNightActive(event.getFrom())) {
            if (isBloodNightActive(event.getPlayer().getWorld())) {
                // no action needs to be taken
                return;
            }
            disableBloodNightForPlayer(event.getPlayer());
        } else {
            if (isBloodNightActive(event.getPlayer().getWorld())) {
                enableBloodNightForPlayer(event.getPlayer());
            }
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        if (playerConsistencyMap.containsKey(event.getPlayer().getUniqueId())) {
            playerConsistencyMap.get(event.getPlayer().getUniqueId()).revert(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (isBloodNightActive(event.getPlayer().getWorld())) {
            enableBloodNightForPlayer(event.getPlayer());
        }
    }

    private void enableBloodNightForPlayer(Player player) {
        playerConsistencyMap.put(player.getUniqueId(), new ConsistencyCache(player));
        if (configuration.getNightSettings().isForcePhantoms()) {
            player.setStatistic(Statistic.TIME_SINCE_REST, 72000);
        }
    }

    private void disableBloodNightForPlayer(Player player) {
        ConsistencyCache consistencyCache = playerConsistencyMap.get(player.getUniqueId());
        if (consistencyCache != null) {
            consistencyCache.revert(player);
        }
    }

    private int getMoonPhase(World world) {
        int days = (int) Math.floor(world.getFullTime() / 24000d);
        return days % 8;
    }

    /**
     * Check if a world is currenty in blood night mode.
     *
     * @param world world to check
     * @return true if world is currently in blood night mode.
     */
    public boolean isBloodNightActive(World world) {
        return bloodWorlds.contains(world);
    }

    private boolean isNight(World world) {
        long openInTicks = getDiff(world.getFullTime(), configuration.getNightSettings().getNightBegin());
        long closedInTicks = getDiff(world.getFullTime(), configuration.getNightSettings().getNightEnd());
        // check if door should be open
        return openInTicks > closedInTicks;
    }

    private long getDiff(long fullTime, long nextTime) {
        long currentTime = fullTime % 24000;
        return currentTime > nextTime ? 24000 - currentTime + nextTime : nextTime - currentTime;
    }

    public void registerWorld(World world) {
        observedWorlds.add(world);
    }

    public boolean unregisterWorld(World world) {
        return observedWorlds.remove(world);
    }

    public void shutdown() {
        for (Map.Entry<UUID, ConsistencyCache> entry : playerConsistencyMap.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null) continue;
            entry.getValue().revert(player);
        }
    }

    public void reload() {
        for (World observedWorld : bloodWorlds) {
            resolveBloodNight(observedWorld);
        }
        observedWorlds.clear();
        configuration.getNightSettings().getWorlds().stream()
                .map(Bukkit::getWorld)
                .filter(Objects::nonNull)
                .forEach(observedWorlds::add);
        timeState.clear();
        bloodWorlds.clear();
    }

    public Set<World> getBloodWorlds() {
        return Collections.unmodifiableSet(bloodWorlds);
    }

    @Getter
    private static class ConsistencyCache {
        private final int timeSinceRest;

        public ConsistencyCache(Player player) {
            timeSinceRest = player.getStatistic(Statistic.TIME_SINCE_REST);
        }

        public void revert(Player player) {
            player.setStatistic(Statistic.TIME_SINCE_REST, timeSinceRest + player.getStatistic(Statistic.TIME_SINCE_REST));
        }
    }
}
