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
    private double monsterDamageMultiplier;

    /**
     * The modifier which will be multiplied with player damage when dealing damage to monsters.
     */
    private double playerDamageMultiplier;

    /**
     * The modifier which will be muliplied with the dropped exp of a monster.
     */
    private double experienceMultiplier;

    /**
     * The modifier which will be multiplied with the dropped item amount.
     */
    private double dropMultiplier;

    /**
     * Sleep time will be set for every player when the nights starts and will be reset to earlier value when the night ends
     */
    private boolean forcePhantoms;

    /**
     * If false a blood night can not be skipped by sleeping in a bed.
     */
    private boolean skippable;
    /**
     * Tick when a night starts to be a night.
     */
    private int nightBegin;

    /**
     * Tick when a night stops to be a night.
     */
    private int nightEnd;

    // TODO implement
    private boolean boosBar;
    private boolean applyEffect;

    private List<String> startCommands;
    private List<String> endCommands;

    private boolean overrideNightDuration;

    private int nightDuration = 600;

    public NightSettings() {
        monsterDamageMultiplier = 2;
        playerDamageMultiplier = 0.5;
        experienceMultiplier = 4;
        dropMultiplier = 2;
        forcePhantoms = true;
        skippable = false;
        nightBegin = 14000;
        nightEnd = 23000;
    }

    public NightSettings(Map<String, Object> objectMap) {
        TypeResolvingMap map = SerializationUtil.mapOf(objectMap);
        monsterDamageMultiplier = map.getValueOrDefault("monsterDamageMultiplier", 2d);
        playerDamageMultiplier = map.getValueOrDefault("playerDamageMultiplier", 0.5);
        experienceMultiplier = map.getValueOrDefault("experienceMultiplier", 4d);
        dropMultiplier = map.getValueOrDefault("dropMultiplier", 2d);
        forcePhantoms = map.getValueOrDefault("forcePhantoms", true);
        skippable = map.getValueOrDefault("skippable", false);
        nightBegin = map.getValueOrDefault("nightBegin", 13000);
        nightEnd = map.getValueOrDefault("nightEnd", 23000);
        overrideNightDuration = map.getValueOrDefault("overrideNightDuration", false);
        nightDuration = map.getValueOrDefault("nightEnd", 600);
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
