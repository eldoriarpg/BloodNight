package de.eldoria.bloodnight.config;

import de.eldoria.bloodnight.core.BloodNight;
import lombok.Getter;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Configuration {
    private final Plugin plugin;
    /**
     * Version of the config.
     */
    @Getter
    private int version;

    @Getter
    private GeneralSettings generalSettings;
    private Map<String, WorldSettings> worldSettings = new HashMap<>();


    public Configuration(Plugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public WorldSettings getWorldSettings(String key) {
        return worldSettings.computeIfAbsent(key, WorldSettings::new);
    }

    public WorldSettings getWorldSettings(World key) {
        return getWorldSettings(key.getName());
    }

    public void addWorldSettings(World world) {
        worldSettings.put(world.getName(), new WorldSettings(world.getName()));
    }

    public Map<String, WorldSettings> getWorldSettings() {
        return worldSettings;
    }

    public void reload() {
        FileConfiguration config = plugin.getConfig();

        BloodNight.logger().info("Loading config.");

        if (!config.contains("version")) {
            init();
        }

        version = config.getInt("version");
        generalSettings = (GeneralSettings) config.get("generalSettings", new GeneralSettings());
        List<WorldSettings> worldList = (List<WorldSettings>) config.get("worldSettings", new ArrayList<>());
        for (WorldSettings settings : worldList) {
            worldSettings.put(settings.getWorldName(), settings);
        }

        safeConfig();
    }

    private void init() {
        BloodNight.logger().info("Config is empty. Rebuilding config");
        version = 1;
        BloodNight.logger().info("Config version 1");
        generalSettings = new GeneralSettings();
        BloodNight.logger().info("Added general settings");
        worldSettings.put("world", new WorldSettings("world"));
        BloodNight.logger().info("Added default settings for world \"world\"");
        BloodNight.logger().info("Config initialized");
    }

    /**
     * Safe current config settings.
     */
    public void safeConfig() {
        FileConfiguration config = plugin.getConfig();
        config.set("version", version);
        config.set("generalSettings", generalSettings);
        config.set("worldSettings", new ArrayList<>(worldSettings.values()));
        plugin.saveConfig();
    }
}
