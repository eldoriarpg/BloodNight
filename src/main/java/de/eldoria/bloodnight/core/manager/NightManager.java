package de.eldoria.bloodnight.core.manager;

import de.eldoria.bloodnight.config.Configuration;
import de.eldoria.bloodnight.config.worldsettings.BossBarSettings;
import de.eldoria.bloodnight.config.worldsettings.NightSelection;
import de.eldoria.bloodnight.config.worldsettings.NightSettings;
import de.eldoria.bloodnight.config.worldsettings.WorldSettings;
import de.eldoria.bloodnight.core.BloodNight;
import de.eldoria.bloodnight.core.events.BloodNightBeginEvent;
import de.eldoria.bloodnight.core.events.BloodNightEndEvent;
import de.eldoria.eldoutilities.localization.Localizer;
import de.eldoria.eldoutilities.messages.MessageSender;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Statistic;
import org.bukkit.World;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
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

public class NightManager implements Listener, Runnable {

    private final Configuration configuration;
    /**
     * Map contains for every active world a boolean if it is currently night.
     */
    private final Map<String, Boolean> timeState = new HashMap<>();
    /**
     * A set containing all world where a blood night is active.
     */
    private final Map<World, BloodNightData> bloodWorlds = new HashMap<>();
    /**
     * A set of all worlds which are observed by the plugin.
     */
    private final Set<World> observedWorlds = new HashSet<>();

    private final Set<World> forceNight = new HashSet<>();

    private final Map<UUID, ConsistencyCache> playerConsistencyMap = new HashMap<>();

    private final Map<String, Double> customTimes = new HashMap<>();

    private final PluginManager pluginManager = Bukkit.getPluginManager();
    private final ThreadLocalRandom rand = ThreadLocalRandom.current();
    private final Localizer localizer;
    private final MessageSender messageSender;

    private int worldRefresh = 0;

    public NightManager(Configuration configuration) {
        this.configuration = configuration;
        this.localizer = BloodNight.localizer();
        this.messageSender = MessageSender.get(BloodNight.getInstance());
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
        worldRefresh++;
        if (worldRefresh == 5) {
            for (World observedWorld : observedWorlds) {
                calcualteWorldState(observedWorld);
            }
            worldRefresh = 0;
        }

        for (Map.Entry<World, BloodNightData> entry : bloodWorlds.entrySet()) {
            WorldSettings settings = configuration.getWorldSettings(entry.getKey().getName());
            NightSettings ns = settings.getNightSettings();
            if (ns.isOverrideNightDuration()) {
                int nightDurationTicks = ns.getNightDuration() * 20;
                long normalNightDuration = getDiff(ns.getNightBegin(), ns.getNightEnd());
                double calcTicks = (double) normalNightDuration / nightDurationTicks;

                long fullTime = entry.getKey().getFullTime();
                double time = customTimes.compute(entry.getKey().getName(), (key, old) -> (old == null ? fullTime : old) + calcTicks);

                // This scheduler is called every 5 ticks.
                entry.getKey().setFullTime(Math.round(time));
            }
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

        if (!settings.isEnabled()) {
            if (BloodNight.isDebug()) {
                BloodNight.logger().info("Blood night in world " + world.getName() + " is not enabled. Will not initialize.");
            }
            return;
        }

        // skip the calculation if a night should be forced.
        if (!forceNight.remove(world)) {
            NightSelection sel = settings.getNightSelection();
            switch (sel.getNightSelectionType()) {
                case RANDOM:
                    if (sel.getProbability() < val) return;
                    break;
                case MOON_PHASE:
                    int moonPhase = getMoonPhase(world);
                    if (!sel.getPhases().containsKey(moonPhase)) return;
                    if (sel.getPhases().get(moonPhase) > val) return;
                    break;
                case INTERVAL:
                    sel.setCurInterval(sel.getCurInterval() + 1);
                    if (sel.getCurInterval() != sel.getInterval()) {
                        return;
                    }
                    sel.setCurInterval(0);
                    break;
            }
        }

        if (!settings.isEnabled()) {
            if (BloodNight.isDebug()) {
                BloodNight.logger().info("Blood night in world " + world.getName() + " is not enabled. Will not initialize.");
            }
            return;
        }

        if (BloodNight.isDebug()) {
            BloodNight.logger().info("BloodNight in " + world.getName() + " activated.");
        }

        // A new blood night has begun.
        pluginManager.callEvent(new BloodNightBeginEvent(world));
        BossBar bossBar = null;
        BossBarSettings bbS = settings.getBossBarSettings();
        if (bbS.isEnabled()) {
            bossBar = Bukkit.createBossBar(BloodNight.getNamespacedKey("bossBar" + settings.getWorldName()), bbS.getTitle(), bbS.getColor(), BarStyle.SOLID, bbS.getEffects());
        }
        BloodNightData bloodNightData = new BloodNightData(bossBar);
        bloodWorlds.put(world, bloodNightData);

        for (String cmd : settings.getNightSettings().getStartCommands()) {
            cmd = cmd.replace("{world}", world.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
        }

        if (settings.getNightSettings().isOverrideNightDuration()) {
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        }

        for (Player player : world.getPlayers()) {
            enableBloodNightForPlayer(player, bloodNightData);
        }
    }

    private void resolveBloodNight(World world) {

        if (BloodNight.isDebug()) {
            BloodNight.logger().info("BloodNight in " + world.getName() + " resolved.");
        }

        WorldSettings settings = configuration.getWorldSettings(world.getName());

        if (settings.getNightSettings().isOverrideNightDuration()) {
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
            customTimes.remove(world.getName());
        }

        for (String cmd : settings.getNightSettings().getEndCommands()) {
            cmd = cmd.replace("{world}", world.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
        }

        pluginManager.callEvent(new BloodNightEndEvent(world));
        BloodNightData bloodNightData = bloodWorlds.remove(world);
        for (Player player : world.getPlayers()) {
            disableBloodNightForPlayer(player, bloodNightData);
        }

        if (bloodNightData.getBossBar() != null) {
            Bukkit.removeBossBar(BloodNight.getNamespacedKey("bossBar" + settings.getWorldName()));
        }
    }

    private void enableBloodNightForPlayer(Player player, BloodNightData bloodNightData) {
        playerConsistencyMap.put(player.getUniqueId(), new ConsistencyCache(player));
        WorldSettings worldSettings = configuration.getWorldSettings(player.getWorld().getName());
        if (worldSettings.getMobSettings().isForcePhantoms()) {
            player.setStatistic(Statistic.TIME_SINCE_REST, 72000);
        }
        if (configuration.getGeneralSettings().isBlindness()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 5 * 20, 1, false, true));
        }
        if (bloodNightData.getBossBar() != null) {
            bloodNightData.getBossBar().addPlayer(player);
        }
    }

    private void disableBloodNightForPlayer(Player player, BloodNightData bloodNightData) {
        ConsistencyCache consistencyCache = playerConsistencyMap.get(player.getUniqueId());
        if (consistencyCache != null) {
            consistencyCache.revert(player);
        }
        if (bloodNightData.getBossBar() != null) {
            bloodNightData.getBossBar().removePlayer(player);
        }
        if (configuration.getGeneralSettings().isBlindness()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 5 * 20, 1, false, true));
        }
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

    // <--- Night Listener ---> //

    @EventHandler
    public void onBedEnter(PlayerBedEnterEvent event) {
        if (!isBloodNightActive(event.getPlayer().getWorld())) return;
        NightSettings nightSettings = configuration.getWorldSettings(event.getPlayer().getWorld()).getNightSettings();
        if (nightSettings.isSkippable()) return;
        messageSender.sendMessage(event.getPlayer(), localizer.getMessage("notify.youCantSleep"));
        event.setCancelled(true);
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


    public boolean isWorldRegistered(World world) {
        return observedWorlds.contains(world);
    }

    public void forceNight(World world) {
        forceNight.add(world);
        if (isNight(world)) {
            initializeBloodNight(world);
        }
    }

    public void cancelNight(World world) {
        resolveBloodNight(world);
    }

    public void shutdown() {
        BloodNight.logger().info("Shutting down night manager.");

        BloodNight.logger().info("Apply consistency cache.");
        for (Map.Entry<UUID, ConsistencyCache> entry : playerConsistencyMap.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null) continue;
            entry.getValue().revert(player);
        }

        BloodNight.logger().info("Resolving blood nights.");
        for (World world : bloodWorlds.keySet()) {
            resolveBloodNight(world);
        }

        BloodNight.logger().info("Night manager shutdown successful.");
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

    public Set<World> getObservedWorlds() {
        return observedWorlds;
    }

    @Getter
    private static class ConsistencyCache {
        private final int timeSinceRest;

        public ConsistencyCache(Player player) {
            timeSinceRest = player.getStatistic(Statistic.TIME_SINCE_REST);
        }

        public void revert(Player player) {
            player.setStatistic(Statistic.TIME_SINCE_REST, timeSinceRest);
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
