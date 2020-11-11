package de.eldoria.bloodnight.core.manager;

import de.eldoria.bloodnight.config.Configuration;
import de.eldoria.bloodnight.config.worldsettings.BossBarSettings;
import de.eldoria.bloodnight.config.worldsettings.NightSelection;
import de.eldoria.bloodnight.config.worldsettings.NightSettings;
import de.eldoria.bloodnight.config.worldsettings.WorldSettings;
import de.eldoria.bloodnight.core.BloodNight;
import de.eldoria.bloodnight.core.events.BloodNightBeginEvent;
import de.eldoria.bloodnight.core.events.BloodNightEndEvent;
import de.eldoria.bloodnight.util.MoonPhase;
import de.eldoria.eldoutilities.container.Pair;
import de.eldoria.eldoutilities.localization.ILocalizer;
import de.eldoria.eldoutilities.messages.MessageSender;
import de.eldoria.eldoutilities.utils.EMath;
import de.eldoria.eldoutilities.utils.ObjUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.NamespacedKey;
import org.bukkit.Statistic;
import org.bukkit.World;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.boss.KeyedBossBar;
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

import java.time.Instant;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
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
    private final ILocalizer localizer;
    private final MessageSender messageSender;

    private int worldRefresh = 0;

    private boolean initialized = false;

    public NightManager(Configuration configuration) {
        this.configuration = configuration;
        this.localizer = ILocalizer.getPluginLocalizer(BloodNight.class);
        this.messageSender = MessageSender.getPluginMessageSender(BloodNight.class);
        reload();
    }

    // <--- World consistency ---> //

    @EventHandler
    public void onWorldUnload(WorldUnloadEvent event) {
        observedWorlds.remove(event.getWorld());
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        if (configuration.getWorldSettings(event.getWorld()) != null) {
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
        if (!initialized) {
            cleanup();
            BloodNight.logger().info("Night manager initialized.");
            initialized = true;
        }

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

    private void cleanup() {
        BloodNight.logger().info("Executing cleanup task on startup.");

        Iterator<KeyedBossBar> bossBars = Bukkit.getBossBars();
        String s = BloodNight.getInstance().getName().toLowerCase(Locale.ROOT);
        int i = 0;
        while (bossBars.hasNext()) {
            KeyedBossBar next = bossBars.next();
            if (next.getKey().getNamespace().equalsIgnoreCase(s)) {
                Bukkit.removeBossBar(next.getKey());
                i++;
                if (BloodNight.isDebug()) {
                    BloodNight.logger().info("Removed 1 boss bar" + next.getKey());
                }
            }
        }
        BloodNight.logger().info("Removed " + i + " hanging boss bars.");
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
                ObjUtil.nonNull(bloodNightData.getBossBar(), bossBar -> {
                    bossBar.setProgress(Math.max(Math.min(left / (double) total, 1), 0));
                });
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
            int val = rand.nextInt(101);

            switch (sel.getNightSelectionType()) {
                case RANDOM:
                    if (sel.getProbability() <= 0) return;
                    if (val > sel.getProbability()) return;
                    break;
                case MOON_PHASE:
                    int moonPhase = getMoonPhase(world);
                    if (!sel.getMoonPhase().containsKey(moonPhase)) return;
                    if (sel.getPhaseProbability(moonPhase) <= 0) return;
                    if (val > sel.getPhaseProbability(moonPhase)) return;
                    break;
                case INTERVAL:
                    sel.upcountInterval();
                    if (sel.getCurInterval() != sel.getInterval()) {
                        return;
                    }
                    if (sel.getIntervalProbability() <= 0) return;
                    if (val > sel.getIntervalProbability()) return;
                    sel.setCurInterval(0);
                    break;
                case PHASE:
                    sel.upcountPhase();
                    int phaseProb = sel.getPhaseCustom().get(sel.getCurrPhase());
                    if (phaseProb <= 0) return;
                    if (val > phaseProb) return;
                    break;
                case CURVE:
                    double curveProb;
                    // First half. Increasing curve.
                    if (sel.getCurrCurvePos() <= sel.getPeriod() / 2) {
                        curveProb = EMath.smoothCurveValue(Pair.of(0d, (double) sel.getMinCurveVal()),
                                Pair.of((double) sel.getPeriod() / 2,
                                        (double) sel.getMaxCurveVal()), sel.getCurrCurvePos());
                    } else {
                        curveProb = EMath.smoothCurveValue(Pair.of((double) sel.getPeriod() / 2, (double) sel.getMaxCurveVal()),
                                Pair.of((double) sel.getPeriod(),
                                        (double) sel.getMinCurveVal()), sel.getCurrCurvePos());
                    }
                    if (curveProb <= 0) return;
                    if (val > curveProb) return;
                    break;
                case REAL_MOON_PHASE:
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(Date.from(Instant.now()));
                    // Get moon phase based on Server time. Convert to minecraft moon phase.
                    int realMoonPhase = (MoonPhase.computePhaseIndex(cal) + 4) % 8;
                    if (!sel.getMoonPhase().containsKey(realMoonPhase)) return;
                    if (sel.getPhaseProbability(realMoonPhase) <= 0) return;
                    if (val > sel.getPhaseProbability(realMoonPhase)) return;
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + sel.getNightSelectionType());
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
            bossBar = Bukkit.createBossBar(getBossBarNamespace(world), bbS.getTitle(), bbS.getColor(), BarStyle.SOLID, bbS.getEffects());
        }

        bloodWorlds.put(world, new BloodNightData(bossBar));

        dispatchCommands(settings.getNightSettings().getStartCommands(), world);

        if (settings.getNightSettings().isOverrideNightDuration()) {
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        }

        for (Player player : world.getPlayers()) {
            enableBloodNightForPlayer(player, world);
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

        dispatchCommands(settings.getNightSettings().getEndCommands(), world);

        pluginManager.callEvent(new BloodNightEndEvent(world));
        for (Player player : world.getPlayers()) {
            disableBloodNightForPlayer(player, world);
        }

        bloodWorlds.remove(world);

        ObjUtil.nonNull(Bukkit.getBossBar(getBossBarNamespace(world)), b -> {
            b.removeAll();
            if (!Bukkit.removeBossBar(getBossBarNamespace(world))) {
                if (BloodNight.isDebug()) {
                    BloodNight.logger().warning("Could not remove boss bar " + getBossBarNamespace(world));
                }
            }
        });
    }

    private void dispatchCommands(List<String> cmds, World world) {
        for (String cmd : cmds) {
            cmd = cmd.replace("{world}", world.getName());
            if (cmd.contains("{player}")) {
                for (Player player : world.getPlayers()) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("{player}", player.getName()));
                }
            } else {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
            }
        }
    }

    private void enableBloodNightForPlayer(Player player, World world) {
        playerConsistencyMap.put(player.getUniqueId(), new ConsistencyCache(player));
        BloodNightData bloodNightData = this.bloodWorlds.get(world);
        WorldSettings worldSettings = configuration.getWorldSettings(player.getWorld().getName());

        if (BloodNight.isDebug()) {
            BloodNight.logger().info("Enabling blood night for player " + player.getName());
        }
        if (worldSettings.getMobSettings().isForcePhantoms()) {
            player.setStatistic(Statistic.TIME_SINCE_REST, 720000);
        }
        if (configuration.getGeneralSettings().isBlindness()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 5 * 20, 1, false, true));
        }

        ObjUtil.nonNull(bloodNightData.getBossBar(), b -> {
            bloodNightData.getBossBar().addPlayer(player);
        });
    }

    private void disableBloodNightForPlayer(Player player, World world) {
        ConsistencyCache consistencyCache = playerConsistencyMap.get(player.getUniqueId());

        if (BloodNight.isDebug()) {
            BloodNight.logger().info("Resolving blood night for player " + player.getName());
        }

        if (consistencyCache != null) {
            consistencyCache.revert(player);
        }

        ObjUtil.nonNull(Bukkit.getBossBar(getBossBarNamespace(world)), b -> {
            b.removePlayer(player);
        });

        if (configuration.getGeneralSettings().isBlindness()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 5 * 20, 1, false, true));
        }
    }

    // <--- Player state consistency ---> //

    @EventHandler
    public void onPlayerWorldChange(PlayerChangedWorldEvent event) {
        if (isBloodNightActive(event.getFrom())) {
            disableBloodNightForPlayer(event.getPlayer(), event.getFrom());
        } else {
            ObjUtil.nonNull(Bukkit.getBossBar(getBossBarNamespace(event.getFrom())),
                    b -> {
                        b.removePlayer(event.getPlayer());
                    });
        }

        if (isBloodNightActive(event.getPlayer().getWorld())) {
            enableBloodNightForPlayer(event.getPlayer(), event.getPlayer().getWorld());
        } else {
            ObjUtil.nonNull(Bukkit.getBossBar(getBossBarNamespace(event.getPlayer().getWorld())),
                    b -> {
                        b.removePlayer(event.getPlayer());
                    });
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        if (playerConsistencyMap.containsKey(event.getPlayer().getUniqueId())) {
            playerConsistencyMap.get(event.getPlayer().getUniqueId()).revert(event.getPlayer());
        }
        if (isBloodNightActive(event.getPlayer().getWorld())) {
            disableBloodNightForPlayer(event.getPlayer(), event.getPlayer().getWorld());
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (isBloodNightActive(event.getPlayer().getWorld())) {
            enableBloodNightForPlayer(event.getPlayer(), event.getPlayer().getWorld());
        }
    }

    // <--- Night Listener ---> //

    @EventHandler(priority = EventPriority.HIGHEST)
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
     *
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

    public NamespacedKey getBossBarNamespace(World world) {
        return BloodNight.getNamespacedKey("bossBar" + world.getName());
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
