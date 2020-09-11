package de.eldoria.bloodnight.config;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

@Getter
@Setter
public class Configuration {
    private final Plugin plugin;
    /**
     * Version of the config.
     */
    private int version;

    private GeneralSettings generalSettings;
    private NightSelection nightSelection;
    private NightSettings nightSettings;

    public Configuration(Plugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        FileConfiguration config = plugin.getConfig();

        if (!config.contains("version")) {
            init();
        }

        version = config.getInt("version");
        generalSettings = (GeneralSettings) config.get("generalSettings");
        nightSelection = (NightSelection) config.get("nightSelection");
        nightSettings = (NightSettings) config.get("nightSettings");
    }

    private void init() {
        version = 1;
        generalSettings = new GeneralSettings();
        nightSelection = new NightSelection();
        nightSettings = new NightSettings();
        safeConfig();
    }

    /**
     * Safe current config settings.
     */
    public void safeConfig() {
        FileConfiguration config = plugin.getConfig();
        config.set("version", version);
        config.set("generalSettings", generalSettings);
        config.set("nightSelection", nightSelection);
        config.set("nightSettings", nightSettings);
        plugin.saveConfig();
    }
}
