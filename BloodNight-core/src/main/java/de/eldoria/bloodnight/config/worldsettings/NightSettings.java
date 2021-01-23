package de.eldoria.bloodnight.config.worldsettings;

import de.eldoria.bloodnight.core.manager.nightmanager.NightUtil;
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
import java.util.concurrent.ThreadLocalRandom;

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

    private NightDuration nightDurationMode = NightDuration.NORMAL;

    /**
     * The duration of a night when {@link #nightDurationMode} is set to {@link NightDuration#EXTENDED}.
     * <p>
     * The min duration of a night when when {@link #nightDurationMode} is set to {@link NightDuration#RANGE}.
     */
    private int nightDuration = 600;
    /**
     * The max duration of a night when {@link #nightDurationMode} is set to {@link NightDuration#RANGE}
     */
    private int maxNightDuration = 600;
    /**
     * The duration of the current night in ticks.
     */
    private transient int currentDuration = 0;

    public NightSettings() {
    }

    public NightSettings(Map<String, Object> objectMap) {
        TypeResolvingMap map = SerializationUtil.mapOf(objectMap);
        skippable = map.getValueOrDefault("skippable", skippable);
        nightBegin = map.getValueOrDefault("nightBegin", nightBegin);
        nightEnd = map.getValueOrDefault("nightEnd", nightEnd);
        startCommands = map.getValueOrDefault("startCommands", startCommands);
        endCommands = map.getValueOrDefault("endCommands", endCommands);
        if (objectMap.containsKey("overrideNightDuration")) {
            nightDurationMode = map.getValue("overrideNightDuration") ? NightDuration.EXTENDED : NightDuration.NORMAL;
        } else {
            nightDurationMode = map.getValueOrDefault("nightDurationMode", nightDurationMode, NightDuration.class);
        }
        nightDuration = map.getValueOrDefault("nightDuration", nightDuration);
        setMaxNightDuration(map.getValueOrDefault("maxNightDuration", maxNightDuration));
    }

    public void setMaxNightDuration(int maxNightDuration) {
        this.maxNightDuration = Math.max(nightDuration, maxNightDuration);
    }

    public void setNightDuration(int nightDuration) {
        this.nightDuration = nightDuration;
        this.maxNightDuration = Math.max(this.nightDuration, this.maxNightDuration);
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        return SerializationUtil.newBuilder()
                .add("skippable", skippable)
                .add("nightBegin", nightBegin)
                .add("nightEnd", nightEnd)
                .add("startCommands", startCommands)
                .add("endCommands", endCommands)
                .add("nightDurationMode", nightDurationMode.name())
                .add("nightDuration", nightDuration)
                .build();
    }

    public void regenerateNightDuration() {
        switch (nightDurationMode) {
            case NORMAL:
                currentDuration = (int) NightUtil.getDiff(nightBegin, nightEnd);
                break;
            case EXTENDED:
                currentDuration = nightDuration * 20;
                break;
            case RANGE:
                currentDuration = ThreadLocalRandom.current().nextInt(nightDuration, maxNightDuration + 1) * 20;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + nightDurationMode);
        }
    }

    public int getCurrentNightDuration() {
        return currentDuration;
    }

    public boolean isCustomNightDuration() {
        return nightDurationMode != NightDuration.NORMAL;
    }

    public enum NightDuration {
        NORMAL, EXTENDED, RANGE
    }
}
