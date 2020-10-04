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
import de.eldoria.bloodnight.core.manager.MobManager;
import de.eldoria.bloodnight.core.manager.NightManager;
import de.eldoria.bloodnight.core.manager.NotificationManager;
import de.eldoria.eldoutilities.localization.Localizer;
import de.eldoria.eldoutilities.messages.MessageSender;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.logging.Logger;

public class BloodNight extends JavaPlugin {

    @Getter
    private static BloodNight instance;
    private static Logger logger;
    private NightManager nightManager;
    private MobManager mobManager;
    private Localizer localizer;
    private Configuration configuration;
    private InventoryListener inventoryListener;
    private boolean initialized = false;
    private static boolean debug = false;

    @SuppressWarnings("StaticVariableUsedBeforeInitialization")
    @NotNull
    public static Logger logger() {
        return logger;
    }

    public static NamespacedKey getNamespacedKey(String string) {
        return new NamespacedKey(instance, string);
    }

    public static Localizer localizer() {
        return instance.localizer;
    }

    public static boolean isDebug() {
        return debug;
    }

    @Override
    public void onEnable() {
        if (!initialized) {
            instance = this;
            logger = getLogger();
            registerSerialization();
            configuration = new Configuration(this);

            debug = configuration.getGeneralSettings().isDebug();

            localizer = new Localizer(this, configuration.getGeneralSettings().getLanguage(), "messages",
                    "messages", Locale.US, "de_DE", "en_US");
            MessageSender.create(this, "§4[BN] ", '2', 'c');
            registerListener();
            registerCommand("bloodnight",
                    new BloodNightCommand(configuration, localizer, this, nightManager, mobManager, inventoryListener));
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
        localizer.setLocale(configuration.getGeneralSettings().getLanguage());
        debug = configuration.getGeneralSettings().isDebug();
        nightManager.reload();
    }

    private void registerListener() {
        PluginManager pm = Bukkit.getPluginManager();

        MessageSender messageSender = MessageSender.get(this);
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

    @Override
    public void onDisable() {
        if (nightManager != null) {
            nightManager.shutdown();
        }
        logger().info("Blood Night disabled!");
    }

    private void registerCommand(String command, TabExecutor executor) {
        PluginCommand cmd = getCommand(command);
        if (cmd != null) {
            cmd.setExecutor(executor);
            return;
        }
        logger().warning("Command " + command + " not found!");
    }
}
