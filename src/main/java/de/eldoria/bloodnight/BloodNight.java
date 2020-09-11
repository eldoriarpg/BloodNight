package de.eldoria.bloodnight;

import de.eldoria.bloodnight.config.Configuration;
import de.eldoria.bloodnight.listener.BedListener;
import de.eldoria.bloodnight.listener.DamageListener;
import de.eldoria.bloodnight.listener.LootListener;
import de.eldoria.bloodnight.listener.MessageListener;
import de.eldoria.bloodnight.listener.NightListener;
import de.eldoria.eldoutilities.localization.Localizer;
import de.eldoria.eldoutilities.messages.MessageSender;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Locale;

public class BloodNight extends JavaPlugin {

    @Getter
    private static BloodNight instance;
    private NightListener nightListener;
    private Localizer localizer;
    private Configuration configuration;
    private MessageSender messageSender;

    @Override
    public void onEnable() {
        instance = this;
        configuration = new Configuration(this);

        localizer = new Localizer(this, configuration.getGeneralSettings().getLanguage(), "messages",
                "messages", Locale.US, "de_DE", "en_US");
        MessageSender.create(this, "§4[BN] ", '2', 'c');
    }

    private void registerListener() {
        PluginManager pm = Bukkit.getPluginManager();

        nightListener = new NightListener(configuration);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, nightListener, 10, 5);
        pm.registerEvents(new MessageListener(localizer, nightListener, MessageSender.get(this)), this);
        pm.registerEvents(nightListener, this);
        pm.registerEvents(new DamageListener(nightListener, configuration), this);
        pm.registerEvents(new BedListener(configuration, nightListener, localizer, messageSender), this);
        pm.registerEvents(new LootListener(nightListener, configuration), this);
    }

    @Override
    public void onDisable() {
        nightListener.shutdown();
        super.onDisable();
    }
}
