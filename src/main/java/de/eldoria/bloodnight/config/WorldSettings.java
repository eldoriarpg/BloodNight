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
    private boolean enabled;
    private NightSelection nightSelection;
    private NightSettings nightSettings;
    private MobSettings mobSettings;

    public WorldSettings(Map<String, Object> objectMap) {
        TypeResolvingMap map = SerializationUtil.mapOf(objectMap);
        worldName = map.getValue("world");
        enabled = map.getValue("enabled");
        nightSelection = map.getValue("nightSelection");
        nightSettings = map.getValue("nightSettings");
        mobSettings = map.getValue("mobSettings");
    }

    public WorldSettings(String world) {
        this.worldName = world;
        enabled = false;
        nightSelection = new NightSelection();
        nightSettings = new NightSettings();
        mobSettings = new MobSettings();
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        return SerializationUtil.newBuilder()
                .add("world", worldName)
                .add("enabled", enabled)
                .add("nightSelection", nightSelection)
                .add("nightSettings", nightSettings)
                .add("mobSettings", mobSettings)
                .build();
    }
}
