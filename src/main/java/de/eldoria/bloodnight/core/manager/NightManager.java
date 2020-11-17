package de.eldoria.bloodnight.core.manager;

import de.eldoria.bloodnight.config.Configuration;
import de.eldoria.bloodnight.config.worldsettings.BossBarSettings;
import de.eldoria.bloodnight.config.worldsettings.NightSelection;
import de.eldoria.bloodnight.config.worldsettings.NightSettings;
import de.eldoria.bloodnight.config.worldsettings.WorldSettings;
import de.eldoria.bloodnight.core.BloodNight;
import de.eldoria.bloodnight.core.events.BloodNightBeginEvent;
import de.eldoria.bloodnight.core.events.BloodNightEndEvent;
import de.eldoria.bloodnight.core.manager.nightmanager.BloodNightData;
import de.eldoria.bloodnight.core.manager.nightmanager.NightUtil;
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
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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

		refreshTime();

		playRandomSound();
	}

	private void refreshTime() {
		for (Map.Entry<World, BloodNightData> entry : bloodWorlds.entrySet()) {
			WorldSettings settings = configuration.getWorldSettings(entry.getKey().getName());
			NightSettings ns = settings.getNightSettings();
			if (ns.isCustomNightDuration()) {
				double calcTicks = NightUtil.getNightTicksPerTick(entry.getKey(), settings);

				double time = customTimes.compute(entry.getKey().getName(),
						(key, old) -> (old == null ? entry.getKey().getFullTime() : old) + calcTicks);

				entry.getKey().setFullTime(Math.round(time));
			}
		}
	}

	private void playRandomSound() {
		for (BloodNightData data : bloodWorlds.values()) {
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
				if (BloodNight.isDebug()) {
					BloodNight.logger().info("Removed 1 boss bar" + next.getKey());
				}
			}
		}
		BloodNight.logger().info("Removed " + i + " hanging boss bars.");
	}

	private void calcualteWorldState(World world) {
		boolean current = NightUtil.isNight(world, configuration.getWorldSettings(world));
		boolean old = timeState.getOrDefault(world.getName(), false);

		if (current == old) {
			if (bloodWorlds.containsKey(world)) {
				BloodNightData bloodNightData = bloodWorlds.get(world);
				ObjUtil.nonNull(bloodNightData.getBossBar(), bossBar -> {
					bossBar.setProgress(NightUtil.getNightProgress(world, configuration.getWorldSettings(world)));
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
			int val = ThreadLocalRandom.current().nextInt(101);

			sel.upcount();

			int probability = sel.getCurrentProbability(world);
			if (probability <= 0) return;
			if (val > probability) return;
		}

		BloodNightBeginEvent beginEvent = new BloodNightBeginEvent(world);
		// A new blood night has begun.
		pluginManager.callEvent(beginEvent);

		if (beginEvent.isCancelled()) {
			if (BloodNight.isDebug()) {
				BloodNight.logger().info("BloodNight in " + world.getName() + " was canceled by another plugin.");
			}
			return;
		}
		if (BloodNight.isDebug()) {
			BloodNight.logger().info("BloodNight in " + world.getName() + " activated.");
		}


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
		if (BloodNight.isDebug()) {
			BloodNight.logger().info("BloodNight in " + world.getName() + " resolved.");
		}

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

		bloodWorlds.remove(world).resolveAll();
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

		worldSettings.getSoundSettings().playStartSound(player);

		bloodNightData.addPlayer(player);
	}

	private void disableBloodNightForPlayer(Player player, World world) {
		WorldSettings worldSettings = configuration.getWorldSettings(player.getWorld().getName());

		if (BloodNight.isDebug()) {
			BloodNight.logger().info("Resolving blood night for player " + player.getName());
		}

		bloodWorlds.get(world).removePlayer(player);

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


	// <--- Utility functions ---> //


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

	public void forceNight(World world) {
		forceNight.add(world);
		if (NightUtil.isNight(world, configuration.getWorldSettings(world))) {
			initializeBloodNight(world);
		}
	}

	public void cancelNight(World world) {
		resolveBloodNight(world);
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
}
