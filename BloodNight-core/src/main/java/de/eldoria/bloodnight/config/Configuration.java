package de.eldoria.bloodnight.config;

import de.eldoria.bloodnight.config.generalsettings.GeneralSettings;
import de.eldoria.bloodnight.config.worldsettings.WorldSettings;
import de.eldoria.bloodnight.core.BloodNight;
import de.eldoria.eldoutilities.configuration.EldoConfig;
import de.eldoria.eldoutilities.utils.ObjUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Configuration extends EldoConfig {
    private Map<String, WorldSettings> worldSettings;
    @Getter
    private GeneralSettings generalSettings;


    public Configuration(Plugin plugin) {
        super(plugin);
    }

    @Override
    protected void init() {
        worldSettings = new HashMap<>();
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

    @Override
    protected void reloadConfigs() {
        BloodNight.logger().info("Loading config.");

        if (getVersion() == -1) {
            initV2();
            save();
            reloadConfigs();
            return;
        }

        if (getVersion() <= 1) {
            BloodNight.logger().info("§2Migrating config to v2");
            migrateToV2();
            BloodNight.logger().info("§2Migration of config to v2 done.");
        }

        generalSettings = getMainConfig(plugin).getConfig().getObject("generalSettings", GeneralSettings.class);

        for (World world : Bukkit.getWorlds()) {
            loadWorldSettings(world.getName(), true);
        }
        save();
    }

    private void initV2() {
        BloodNight.logger().info("§eConfig is empty. Rebuilding config");
        setVersion(2, false);
        BloodNight.logger().info("§2Config version 2");
        generalSettings = new GeneralSettings();
        BloodNight.logger().info("§2Added general settings");
        BloodNight.logger().info("§2Config initialized");
    }

    private void migrateToV2() {
        setVersion(2, false);
        getMainConfig(plugin).getConfig().set("updateReminder", null);
        List<WorldSettings> worldList = ObjUtil.nonNull((List<WorldSettings>) getMainConfig(plugin).getConfig().get("worldSettings", new ArrayList<>()), new ArrayList<>());

        Path worldSettings = Paths.get(plugin.getDataFolder().toPath().toString(), "worldSettings");

        for (WorldSettings settings : worldList) {
            loadConfig(getWorldConfigPath(settings.getWorldName()), s -> {
                s.set("version", 1);
                s.set("settings", settings);
            }, true);
            BloodNight.logger().info("§2Migrated settings for " + settings.getWorldName());
        }
        getMainConfig(plugin).getConfig().set("worldSettings", null);
    }

    private @NotNull
    WorldSettings loadWorldSettings(String world, boolean reload) {
        if (reload) {
            return worldSettings.compute(world, (k, v) -> {
                FileConfiguration config = loadConfig(getWorldConfigPath(world), c -> {
                    c.set("version", 1);
                    c.set("settings", new WorldSettings(world));
                }, true);
                return config.getObject("settings", WorldSettings.class, new WorldSettings(world));
            });
        }
        return worldSettings.computeIfAbsent(world, k -> {
            FileConfiguration config = loadConfig(getWorldConfigPath(world), c -> {
                c.set("version", 1);
                c.set("settings", new WorldSettings(world));
            }, false);
            return config.getObject("settings", WorldSettings.class, new WorldSettings(world));
        });
    }

    @Override
    protected void saveConfigs() {
        getMainConfig(plugin).getConfig().set("generalSettings", generalSettings);

        for (Map.Entry<String, WorldSettings> entry : worldSettings.entrySet()) {
            ObjUtil.nonNull(loadConfig(getWorldConfigPath(entry.getKey()), null, false),
                    configuration -> {
                        configuration.set("settings", entry.getValue());
                    });
        }

        BloodNight.logger().config("Saved config.");

    }

    private @NotNull String getWorldConfigPath(String world) {
        return "worldSettings/" + world;
    }
}
