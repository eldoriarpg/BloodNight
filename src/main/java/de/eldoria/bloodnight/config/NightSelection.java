package de.eldoria.bloodnight.config;

import de.eldoria.bloodnight.core.BloodNight;
import de.eldoria.eldoutilities.serialization.SerializationUtil;
import de.eldoria.eldoutilities.serialization.TypeResolvingMap;
import de.eldoria.eldoutilities.utils.EnumUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Getter
@Setter
@SerializableAs("bloodNightNightSelection")
public class NightSelection implements ConfigurationSerializable {

    private NightSelectionType nightSelectionType = NightSelectionType.RANDOM;

    /**
     * Probability that a night becomes a blood night.
     * In percent 0-100.
     */
    private int probability = 20;


    private Map<Integer, Integer> phases = new HashMap<Integer, Integer>() {{
        put(0, 0);
        put(1, 10);
        put(2, 20);
        put(3, 40);
        put(4, 100);
        put(5, 40);
        put(6, 20);
        put(7, 10);
    }};

    public NightSelection(Map<String, Object> objectMap) {
        TypeResolvingMap map = SerializationUtil.mapOf(objectMap);
        probability = map.getValue("probability");
        nightSelectionType = map.getValue("nightSelectionType", o -> EnumUtil.parse(o, NightSelectionType.class));
        List<String> list = map.getValue("phases");
        for (String s : list) {
            String[] split = s.split(":");
            try {
                if (split.length == 1) {
                    phases.put(Integer.parseInt(split[0]), 100);
                } else if (split.length == 2) {
                    phases.put(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
                } else {
                    BloodNight.getInstance().getLogger().log(Level.WARNING, "Could not parse " + s + " to moon phase.");
                }
            } catch (NumberFormatException e) {
                BloodNight.getInstance().getLogger().log(Level.WARNING, "Could not parse " + s + " to moon phase.");
            }
        }
    }

    public NightSelection() {
    }

    public int getPhaseProbability(int phase) {
        return phases.getOrDefault(phase, -1);
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        List<String> phases = this.phases.entrySet().stream().map(e -> e.getKey() + ":" + e.getValue()).collect(Collectors.toList());

        return SerializationUtil.newBuilder()
                .add("probability", probability)
                .add("nightSelectionType", nightSelectionType.name())
                .add("phases", phases)
                .build();
    }

    public enum NightSelectionType {
        RANDOM, MOON_PHASE
    }
}
