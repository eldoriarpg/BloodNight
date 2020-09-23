package de.eldoria.bloodnight.listener;

import de.eldoria.bloodnight.core.BloodNight;
import de.eldoria.bloodnight.config.Configuration;
import de.eldoria.bloodnight.config.NightSettings;
import de.eldoria.bloodnight.config.WorldSettings;
import de.eldoria.bloodnight.events.BloodNightBeginEvent;
import de.eldoria.bloodnight.events.BloodNightEndEvent;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

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
    private final Map<World, BloodNightData> bloodWorlds = new HashMap<>();
    /**
     * A set of all worlds which are observed by the plugin.
     */
    private final Set<World> observedWorlds = new HashSet<>();

    private final Map<UUID, ConsistencyCache> playerConsistencyMap = new HashMap<>();

    private final PluginManager pluginManager = Bukkit.getPluginManager();
    private final ThreadLocalRandom rand = ThreadLocalRandom.current();

    public NightListener(Configuration configuration) {
        this.configuration = configuration;
        reload();
    }

    // <--- World consistency ---> //

    @EventHandler
    public void onWorldUnload(WorldUnloadEvent event) {
        observedWorlds.remove(event.getWorld());
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        if (configuration.getWorldSettings(event.getWorld().getName()) != null) {
            observedWorlds.add(event.getWorld());
            calcualteWorldState(event.getWorld());
        }
    }

    // <--- Time consistency ---> //

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

    // <--- Refresh routine ---> //

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

        if (current == old) {
            if (bloodWorlds.containsKey(world)) {
                NightSettings settings = configuration.getWorldSettings(world.getName()).getNightSettings();
                long total = getDiff(settings.getNightBegin(), settings.getNightEnd());
                long left = getDiff(world.getFullTime(), settings.getNightEnd());
                BloodNightData bloodNightData = bloodWorlds.get(world);
                bloodNightData.getBossBar().setProgress(left / (double) total);
            }
            return;
        }

        timeState.put(world.getName(), current);

        if (current) {
            // A new night has begun.
            initializeBloodNight(world);
            return;
        }

        if (bloodWorlds.containsKey(world)) {
            // A blood night has ended.
            resolveBloodNight(world);
        }
    }

    // <--- BloodNight activation and deactivation --->

    private void initializeBloodNight(World world) {
        int val = rand.nextInt(101);
        WorldSettings settings = configuration.getWorldSettings(world.getName());
        switch (settings.getNightSelection().getNightSelectionType()) {
            case RANDOM:
                if (settings.getNightSelection().getProbability() > val) return;
                break;
            case MOON_PHASE:
                int moonPhase = getMoonPhase(world);
                if (!settings.getNightSelection().getPhases().containsKey(moonPhase)) return;
                if (settings.getNightSelection().getPhases().get(moonPhase) > val) return;
                break;
        }

        BloodNight.logger().info("BloodNight in " + world.getName() + " activated.");

        // A new blood night has begun.
        pluginManager.callEvent(new BloodNightBeginEvent(world));
        BossBar bossBar = Bukkit.createBossBar("§c§lBlood Night", BarColor.RED, BarStyle.SOLID, BarFlag.DARKEN_SKY);
        BloodNightData bloodNightData = new BloodNightData(bossBar);
        bloodWorlds.put(world, bloodNightData);

        for (Player player : world.getPlayers()) {
            enableBloodNightForPlayer(player, bloodNightData);
        }

    }

    private void resolveBloodNight(World world) {

        BloodNight.logger().info("BloodNight in " + world.getName() + " resolved.");

        pluginManager.callEvent(new BloodNightEndEvent(world));
        BloodNightData bloodNightData = bloodWorlds.remove(world);
        for (Player player : world.getPlayers()) {
            disableBloodNightForPlayer(player, bloodNightData);
        }
    }

    private void enableBloodNightForPlayer(Player player, BloodNightData bloodNightData) {
        playerConsistencyMap.put(player.getUniqueId(), new ConsistencyCache(player));
        WorldSettings worldSettings = configuration.getWorldSettings(player.getWorld().getName());
        if (worldSettings.getNightSettings().isForcePhantoms()) {
            player.setStatistic(Statistic.TIME_SINCE_REST, 72000);
        }
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 5 * 20, 1, false, true));
        bloodNightData.getBossBar().addPlayer(player);
    }

    private void disableBloodNightForPlayer(Player player, BloodNightData bloodNightData) {
        ConsistencyCache consistencyCache = playerConsistencyMap.get(player.getUniqueId());
        if (consistencyCache != null) {
            consistencyCache.revert(player);
        }
        bloodNightData.getBossBar().removePlayer(player);
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 5 *20, 1, false, true));
    }

    // <--- Player state consistency ---> //

    @EventHandler
    public void onPlayerWorldChange(PlayerChangedWorldEvent event) {
        if (isBloodNightActive(event.getFrom())) {
            if (isBloodNightActive(event.getPlayer().getWorld())) {
                // no action needs to be taken
                return;
            }
            disableBloodNightForPlayer(event.getPlayer(), bloodWorlds.get(event.getFrom()));
        } else {
            if (isBloodNightActive(event.getPlayer().getWorld())) {
                enableBloodNightForPlayer(event.getPlayer(), bloodWorlds.get(event.getPlayer().getWorld()));
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
            enableBloodNightForPlayer(event.getPlayer(), bloodWorlds.get(event.getPlayer().getWorld()));
        }
    }

    // <--- Utility functions ---> //

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
        return bloodWorlds.containsKey(world);
    }

    private boolean isNight(World world) {
        WorldSettings worldSettings = configuration.getWorldSettings(world.getName());
        long openInTicks = getDiff(world.getFullTime(), worldSettings.getNightSettings().getNightBegin());
        long closedInTicks = getDiff(world.getFullTime(), worldSettings.getNightSettings().getNightEnd());
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
        for (World observedWorld : bloodWorlds.keySet()) {
            resolveBloodNight(observedWorld);
        }
        observedWorlds.clear();
        configuration.getWorldSettings().keySet().stream()
                .map(Bukkit::getWorld)
                .filter(Objects::nonNull)
                .forEach(observedWorlds::add);
        timeState.clear();
        bloodWorlds.clear();
    }

    public Set<World> getBloodWorlds() {
        return Collections.unmodifiableSet(bloodWorlds.keySet());
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

    @Getter
    private static class BloodNightData {
        private final BossBar bossBar;

        public BloodNightData(BossBar bossBar) {

            this.bossBar = bossBar;
        }
    }
}
