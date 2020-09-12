package de.eldoria.bloodnight;

import de.eldoria.bloodnight.command.BloodNightCommand;
import de.eldoria.bloodnight.config.Configuration;
import de.eldoria.bloodnight.config.GeneralSettings;
import de.eldoria.bloodnight.config.NightSelection;
import de.eldoria.bloodnight.config.NightSettings;
import de.eldoria.bloodnight.listener.BedListener;
import de.eldoria.bloodnight.listener.DamageListener;
import de.eldoria.bloodnight.listener.LootListener;
import de.eldoria.bloodnight.listener.MessageListener;
import de.eldoria.bloodnight.listener.NightListener;
import de.eldoria.eldoutilities.localization.Localizer;
import de.eldoria.eldoutilities.messages.MessageSender;
import lombok.Getter;
import org.bukkit.Bukkit;
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
    private NightListener nightListener;
    private Localizer localizer;
    private Configuration configuration;

    @SuppressWarnings("StaticVariableUsedBeforeInitialization")
    @NotNull
    public static Logger logger() {
        return logger;
    }

    @Override
    public void onEnable() {
        instance = this;
        logger = getLogger();
        registerSerialization();
        configuration = new Configuration(this);

        localizer = new Localizer(this, configuration.getGeneralSettings().getLanguage(), "messages",
                "messages", Locale.US, "de_DE", "en_US");
        MessageSender.create(this, "§4[BN] ", '2', 'c');
        registerListener();
        registerCommand("bloodnight", new BloodNightCommand(configuration, localizer, this, nightListener));

        logger().info("BloodNight enabled!");
    }

    private void registerListener() {
        PluginManager pm = Bukkit.getPluginManager();

        MessageSender messageSender = MessageSender.get(this);
        nightListener = new NightListener(configuration);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, nightListener, 10, 5);
        pm.registerEvents(new MessageListener(localizer, nightListener , messageSender), this);
        pm.registerEvents(nightListener, this);
        pm.registerEvents(new DamageListener(nightListener, configuration), this);
        pm.registerEvents(new BedListener(configuration, nightListener, localizer, messageSender), this);
        pm.registerEvents(new LootListener(nightListener, configuration), this);
    }

    private void registerSerialization() {
        ConfigurationSerialization.registerClass(GeneralSettings.class);
        ConfigurationSerialization.registerClass(NightSelection.class);
        ConfigurationSerialization.registerClass(NightSettings.class);
    }

    @Override
    public void onDisable() {
        if (nightListener != null) {
            nightListener.shutdown();
        }
        super.onDisable();
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
