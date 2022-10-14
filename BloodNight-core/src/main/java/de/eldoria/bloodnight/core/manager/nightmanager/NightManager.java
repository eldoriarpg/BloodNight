package de.eldoria.bloodnight.core.manager.nightmanager;

import de.eldoria.bloodnight.config.Configuration;
import de.eldoria.bloodnight.config.worldsettings.BossBarSettings;
import de.eldoria.bloodnight.config.worldsettings.NightSelection;
import de.eldoria.bloodnight.config.worldsettings.NightSettings;
import de.eldoria.bloodnight.config.worldsettings.WorldSettings;
import de.eldoria.bloodnight.config.worldsettings.deathactions.PlayerDeathActions;
import de.eldoria.bloodnight.config.worldsettings.deathactions.PotionEffectSettings;
import de.eldoria.bloodnight.core.BloodNight;
import de.eldoria.bloodnight.core.manager.nightmanager.util.BloodNightData;
import de.eldoria.bloodnight.core.manager.nightmanager.util.NightUtil;
import de.eldoria.bloodnight.events.BloodNightBeginEvent;
import de.eldoria.bloodnight.events.BloodNightEndEvent;
import de.eldoria.bloodnight.specialmobs.SpecialMobUtil;
import de.eldoria.bloodnight.util.C;
import de.eldoria.eldoutilities.core.EldoUtilities;
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
import org.bukkit.plugin.PluginManager;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class NightManager extends BukkitRunnable implements Listener {

    private final Configuration configuration;
    /**
     * A set containing all world where a blood night is active.
     */
    private final Map<World, BloodNightData> bloodWorlds = new HashMap<>();

    private final Queue<World> startNight = new ArrayDeque<>();
    private final Queue<World> endNight = new ArrayDeque<>();

    private final Set<World> forceNights = new HashSet<>();

    private final PluginManager pluginManager = Bukkit.getPluginManager();
    private final ILocalizer localizer;
    private final MessageSender messageSender;
    private TimeManager timeManager;
    private SoundManager soundManager;

    private boolean initialized = false;

    public NightManager(Configuration configuration) {
        this.configuration = configuration;
        this.localizer = ILocalizer.getPluginLocalizer(BloodNight.class);
        this.messageSender = MessageSender.getPluginMessageSender(BloodNight.class);
        reload();
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

        changeNightStates();
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

    private void initializeBloodNight(World world) {
        initializeBloodNight(world, false);
    }

    private void initializeBloodNight(World world, boolean force) {
        WorldSettings settings = configuration.getWorldSettings(world.getName());

        if (isBloodNightActive(world)) return;

        if (!settings.isEnabled()) {
            BloodNight.logger().fine("Blood night in world " + world.getName() + " is not enabled. Will not initialize.");
            return;
        }

        // skip the calculation if a night should be forced.
        if (!force) {
            NightSelection sel = settings.getNightSelection();
            int val = ThreadLocalRandom.current().nextInt(101);
            sel.upcount();

            BloodNight.logger().config("Evaluating Blood Night State.");
            BloodNight.logger().config(sel.toString());

            int probability = sel.getCurrentProbability(world);

            BloodNight.logger().config("Current probability: " + probability);
            if (probability <= 0) return;
            if (val > probability) return;
        }
        BloodNightBeginEvent beginEvent = new BloodNightBeginEvent(world);
        // A new blood night has begun.
        pluginManager.callEvent(beginEvent);

        if (beginEvent.isCancelled()) {
            BloodNight.logger().config("BloodNight in " + world.getName() + " was cancelled by another plugin.");
            return;
        }
        BloodNight.logger().config("BloodNight in " + world.getName() + " activated.");


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
    }

    private void resolveBloodNight(World world) {
        if (!isBloodNightActive(world)) return;

        BloodNight.logger().config("BloodNight in " + world.getName() + " resolved.");

        WorldSettings settings = configuration.getWorldSettings(world.getName());

        if (settings.getNightSettings().isCustomNightDuration()) {
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
            timeManager.removeCustomTime(world);
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

        worldSettings.getSoundSettings().playEndsound(player);
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
        if (!isBloodNightActive(event.getEntity().getWorld())) return;
        PlayerDeathActions actions = configuration.getWorldSettings(event.getEntity().getWorld())
                .getDeathActionSettings()
                .getPlayerDeathActions();
        SpecialMobUtil.dispatchShockwave(actions.getShockwaveSettings(), event.getEntity().getLocation());
        SpecialMobUtil.dispatchLightning(actions.getLightningSettings(), event.getEntity().getLocation());

        if (actions.getLoseInvProbability() > ThreadLocalRandom.current().nextInt(100)) {
            event.getDrops().clear();
        }

        if (actions.getLoseExpProbability() > ThreadLocalRandom.current().nextInt(100)) {
            event.setDroppedExp(0);
        }

        for (String deathCommand : actions.getDeathCommands()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), deathCommand.replace("{player}", event.getEntity().getName()));
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        PlayerDeathActions actions = configuration.getWorldSettings(player.getWorld())
                .getDeathActionSettings()
                .getPlayerDeathActions();
        if (!isBloodNightActive(player.getWorld()) || event.isBedSpawn() || event.isAnchorSpawn()) {
            return;
        }

        EldoUtilities.getDelayedActions().schedule(() -> {
            for (PotionEffectSettings value : actions.getRespawnEffects().values()) {
                player.addPotionEffect(new PotionEffect(value.getEffectType(), value.getDuration() * 20, 1, false));
            }
        }, 1);
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

        if (timeManager != null) {
            if (!timeManager.isCancelled()) {
                timeManager.cancel();
            }
        }
        timeManager = new TimeManager(configuration, this);
        BloodNight.getInstance().registerListener(timeManager);
        timeManager.runTaskTimer(BloodNight.getInstance(), 1, 5);
        if (soundManager != null) {
            if (!soundManager.isCancelled()) {
                soundManager.cancel();
            }
        }
        soundManager = new SoundManager(this, configuration);
        soundManager.runTaskTimer(BloodNight.getInstance(), 2, 5);

        timeManager.reload();
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
    public Map<World, BloodNightData> getBloodWorldsMap() {
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

    public void startNight(World world) {
        startNight.add(world);
    }

    public void endNight(World world) {
        endNight.add(world);
    }
}
