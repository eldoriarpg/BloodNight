package de.eldoria.bloodnight.config.worldsettings;

import de.eldoria.eldoutilities.serialization.SerializationUtil;
import de.eldoria.eldoutilities.serialization.TypeResolvingMap;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@SerializableAs("bloodNightNightSettings")
public class NightSettings implements ConfigurationSerializable {

    /**
     * If false a blood night can not be skipped by sleeping in a bed.
     */
    private boolean skippable = false;
    /**
     * Tick when a night starts to be a night.
     */
    private int nightBegin = 14000;

    /**
     * Tick when a night stops to be a night.
     */
    private int nightEnd = 23000;

    private List<String> startCommands = new ArrayList<>();
    private List<String> endCommands = new ArrayList<>();

    private boolean overrideNightDuration = false;

    private int nightDuration = 600;

    public NightSettings() {
    }

    public NightSettings(Map<String, Object> objectMap) {
        TypeResolvingMap map = SerializationUtil.mapOf(objectMap);
        skippable = map.getValueOrDefault("skippable", skippable);
        nightBegin = map.getValueOrDefault("nightBegin", nightBegin);
        nightEnd = map.getValueOrDefault("nightEnd", nightEnd);
        startCommands = map.getValueOrDefault("startCommands", startCommands);
        endCommands = map.getValueOrDefault("endCommands", endCommands);
        overrideNightDuration = map.getValueOrDefault("overrideNightDuration", overrideNightDuration);
        nightDuration = map.getValueOrDefault("nightDuration", nightDuration);
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        return SerializationUtil.newBuilder()
                .add("skippable", skippable)
                .add("nightBegin", nightBegin)
                .add("nightEnd", nightEnd)
                .add("startCommands", startCommands)
                .add("endCommands", endCommands)
                .add("overrideNightDuration", overrideNightDuration)
                .add("nightDuration", nightDuration)
                .build();
    }
}
