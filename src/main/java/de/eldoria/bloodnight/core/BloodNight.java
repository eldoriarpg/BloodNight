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
import de.eldoria.bloodnight.core.api.BloodNightAPI;
import de.eldoria.bloodnight.core.manager.MobManager;
import de.eldoria.bloodnight.core.manager.NightManager;
import de.eldoria.bloodnight.core.manager.NotificationManager;
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

public class BloodNight extends EldoPlugin {

    @Getter
    private static BloodNight instance;
    private static boolean debug = false;
    private NightManager nightManager;
    private MobManager mobManager;
    private Configuration configuration;
    private InventoryListener inventoryListener;
    private boolean initialized = false;
    private BloodNightAPI bloodNightAPI;

    public static NamespacedKey getNamespacedKey(String string) {
        return new NamespacedKey(instance, string);
    }

    public static BloodNightAPI getBloodNightAPI() {
        return instance.bloodNightAPI;
    }

    public static boolean isDebug() {
        return debug;
    }

    @Override
    public void onEnable() {
        if (!initialized) {
            instance = this;
            registerSerialization();
            configuration = new Configuration(this);

            debug = configuration.getGeneralSettings().isDebug();

            ILocalizer.create(this, configuration.getGeneralSettings().getLanguage(), "de_DE", "en_US");
            MessageSender.create(this, "§4[BN] ", '2', 'c');
            registerListener();
            bloodNightAPI = new BloodNightAPI(nightManager);
            registerCommand("bloodnight",
                    new BloodNightCommand(configuration, this, nightManager, mobManager, inventoryListener));

            enableMetrics();

            if (configuration.isUpdateReminder()) {
                Updater.Spigot(new SpigotUpdateData(this, Permissions.RELOAD, true, 85095));
            }
        }

        onReload();

        if (initialized) {
            logger().info("BloodNight reloaded!");
        } else {
            logger().info("BloodNight enabled!");
            initialized = true;
        }
    }

    public void onReload() {
        configuration.reload();
        ILocalizer.getPluginLocalizer(this).setLocale(configuration.getGeneralSettings().getLanguage());
        debug = configuration.getGeneralSettings().isDebug();

        if (debug) {
            logger().info("§cDebug mode active");
        }
        nightManager.reload();
    }

    private void registerListener() {
        PluginManager pm = Bukkit.getPluginManager();

        MessageSender messageSender = MessageSender.getPluginMessageSender(this);
        nightManager = new NightManager(configuration);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, nightManager, 100, 1);
        pm.registerEvents(new NotificationManager(configuration, nightManager), this);
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
    }

    private void enableMetrics() {
        Metrics metrics = new Metrics(this, 9123);
        if (metrics.isEnabled()) {
            logger().info("§2Metrics enabled. Thank you! (> ^_^ )>");
            return;
        }
        logger().info("§2Metrics are not enabled. Metrics help me to stay motivated. Please enable it.");
    }

    @Override
    public void onDisable() {
        if (nightManager != null) {
            nightManager.shutdown();
        }
        logger().info("Blood Night disabled!");
    }
}
