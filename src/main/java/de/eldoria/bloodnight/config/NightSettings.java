package de.eldoria.bloodnight.config;

import de.eldoria.eldoutilities.serialization.SerializationUtil;
import de.eldoria.eldoutilities.serialization.TypeResolvingMap;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
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
     * The modifier which will be muliplied with the droppes exp of a monster.
     */
    private double experienceMultiplier;

    /**
     * The modifier which will be multiplied with the dropped item amount.
     */
    private double dropMultiplier;

    /**
     * sleep time will be set for every player when the nights starts and will be reset to earlier value when the night ends
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

    /**
     * List of worlds where blood night is active.
     */
    private List<String> worlds;

    public NightSettings() {
        monsterDamageMultiplier = 2;
        playerDamageMultiplier = 0.5;
        experienceMultiplier = 3;
        dropMultiplier = 2;
        forcePhantoms = true;
        skippable = false;
        nightBegin = 14000;
        nightEnd = 23000;
        worlds = new ArrayList<>(Collections.singletonList("world"));
    }

    public NightSettings(Map<String, Object> objectMap) {
        TypeResolvingMap map = SerializationUtil.mapOf(objectMap);
        monsterDamageMultiplier = map.getValue("monsterDamageMultiplier");
        playerDamageMultiplier = map.getValue("playerDamageMultiplier");
        experienceMultiplier = map.getValue("experienceMultiplier");
        dropMultiplier = map.getValue("dropMultiplier");
        forcePhantoms = map.getValue("forcePhantoms");
        skippable = map.getValue("skippable");
        nightBegin = map.getValue("nightBegin");
        nightEnd = map.getValue("nightEnd");
        worlds = map.getValue("worlds");
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        return SerializationUtil.newBuilder()
                .add("monsterDamageMultiplier", monsterDamageMultiplier)
                .add("playerDamageMultiplier", playerDamageMultiplier)
                .add("forcePhantoms", forcePhantoms)
                .add("skippable", skippable)
                .add("nightBegin", nightBegin)
                .add("nightEnd", nightEnd)
                .add("worlds", worlds)
                .build();
    }
}
