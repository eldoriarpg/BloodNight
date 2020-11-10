package de.eldoria.bloodnight.config;

import de.eldoria.bloodnight.config.generalsettings.GeneralSettings;
import de.eldoria.bloodnight.config.worldsettings.WorldSettings;
import de.eldoria.bloodnight.core.BloodNight;
import de.eldoria.eldoutilities.utils.ObjUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class Configuration {
    private final Plugin plugin;
    private final Map<String, WorldSettings> worldSettings = new HashMap<>();
    private final Path worldSettingPaths;
    private final Map<String, FileConfiguration> worldConfigs = new HashMap<>();
    /**
     * Version of the config.
     */
    @Getter
    private int version;

    @Getter
    private GeneralSettings generalSettings;


    public Configuration(Plugin plugin) {
        this.plugin = plugin;
        worldSettingPaths = Paths.get(plugin.getDataFolder().toPath().toString(), "worldSettings");
        reload();
    }

    public WorldSettings getWorldSettings(World key) {
        return loadWorldSettings(key.getName(), false);
    }

    public WorldSettings getWorldSettings(String key) {
        return loadWorldSettings(key, false);
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

        if (version <= 1) {
            BloodNight.logger().info("Migrating config to v2");
            migrateToV2();
            BloodNight.logger().info("Migration of config to v2 done.");
        }

        generalSettings = config.getObject("generalSettings", GeneralSettings.class);

        for (World world : Bukkit.getWorlds()) {
            loadWorldSettings(world.getName(), true);
        }

        saveConfig();
    }

    private void migrateToV2() {
        FileConfiguration config = plugin.getConfig();
        config.set("version", 2);
        List<WorldSettings> worldList = ObjUtil.nonNull((List<WorldSettings>) config.get("worldSettings", new ArrayList<>()), new ArrayList<>());

        Path worldSettings = Paths.get(plugin.getDataFolder().toPath().toString(), "worldSettings");
        try {
            Files.createDirectories(worldSettings);
        } catch (IOException e) {
            BloodNight.logger().log(Level.WARNING, "Could not create world settings directory.", e);
        }

        for (WorldSettings settings : worldList) {
            Path worldSetting = Paths.get(worldSettings.toString(), settings.getWorldName() + ".yml");
            File worldConfig = worldSetting.toFile();
            YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(worldConfig);

            if (!worldConfig.exists()) {
                createWorldConfigFile(settings.getWorldName(), settings);
                BloodNight.logger().info("Migrated settings for " + settings.getWorldName());
            }
        }
        config.set("worldSettings", null);
    }

    private WorldSettings loadWorldSettings(String world, boolean reload) {
        if (reload) {
            return worldSettings.compute(world, (k, v) -> {
                FileConfiguration config = worldConfigs.compute(
                        k, (k2, v2) -> {
                            Path worldSetting = Paths.get(worldSettingPaths.toString(), world + ".yml");
                            if (!worldSetting.toFile().exists()) {
                                createWorldConfigFile(world, null);
                            }

                            return YamlConfiguration.loadConfiguration(worldSetting.toFile());
                        });
                return config.getObject("settings", WorldSettings.class);
            });
        }
        return worldSettings.computeIfAbsent(world, k -> {
            FileConfiguration config = worldConfigs.computeIfAbsent(k, k2 -> {
                Path worldSetting = Paths.get(worldSettingPaths.toString(), world + ".yml");
                if (!worldSetting.toFile().exists()) {
                    createWorldConfigFile(world, null);
                }

                return YamlConfiguration.loadConfiguration(worldSetting.toFile());
            });
            return config.getObject("settings", WorldSettings.class);
        });
    }

    private FileConfiguration createWorldConfigFile(String world, @Nullable WorldSettings settings) {
        Path worldSetting = Paths.get(worldSettingPaths.toString(), world + ".yml");

        try {
            Files.createFile(worldSetting);
        } catch (IOException e) {
            BloodNight.logger().log(Level.WARNING, "Could not create world settings.", e);
            return null;
        }

        YamlConfiguration tempConfig = YamlConfiguration.loadConfiguration(worldSetting.toFile());
        tempConfig.set("version", 1);
        tempConfig.set("settings", ObjUtil.nonNull(settings, new WorldSettings(world)));
        try {
            tempConfig.save(worldSetting.toFile());
        } catch (IOException e) {
            BloodNight.logger().log(Level.WARNING, "Could not save config.");
        }
        return tempConfig;
    }

    private void saveWorldConfig(String world) {
        Path path = Paths.get(worldSettingPaths.toString(), world + ".yml");
        WorldSettings worldSettings = getWorldSettings(world);
        FileConfiguration config = worldConfigs.get(world);
        config.set("settings", worldSettings);
        try {
            config.save(path.toFile());
        } catch (IOException e) {
            BloodNight.logger().log(Level.WARNING, "Could not save world settings", e);
        }
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
        config.set("generalSettings", generalSettings);

        for (String world : worldConfigs.keySet()) {
            saveWorldConfig(world);
        }

        if (generalSettings.isDebug()) {
            BloodNight.logger().info("Saved config.");
        }
        plugin.saveConfig();
    }

    public void cleanup() {
        List<String> invalid = new ArrayList<>();
        BloodNight.logger().info("Performing config cleanup.");
        for (Map.Entry<String, WorldSettings> entry : this.worldSettings.entrySet()) {
            if (Bukkit.getWorld(entry.getKey()) == null) {
                invalid.add(entry.getKey());
                BloodNight.logger().info("Didnt found a matching world for " + entry.getKey() + "Will will discard Settings");
            }
        }

        invalid.forEach(this.worldSettings::remove);

        saveConfig();
    }
}
