package de.eldoria.bloodnight.core.manager;

import de.eldoria.bloodnight.config.Configuration;
import de.eldoria.bloodnight.config.worldsettings.BossBarSettings;
import de.eldoria.bloodnight.config.worldsettings.NightSelection;
import de.eldoria.bloodnight.config.worldsettings.NightSettings;
import de.eldoria.bloodnight.config.worldsettings.WorldSettings;
import de.eldoria.bloodnight.config.worldsettings.deathactions.PlayerDeathActions;
import de.eldoria.bloodnight.core.BloodNight;
import de.eldoria.bloodnight.core.manager.nightmanager.BloodNightData;
import de.eldoria.bloodnight.core.manager.nightmanager.NightUtil;
import de.eldoria.bloodnight.events.BloodNightBeginEvent;
import de.eldoria.bloodnight.events.BloodNightEndEvent;
import de.eldoria.bloodnight.specialmobs.SpecialMobUtil;
import de.eldoria.bloodnight.util.C;
import de.eldoria.eldoutilities.localization.ILocalizer;
import de.eldoria.eldoutilities.messages.MessageSender;
import de.eldoria.eldoutilities.utils.ObjUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Statistic;
import org.bukkit.World;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.boss.KeyedBossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.world.TimeSkipEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
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

    private final Queue<World> startNight = new ArrayDeque<>();
    private final Queue<World> endNight = new ArrayDeque<>();

    private final Set<World> forceNights = new HashSet<>();

    private final Map<String, Double> customTimes = new HashMap<>();

    private final PluginManager pluginManager = Bukkit.getPluginManager();
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
    public void onWorldLoad(WorldLoadEvent event) {
        calcualteWorldState(event.getWorld());
    }

    // <--- Time consistency ---> //

    /**
     * Recalulate time state for immediate impact
     *
     * @param event time skip event
     */
    @EventHandler
    public void onTimeSkip(TimeSkipEvent event) {
        calcualteWorldState(event.getWorld());
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
            for (World observedWorld : Bukkit.getWorlds()) {
                calcualteWorldState(observedWorld);
            }
            worldRefresh = 0;
        }

        refreshTime();

        playRandomSound();

        changeNightStates();
    }

    private void refreshTime() {
        for (Map.Entry<World, BloodNightData> entry : getBloodWorldsMap().entrySet()) {
            World world = entry.getKey();
            WorldSettings settings = configuration.getWorldSettings(world.getName());
            NightSettings ns = settings.getNightSettings();
            if (ns.isCustomNightDuration()) {
                double calcTicks = NightUtil.getNightTicksPerTick(world, settings);

                double time = customTimes.compute(world.getName(),
                        (key, old) -> (old == null ? world.getFullTime() : old) + calcTicks);

                world.setFullTime(Math.round(time));
            }

            BloodNightData bloodNightData = getBloodNightData(world);
            ObjUtil.nonNull(bloodNightData.getBossBar(), bossBar -> {
                bossBar.setProgress(NightUtil.getNightProgress(world, configuration.getWorldSettings(world)));
            });
        }
    }

    private void playRandomSound() {
        for (BloodNightData data : getBloodWorldsMap().values()) {
            data.playRandomSound(configuration.getWorldSettings(data.getWorld()).getSoundSettings());
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
                BloodNight.logger().config("Removed 1 boss bar" + next.getKey());
            }
        }
        BloodNight.logger().info("Removed " + i + " hanging boss bars.");
    }

    private void calcualteWorldState(World world) {
        boolean current = NightUtil.isNight(world, configuration.getWorldSettings(world));
        boolean old = timeState.getOrDefault(world.getName(), false);

        if (current == old) {
            return;
        }

        timeState.put(world.getName(), current);

        if (current) {
            // A new night has begun.
            startNight.add(world);
            return;
        }

        if (isBloodNightActive(world)) {
            // A blood night has ended.
            endNight.add(world);
        }
    }

    private void changeNightStates() {
        while (!startNight.isEmpty()) {
            initializeBloodNight(startNight.poll());
        }

        for (Iterator<World> it = forceNights.iterator(); it.hasNext(); ) {
            World world = it.next();
            if (!NightUtil.isNight(world, configuration.getWorldSettings(world))) continue;
            initializeBloodNight(world, true);
            it.remove();
        }

        while (!endNight.isEmpty()) {
            resolveBloodNight(endNight.poll());
        }
    }

    // <--- BloodNight activation and deactivation --->

    private boolean initializeBloodNight(World world) {
        return initializeBloodNight(world, false);
    }

    private boolean initializeBloodNight(World world, boolean force) {
        WorldSettings settings = configuration.getWorldSettings(world.getName());

        if (!settings.isEnabled()) {
            BloodNight.logger().fine("Blood night in world " + world.getName() + " is not enabled. Will not initialize.");
            return true;
        }

        // skip the calculation if a night should be forced.
        if (!force) {
            NightSelection sel = settings.getNightSelection();
            int val = ThreadLocalRandom.current().nextInt(101);

            sel.upcount();

            int probability = sel.getCurrentProbability(world);
            if (probability <= 0) return true;
            if (val > probability) return true;
        }
        BloodNightBeginEvent beginEvent = new BloodNightBeginEvent(world);
        // A new blood night has begun.
        pluginManager.callEvent(beginEvent);

        if (beginEvent.isCancelled()) {
            BloodNight.logger().fine("BloodNight in " + world.getName() + " was canceled by another plugin.");
            return true;
        }
        BloodNight.logger().fine("BloodNight in " + world.getName() + " activated.");


        BossBar bossBar = null;
        BossBarSettings bbS = settings.getBossBarSettings();
        if (bbS.isEnabled()) {
            bossBar = Bukkit.createBossBar(C.getBossBarNamespace(world), bbS.getTitle(), bbS.getColor(), BarStyle.SOLID, bbS.getEffects());
        }

        bloodWorlds.put(world, new BloodNightData(world, bossBar));

        dispatchCommands(settings.getNightSettings().getStartCommands(), world);

        settings.getNightSettings().regenerateNightDuration();

        if (settings.getNightSettings().isCustomNightDuration()) {
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        }

        for (Player player : world.getPlayers()) {
            enableBloodNightForPlayer(player, world);
        }
        return true;
    }

    private void resolveBloodNight(World world) {
        BloodNight.logger().fine("BloodNight in " + world.getName() + " resolved.");

        WorldSettings settings = configuration.getWorldSettings(world.getName());

        if (settings.getNightSettings().isCustomNightDuration()) {
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
            customTimes.remove(world.getName());
        }

        dispatchCommands(settings.getNightSettings().getEndCommands(), world);

        pluginManager.callEvent(new BloodNightEndEvent(world));
        for (Player player : world.getPlayers()) {
            disableBloodNightForPlayer(player, world);
        }

        removeBloodWorld(world).resolveAll();
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
        BloodNightData bloodNightData = getBloodNightData(world);
        WorldSettings worldSettings = configuration.getWorldSettings(player.getWorld().getName());

        BloodNight.logger().finer("Enabling blood night for player " + player.getName());
        if (worldSettings.getMobSettings().isForcePhantoms()) {
            player.setStatistic(Statistic.TIME_SINCE_REST, 720000);
        }
        if (configuration.getGeneralSettings().isBlindness()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 5 * 20, 1, false, true));
        }

        worldSettings.getSoundSettings().playStartSound(player);

        bloodNightData.addPlayer(player);
    }

    private void disableBloodNightForPlayer(Player player, World world) {
        WorldSettings worldSettings = configuration.getWorldSettings(player.getWorld().getName());

        BloodNight.logger().finer("Resolving blood night for player " + player.getName());

        getBloodNightData(world).removePlayer(player);

        if (configuration.getGeneralSettings().isBlindness()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 5 * 20, 1, false, true));
        }

        worldSettings.getSoundSettings().playStartSound(player);
    }

    // <--- Player state consistency ---> //

    @EventHandler
    public void onPlayerWorldChange(PlayerChangedWorldEvent event) {
        if (isBloodNightActive(event.getFrom())) {
            disableBloodNightForPlayer(event.getPlayer(), event.getFrom());
        } else {
            ObjUtil.nonNull(Bukkit.getBossBar(C.getBossBarNamespace(event.getFrom())),
                    b -> {
                        b.removePlayer(event.getPlayer());
                    });
        }

        if (isBloodNightActive(event.getPlayer().getWorld())) {
            enableBloodNightForPlayer(event.getPlayer(), event.getPlayer().getWorld());
        } else {
            ObjUtil.nonNull(Bukkit.getBossBar(C.getBossBarNamespace(event.getPlayer().getWorld())),
                    b -> {
                        b.removePlayer(event.getPlayer());
                    });
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
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

    @EventHandler(priority = EventPriority.LOW)
    public void onBedEnter(PlayerBedEnterEvent event) {
        if (!isBloodNightActive(event.getPlayer().getWorld())) return;
        NightSettings nightSettings = configuration.getWorldSettings(event.getPlayer().getWorld()).getNightSettings();
        if (nightSettings.isSkippable()) return;
        messageSender.sendMessage(event.getPlayer(), localizer.getMessage("notify.youCantSleep"));
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        PlayerDeathActions actions = configuration.getWorldSettings(event.getEntity().getWorld())
                .getDeathActionSettings()
                .getPlayerDeathActions();
        SpecialMobUtil.dispatchShockwave(actions.getShockwaveSettings(), event.getEntity().getLocation());
        SpecialMobUtil.dispatchLightning(actions.getLightningSettings(), event.getEntity().getLocation());

        if (actions.getLoseInvProbability() < ThreadLocalRandom.current().nextInt(101)) {
            event.getDrops().clear();
        }
        if (actions.getLoseExpProbability() < ThreadLocalRandom.current().nextInt(101)) {
            event.getDrops().clear();
        }
        for (String deathCommand : actions.getDeathCommands()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), deathCommand.replace("{player}", event.getEntity().getName()));
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        PlayerDeathActions actions = configuration.getWorldSettings(event.getPlayer().getWorld())
                .getDeathActionSettings()
                .getPlayerDeathActions();
        for (PotionEffectType respawnEffect : actions.getRespawnEffects()) {
            event.getPlayer().addPotionEffect(new PotionEffect(respawnEffect, actions.getEffectDuration(), 1));
        }
    }


    // <--- Utility functions ---> //


    /**
     * Check if a world is currenty in blood night mode.
     *
     * @param world world to check
     * @return true if world is currently in blood night mode.
     */
    public boolean isBloodNightActive(World world) {
        return bloodWorlds.containsKey(world);
    }

    public void forceNight(World world) {
        forceNights.add(world);
    }

    public void cancelNight(World world) {
        endNight.add(world);
    }

    public void shutdown() {
        BloodNight.logger().info("Shutting down night manager.");

        BloodNight.logger().info("Resolving blood nights.");

        // Copy to new collection since the blood worlds are removed on resolve.
        for (World world : new HashSet<>(bloodWorlds.keySet())) {
            resolveBloodNight(world);
        }

        BloodNight.logger().info("Night manager shutdown successful.");
    }

    public void reload() {
        for (World observedWorld : new HashSet<>(bloodWorlds.keySet())) {
            resolveBloodNight(observedWorld);
        }

        timeState.clear();
        bloodWorlds.clear();
    }

    private void addBloodNight(World world, BloodNightData bloodNightData) {
        bloodWorlds.put(world, bloodNightData);
    }

    private BloodNightData removeBloodWorld(World world) {
        return bloodWorlds.remove(world);
    }

    private BloodNightData getBloodNightData(World world) {
        return getBloodWorldsMap().get(world);
    }

    /**
     * Get a unmodifiable map of blood words. This map should be used for iteration purposes only.
     *
     * @return unmodifiable map of blood words
     */
    private Map<World, BloodNightData> getBloodWorldsMap() {
        return Collections.unmodifiableMap(bloodWorlds);
    }

    /**
     * Gets an unmodifiable Set of blood worlds.
     *
     * @return unmodifiable Set of blood worlds.
     */
    public Set<World> getBloodWorldsSet() {
        return Collections.unmodifiableSet(bloodWorlds.keySet());
    }
}
