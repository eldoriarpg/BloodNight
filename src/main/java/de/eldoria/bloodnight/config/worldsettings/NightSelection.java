package de.eldoria.bloodnight.config.worldsettings;

import de.eldoria.bloodnight.core.BloodNight;
import de.eldoria.eldoutilities.serialization.SerializationUtil;
import de.eldoria.eldoutilities.serialization.TypeResolvingMap;
import de.eldoria.eldoutilities.utils.EnumUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;
import sun.jvm.hotspot.ui.ObjectHistogramPanel;

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
     * Probability that a night becomes a blood night. In percent 0-100.
     */
    private int probability = 60;

    private Map<Integer, Integer> moonPhase = new HashMap<Integer, Integer>() {{
        put(0, 0);
        put(1, 10);
        put(2, 20);
        put(3, 40);
        put(4, 100);
        put(5, 40);
        put(6, 20);
        put(7, 10);
    }};

    private Map<Integer, Integer> phase = new HashMap<Integer, Integer>() {{
        put(0, 50);
        put(0, 50);
        put(0, 50);
    }};

    private int currPhase = 0;

    /**
     * Length of a period.
     */
    private int period = 10;
    /**
     * Current value of the curve.
     */
    private int currCurvePos =  0;
    /**
     * Min curve value.
     */
    private int minCurveVal = 20;
    /**
     * Max curve value.
     */
    private int maxCurveVal = 80;

    /**
     * Interval of days.
     */
    private int interval = 5;
    /**
     * Probability on interval day.
     */
    private int intervalProbability = 100;
    /**
     * Current interval
     */
    private int curInterval = 0;

    public NightSelection(Map<String, Object> objectMap) {
        TypeResolvingMap map = SerializationUtil.mapOf(objectMap);
        nightSelectionType = map.getValueOrDefault("nightSelectionType", nightSelectionType,
                o -> EnumUtil.parse(o, NightSelectionType.class));
        // probability
        probability = map.getValueOrDefault("probability", probability);
        // phases
        moonPhase = parsePhase(map.getValueOrDefault("phases",
                moonPhase.entrySet().stream().map(e -> e.getKey() + ":" + e.getValue()).collect(Collectors.toList())));
        phase = parsePhase(map.getValueOrDefault("customPhases",
                phase.entrySet().stream().map(e -> e.getKey() + ":" + e.getValue()).collect(Collectors.toList())));
        verifyPhases();
        currPhase = map.getValueOrDefault("currPhase", currPhase);
        period = map.getValueOrDefault("currPhase", period);
        // curve
        currCurvePos = map.getValueOrDefault("currCurvePos", currCurvePos);
        minCurveVal = map.getValueOrDefault("minCurveVal", minCurveVal);
        maxCurveVal = map.getValueOrDefault("maxCurveVal", maxCurveVal);
        // interval
        interval = map.getValueOrDefault("interval", interval);
        intervalProbability = map.getValueOrDefault("intervalProbability", intervalProbability);

        curInterval = map.getValueOrDefault("curInterval", curInterval);
    }

    public NightSelection() {
    }

    public void upcountInterval() {
        curInterval %= interval;
        curInterval++;
    }

    public void upcountPhase() {
        currPhase %= phase.size() - 1;
        currPhase++;
    }

    public int getPhaseProbability(int phase) {
        return moonPhase.getOrDefault(phase, -1);
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        List<String> phases = this.moonPhase.entrySet().stream().map(e -> e.getKey() + ":" + e.getValue()).collect(Collectors.toList());
        List<String> phasesCustom = this.moonPhase.entrySet().stream().map(e -> e.getKey() + ":" + e.getValue()).collect(Collectors.toList());

        return SerializationUtil.newBuilder()
                .add("probability", probability)
                .add("nightSelectionType", nightSelectionType.name())
                .add("phases", phases)
                .add("phasesCustom", phasesCustom)
                .add("currPhase", currPhase)
                .add("period", period)
                .add("currCurvePos", currCurvePos)
                .add("minCurveVal", minCurveVal)
                .add("maxCurveVal", maxCurveVal)
                .add("interval", interval)
                .add("curInterval", curInterval)
                .build();
    }

    public void setPhase(int phase, int probability) {
        this.phase.put(phase, probability);
        currPhase = Math.min(this.phase.size(), currPhase);
    }

    public void setMoonPhase(int phase, int probability) {
        moonPhase.put(phase, probability);
    }

    public void setPhaseCount(int phaseCount) {
        Map<Integer, Integer> newPhases = new HashMap<>();
        for (int i = 0; i < phaseCount; i++) {
            newPhases.put(i, phase.getOrDefault(i, 50));
        }
        phase = newPhases;
        currPhase = Math.min(this.phase.size(), currPhase);
    }

    private void verifyPhases() {
        Map<Integer, Integer> newPhases = new HashMap<>();
        for (int i = 0; i < phase.size(); i++) {
            newPhases.put(i, phase.getOrDefault(i, 50));
        }
        phase = newPhases;
        currPhase = Math.min(this.phase.size(), currPhase);
    }

    private Map<Integer, Integer> parsePhase(List<String> list){
        Map<Integer, Integer> map = new HashMap<>();
        for (String s : list) {
            String[] split = s.split(":");
            try {
                if (split.length == 1) {
                    map.put(Integer.parseInt(split[0]), 100);
                } else if (split.length == 2) {
                    map.put(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
                } else {
                    BloodNight.getInstance().getLogger().log(Level.WARNING, "Could not parse " + s + " to phase.");
                }
            } catch (NumberFormatException e) {
                BloodNight.getInstance().getLogger().log(Level.WARNING, "Could not parse " + s + " to phase.");
            }
        }
        return map;
    }

    public void setPeriod(int period) {
        this.period = period;
        currCurvePos = 0;
    }

    public enum NightSelectionType {
        /**
         * Determine bloodnight based on a random value.
         */
        RANDOM,
        /**
         * Determine bloodnight based on a random value attached to the ingame moon phase.
         */
        MOON_PHASE,
        /**
         * Determine bloodnight based on the real moon phase and a random value attached to the phase.
         */
        REAL_MOON_PHASE,
        /**
         * Determine bloodnight based on an interval with a random value.
         */
        INTERVAL,
        /**
         * Determine bloodnight based on a random value attached to a phase.
         */
        PHASE,
        /**
         * Determine bloodnight based on a smooth curve with a fixed length and a max and min probability.
         */
        CURVE
    }
}
