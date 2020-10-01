package de.eldoria.bloodnight.config;

import de.eldoria.eldoutilities.serialization.SerializationUtil;
import de.eldoria.eldoutilities.serialization.TypeResolvingMap;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@Getter
@Setter
@SerializableAs("bloodNightWorldSettings")
public class WorldSettings implements ConfigurationSerializable {

    private String worldName;
    private boolean enabled = false;
    private BossBarSettings bossBarSettings = new BossBarSettings();
    private NightSelection nightSelection = new NightSelection();
    private NightSettings nightSettings = new NightSettings();
    private MobSettings mobSettings = new MobSettings();

    public WorldSettings(Map<String, Object> objectMap) {
        TypeResolvingMap map = SerializationUtil.mapOf(objectMap);
        worldName = map.getValue("world");
        assert worldName == null : "World is null. This should not happen";
        enabled = map.getValueOrDefault("enabled", enabled);
        bossBarSettings = map.getValueOrDefault("bossBar", bossBarSettings);
        nightSelection = map.getValueOrDefault("nightSelection", nightSelection);
        nightSettings = map.getValueOrDefault("nightSettings", nightSettings);
        mobSettings = map.getValueOrDefault("mobSettings", mobSettings);
    }

    public WorldSettings(String world) {
        this.worldName = world;
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        return SerializationUtil.newBuilder()
                .add("world", worldName)
                .add("enabled", enabled)
                .add("bossBar", bossBarSettings)
                .add("nightSelection", nightSelection)
                .add("nightSettings", nightSettings)
                .add("mobSettings", mobSettings)
                .build();
    }
}
