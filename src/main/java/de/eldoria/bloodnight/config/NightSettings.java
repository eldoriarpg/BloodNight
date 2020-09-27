package de.eldoria.bloodnight.config;

import de.eldoria.eldoutilities.serialization.SerializationUtil;
import de.eldoria.eldoutilities.serialization.TypeResolvingMap;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@SerializableAs("bloodNightNightSettings")
public class NightSettings implements ConfigurationSerializable {
    /**
     * The modifier which will be multiplied with monster damage when dealing damage to players.
     */
    private double monsterDamageMultiplier = 2;

    /**
     * The modifier which will be multiplied with player damage when dealing damage to monsters.
     */
    private double playerDamageMultiplier = 0.5;

    /**
     * The modifier which will be muliplied with the dropped exp of a monster.
     */
    private double experienceMultiplier = 4;

    /**
     * The modifier which will be multiplied with the dropped item amount.
     */
    private double dropMultiplier = 2;

    /**
     * Sleep time will be set for every player when the nights starts and will be reset to earlier value when the night ends
     */
    private boolean forcePhantoms = true;

    /**
     * If false a blood night can not be skipped by sleeping in a bed.
     */
    private boolean skippable = false;
    /**
     * Tick when a night starts to be a night.
     */
    private int nightBegin= 14000;

    /**
     * Tick when a night stops to be a night.
     */
    private int nightEnd = 23000;

    private List<String> startCommands;
    private List<String> endCommands;

    private boolean overrideNightDuration = false;

    private int nightDuration = 600;

    public NightSettings() {
    }

    public NightSettings(Map<String, Object> objectMap) {
        TypeResolvingMap map = SerializationUtil.mapOf(objectMap);
        monsterDamageMultiplier = map.getValueOrDefault("monsterDamageMultiplier", monsterDamageMultiplier);
        playerDamageMultiplier = map.getValueOrDefault("playerDamageMultiplier", playerDamageMultiplier);
        experienceMultiplier = map.getValueOrDefault("experienceMultiplier", experienceMultiplier);
        dropMultiplier = map.getValueOrDefault("dropMultiplier", dropMultiplier);
        forcePhantoms = map.getValueOrDefault("forcePhantoms", forcePhantoms);
        skippable = map.getValueOrDefault("skippable", skippable);
        nightBegin = map.getValueOrDefault("nightBegin", nightBegin);
        nightEnd = map.getValueOrDefault("nightEnd", nightEnd);
        overrideNightDuration = map.getValueOrDefault("overrideNightDuration", overrideNightDuration);
        nightDuration = map.getValueOrDefault("nightEnd", nightDuration);
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        return SerializationUtil.newBuilder()
                .add("monsterDamageMultiplier", monsterDamageMultiplier)
                .add("playerDamageMultiplier", playerDamageMultiplier)
                .add("experienceMultiplier", experienceMultiplier)
                .add("dropMultiplier", dropMultiplier)
                .add("forcePhantoms", forcePhantoms)
                .add("skippable", skippable)
                .add("nightBegin", nightBegin)
                .add("nightEnd", nightEnd)
                .add("overrideNightDuration", overrideNightDuration)
                .add("nightDuration", nightDuration)
                .build();
    }
}
