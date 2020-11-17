package de.eldoria.bloodnight.core;

import de.eldoria.bloodnight.command.BloodNightCommand;
import de.eldoria.bloodnight.command.InventoryListener;
import de.eldoria.bloodnight.config.Configuration;
import de.eldoria.bloodnight.config.generalsettings.GeneralSettings;
import de.eldoria.bloodnight.config.worldsettings.BossBarSettings;
import de.eldoria.bloodnight.config.worldsettings.NightSelection;
import de.eldoria.bloodnight.config.worldsettings.NightSettings;
import de.eldoria.bloodnight.config.worldsettings.WorldSettings;
import de.eldoria.bloodnight.config.worldsettings.mobsettings.Drop;
import de.eldoria.bloodnight.config.worldsettings.mobsettings.MobSetting;
import de.eldoria.bloodnight.config.worldsettings.mobsettings.MobSettings;
import de.eldoria.bloodnight.config.worldsettings.sound.SoundEntry;
import de.eldoria.bloodnight.config.worldsettings.sound.SoundSettings;
import de.eldoria.bloodnight.core.api.BloodNightAPI;
import de.eldoria.bloodnight.core.api.IBloodNightAPI;
import de.eldoria.bloodnight.core.manager.MobManager;
import de.eldoria.bloodnight.core.manager.NightManager;
import de.eldoria.bloodnight.core.manager.NotificationManager;
import de.eldoria.bloodnight.core.mobfactory.MobFactory;
import de.eldoria.bloodnight.core.mobfactory.SpecialMobRegistry;
import de.eldoria.bloodnight.hooks.HookService;
import de.eldoria.bloodnight.util.Permissions;
import de.eldoria.eldoutilities.localization.ILocalizer;
import de.eldoria.eldoutilities.messages.MessageSender;
import de.eldoria.eldoutilities.plugin.EldoPlugin;
import de.eldoria.eldoutilities.updater.Updater;
import de.eldoria.eldoutilities.updater.butlerupdater.ButlerUpdateData;
import lombok.Getter;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.PluginManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class BloodNight extends EldoPlugin {

	@Getter
	private static BloodNight instance;
	private NightManager nightManager;
	private MobManager mobManager;
	private Configuration configuration;
	private InventoryListener inventoryListener;
	private boolean initialized = false;
	private BloodNightAPI bloodNightAPI;
	private HookService hookService;

	public static NamespacedKey getNamespacedKey(String string) {
		return new NamespacedKey(instance, string.replace(" ", "_"));
	}

	public static IBloodNightAPI getBloodNightAPI() {
		return instance.bloodNightAPI;
	}

	public static boolean isDebug() {
		return Configuration.isDebug(BloodNight.class);
	}

	@Override
	public void onEnable() {
		if (!initialized) {
			instance = this;
			registerSerialization();
			configuration = new Configuration(this);

			ILocalizer localizer = ILocalizer.create(this, "de_DE", "en_US", "es_ES", "tr", "zh_CN");

			Map<String, String> mobLocaleCodes = SpecialMobRegistry.getRegisteredMobs().stream()
					.map(MobFactory::getMobName)
					.collect(Collectors.toMap(
							k -> "mob." + k,
							k -> String.join(" ", k.split("(?<=.)(?=\\p{Lu})"))));
			localizer.addLocaleCodes(mobLocaleCodes);

			localizer.setLocale(configuration.getGeneralSettings().getLanguage());
			MessageSender.create(this, configuration.getGeneralSettings().getPrefix() + " ", '2', 'c');

			registerListener();
			bloodNightAPI = new BloodNightAPI(nightManager, configuration);
			registerCommand("bloodnight",
					new BloodNightCommand(configuration, this, nightManager, mobManager, inventoryListener));

			enableMetrics();

			if (configuration.getGeneralSettings().isUpdateReminder()) {
				Updater.Butler(new ButlerUpdateData(this, Permissions.RELOAD, true,
						configuration.getGeneralSettings().isAutoUpdater(), 4, "https://plugins.eldoria.de"))
						.start();
			}

			hookService = new HookService(this, configuration, nightManager);
			hookService.setup();

			lateInit();
		}

		onReload();

		if (initialized) {
			logger().info("§2BloodNight reloaded!");
		} else {
			logger().info("§2BloodNight enabled!");
			initialized = true;
		}

		IBloodNightAPI bloodNightAPI = BloodNight.getBloodNightAPI();
	}

	public void onReload() {
		configuration.reload();
		ILocalizer.getPluginLocalizer(this).setLocale(configuration.getGeneralSettings().getLanguage());

		if (isDebug()) {
			logger().info("§cDebug mode active");
		}
		nightManager.reload();
	}

	private void lateInit() {
		PluginManager pm = Bukkit.getPluginManager();
		pm.registerEvents(new NotificationManager(configuration, nightManager, hookService), this);
	}

	private void registerListener() {
		PluginManager pm = Bukkit.getPluginManager();

		MessageSender messageSender = MessageSender.getPluginMessageSender(this);
		nightManager = new NightManager(configuration);
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, nightManager, 100, 1);
		pm.registerEvents(nightManager, this);
		mobManager = new MobManager(nightManager, configuration);
		inventoryListener = new InventoryListener(configuration);
		pm.registerEvents(inventoryListener, this);
		pm.registerEvents(mobManager, this);
		// Schedule mobManager
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, mobManager, 100, 1);
	}

	private void registerSerialization() {
		ConfigurationSerialization.registerClass(GeneralSettings.class);
		ConfigurationSerialization.registerClass(NightSelection.class);
		ConfigurationSerialization.registerClass(NightSettings.class);
		ConfigurationSerialization.registerClass(MobSettings.class);
		ConfigurationSerialization.registerClass(MobSetting.class);
		ConfigurationSerialization.registerClass(WorldSettings.class);
		ConfigurationSerialization.registerClass(Drop.class);
		ConfigurationSerialization.registerClass(BossBarSettings.class);
		ConfigurationSerialization.registerClass(MobSettings.MobTypes.class);
		ConfigurationSerialization.registerClass(SoundSettings.class);
		ConfigurationSerialization.registerClass(SoundEntry.class);
	}

	private void enableMetrics() {
		Metrics metrics = new Metrics(this, 9123);
		if (metrics.isEnabled()) {
			logger().info("§2Metrics enabled. Thank you! (> ^_^ )>");

			metrics.addCustomChart(new Metrics.MultiLineChart("update_settings", () -> {
				Map<String, Integer> map = new HashMap<>();
				map.put("Update Check", configuration.getGeneralSettings().isUpdateReminder() ? 1 : 0);
				if (configuration.getGeneralSettings().isUpdateReminder()) {
					map.put("Auto Update", configuration.getGeneralSettings().isUpdateReminder() ? 1 : 0);
					return map;
				}
				map.put("Auto Update", 0);
				return map;
			}));

			metrics.addCustomChart(new Metrics.MultiLineChart("mob_types", () -> {
				Map<String, Integer> map = new HashMap<>();
				for (MobFactory factory : SpecialMobRegistry.getRegisteredMobs()) {
					for (WorldSettings world : configuration.getWorldSettings().values()) {
						if (!world.isEnabled()) continue;
						Optional<MobSetting> mobByName = world.getMobSettings().getMobByName(factory.getMobName());
						map.compute(mobByName.get().getMobName(), (key, value) -> {
							if (value == null) {
								return mobByName.get().isActive() ? 1 : 0;
							}
							return value + (mobByName.get().isActive() ? 1 : 0);
						});
					}
				}
				return map;
			}));

			metrics.addCustomChart(new Metrics.MultiLineChart("night_selection", () -> {
				Map<String, Integer> map = new HashMap<>();
				for (WorldSettings world : configuration.getWorldSettings().values()) {
					if (!world.isEnabled()) continue;
					map.compute(world.getNightSelection().getNightSelectionType().toString(), (key, value) -> {
						if (value == null) {
							return 1;
						}
						return value + 1;
					});
				}
				return map;
			}));

			return;
		}

		logger().info("§2Metrics are not enabled. Metrics help me to stay motivated. Please enable it.");
	}

	@Override
	public void onDisable() {
		if (nightManager != null) {
			nightManager.shutdown();
		}
		if (hookService != null) {
			hookService.shutdown();
		}
		logger().info("Blood Night disabled!");
	}
}
