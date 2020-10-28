package de.eldoria.bloodnight.config;

import de.eldoria.bloodnight.config.generalsettings.GeneralSettings;
import de.eldoria.bloodnight.config.worldsettings.WorldSettings;
import de.eldoria.bloodnight.core.BloodNight;
import de.eldoria.eldoutilities.utils.ObjUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Configuration {
    private final Plugin plugin;
    private final Map<String, WorldSettings> worldSettings = new HashMap<>();
    /**
     * Version of the config.
     */
    @Getter
    private int version;
    /**
     * Metrics enabled.
     */
    @Getter
    private boolean metrics;
    /**
     * Update reminder enabled.
     */
    @Getter
    private boolean updateReminder;

    @Getter
    private GeneralSettings generalSettings;


    public Configuration(Plugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public WorldSettings getWorldSettings(String key) {
        if (worldSettings.containsKey(key)) {
            return worldSettings.get(key);
        }
        BloodNight.logger().info("No world setting for " + key + " present. Computing default settings.");
        return worldSettings.computeIfAbsent(key, WorldSettings::new);
    }

    public WorldSettings getWorldSettings(World key) {
        return getWorldSettings(key.getName());
    }

    public Map<String, WorldSettings> getWorldSettings() {
        return worldSettings;
    }

    public void reload() {
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        BloodNight.logger().info("Loading config.");

        if (!config.contains("version")) {
            init();
            saveConfig();
            plugin.reloadConfig();
            config = plugin.getConfig();
        }

        version = config.getInt("version");
        metrics = config.getBoolean("metrics", true);
        updateReminder = config.getBoolean("updateReminder", true);
        generalSettings = (GeneralSettings) config.get("generalSettings", new GeneralSettings());
        worldSettings.clear();
        List<WorldSettings> worldList = ObjUtil.nonNull((List<WorldSettings>) config.get("worldSettings", new ArrayList<>()), new ArrayList<>());
        for (WorldSettings settings : worldList) {
            if (Bukkit.getWorld(settings.getWorldName()) != null) {
                worldSettings.put(settings.getWorldName(), settings);
                if (generalSettings.isDebug()) {
                    BloodNight.logger().info("Loading world settings for " + settings.getWorldName());
                }
            } else {
                if (generalSettings.isDebug()) {
                    BloodNight.logger().info("Didnt found a matching world for " + settings.getWorldName());
                }
            }
        }
        for (World world : Bukkit.getWorlds()) {
            getWorldSettings(world);
        }

        saveConfig();
    }

    private void init() {
        BloodNight.logger().info("Config is empty. Rebuilding config");
        version = 1;
        BloodNight.logger().info("Config version 1");
        generalSettings = new GeneralSettings();
        BloodNight.logger().info("Added general settings");
        BloodNight.logger().info("Config initialized");
    }

    /**
     * Safe current config settings.
     */
    public void saveConfig() {
        FileConfiguration config = plugin.getConfig();
        config.set("version", version);
        config.set("metrics", metrics);
        config.set("updateReminder", updateReminder);
        config.set("generalSettings", generalSettings);
        config.set("worldSettings", new ArrayList<>(worldSettings.values()));
        if (generalSettings.isDebug()) {
            BloodNight.logger().info("Saved config.");
        }
        plugin.saveConfig();
    }
}
