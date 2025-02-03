package de.eldoria.bloodnight.config.worldsettings;

import de.eldoria.bloodnight.core.BloodNight;
import de.eldoria.bloodnight.core.manager.nightmanager.util.NightUtil;
import de.eldoria.bloodnight.util.MoonPhase;
import de.eldoria.eldoutilities.container.Pair;
import de.eldoria.eldoutilities.serialization.SerializationUtil;
import de.eldoria.eldoutilities.serialization.TypeResolvingMap;
import de.eldoria.eldoutilities.utils.EMath;
import de.eldoria.eldoutilities.utils.EnumUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
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

    private Map<Integer, Integer> moonPhase = new HashMap<>() {{
        put(0, 0);
        put(1, 10);
        put(2, 20);
        put(3, 40);
        put(4, 100);
        put(5, 40);
        put(6, 20);
        put(7, 10);
    }};

    private Map<Integer, Integer> phaseCustom = new HashMap<>() {{
        put(0, 50);
        put(1, 50);
        put(2, 50);
    }};

    private int currPhase;

    /**
     * Length of a period.
     */
    private int period = 10;
    /**
     * Current value of the curve.
     */
    private int currCurvePos;
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
    private int curInterval;

    public NightSelection(Map<String, Object> objectMap) {
        TypeResolvingMap map = SerializationUtil.mapOf(objectMap);
        nightSelectionType = map.getValueOrDefault("nightSelectionType", nightSelectionType,
                o -> EnumUtil.parse(o, NightSelectionType.class).get());
        // probability
        probability = map.getValueOrDefault("probability", probability);
        // phases
        moonPhase = parsePhase(map.getValueOrDefault("phases",
                moonPhase.entrySet().stream().map(e -> e.getKey() + ":" + e.getValue()).collect(Collectors.toList())));
        phaseCustom = parsePhase(map.getValueOrDefault("phasesCustom",
                phaseCustom.entrySet().stream().map(e -> e.getKey() + ":" + e.getValue()).collect(Collectors.toList())));
        verifyPhases();
        currPhase = map.getValueOrDefault("currPhase", currPhase);
        period = map.getValueOrDefault("period", period);
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
        curInterval++;
        curInterval %= interval;
    }

    public void upcountPhase() {
        currPhase++;
        currPhase %= phaseCustom.size();
    }

    public void upcountCurve() {
        currCurvePos++;
        currCurvePos %= period;
    }

    public int getPhaseProbability(int phase) {
        return moonPhase.getOrDefault(phase, -1);
    }

    @Override
    @NotNull
    public Map<String, Object> serialize() {
        List<String> phases = this.moonPhase.entrySet().stream().map(e -> e.getKey() + ":" + e.getValue()).collect(Collectors.toList());
        List<String> phasesCustom = this.phaseCustom.entrySet().stream().map(e -> e.getKey() + ":" + e.getValue()).collect(Collectors.toList());

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
                .add("intervalProbability", intervalProbability)
                .add("curInterval", curInterval)
                .build();
    }

    public void setPhaseCustom(int phase, int probability) {
        this.phaseCustom.put(phase, probability);
        currPhase = Math.min(this.phaseCustom.size(), currPhase);
    }

    public void setMoonPhase(int phase, int probability) {
        moonPhase.put(phase, probability);
    }

    public void setPhaseCount(int phaseCount) {
        Map<Integer, Integer> newPhases = new HashMap<>();
        for (int i = 0; i < phaseCount; i++) {
            newPhases.put(i, phaseCustom.getOrDefault(i, 50));
        }
        phaseCustom = newPhases;
        currPhase = Math.min(this.phaseCustom.size(), currPhase);
    }

    private void verifyPhases() {
        Map<Integer, Integer> newPhases = new HashMap<>();
        for (int i = 0; i < Math.min(phaseCustom.size(), 54); i++) {
            newPhases.put(i, phaseCustom.getOrDefault(i, 50));
        }
        phaseCustom = newPhases;
        currPhase = Math.min(this.phaseCustom.size(), currPhase);
    }

    private Map<Integer, Integer> parsePhase(List<String> list) {
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

    public int getCurrentProbability(World world) {
        return getNextProbability(world, 0);
    }

    public int getNextProbability(World world, int nightOffset) {
        switch (nightSelectionType) {
            case RANDOM:
                return probability;
            case MOON_PHASE:
                int moonPhase = NightUtil.getMoonPhase(world);
                if (!getMoonPhase().containsKey(moonPhase)) return 0;
                return getPhaseProbability((moonPhase + nightOffset) % 8);
            case REAL_MOON_PHASE:
                Calendar cal = Calendar.getInstance();
                cal.setTime(Date.from(Instant.now()));
                // Get moon phase based on Server time. Convert to minecraft moon phase.
                int realMoonPhase = (MoonPhase.computePhaseIndex(cal) + 4) % 8;
                if (!getMoonPhase().containsKey(realMoonPhase)) return 0;
                return getPhaseProbability(realMoonPhase);
            case INTERVAL:
                if ((getCurInterval() + nightOffset) % getInterval() != 0) {
                    return 0;
                }
                return getIntervalProbability();
            case PHASE:
                getPhaseCustom().get((getCurrPhase() + nightOffset) % phaseCustom.size());
                break;
            case CURVE:
                double curveProb;
                int pos = (getCurrCurvePos() + nightOffset) % period;
                // First half. Increasing curve.
                if (pos <= getPeriod() / 2) {
                    curveProb = EMath.smoothCurveValue(Pair.of(0d, (double) getMinCurveVal()),
                            Pair.of((double) getPeriod() / 2,
                                    (double) getMaxCurveVal()), pos);
                } else {
                    curveProb = EMath.smoothCurveValue(Pair.of((double) getPeriod() / 2, (double) getMaxCurveVal()),
                            Pair.of((double) getPeriod(),
                                    (double) getMinCurveVal()), pos);
                }
                return (int) curveProb;
        }
        return 0;
    }

    public void upcount() {
        switch (nightSelectionType) {
            case RANDOM:
                break;
            case MOON_PHASE:
                break;
            case REAL_MOON_PHASE:
                break;
            case INTERVAL:
                upcountInterval();
                break;
            case PHASE:
                upcountPhase();
                break;
            case CURVE:
                upcountCurve();
                break;
        }
    }

    @Override
    public String toString() {
        String phases;
        switch (nightSelectionType) {
            case RANDOM:
                return String.format("Mode: %s | Prob: %s", nightSelectionType, probability);
            case MOON_PHASE:
            case REAL_MOON_PHASE:
                phases = getMoonPhase().entrySet().stream()
                        .map(e -> e.getKey() + ":" + e.getValue())
                        .collect(Collectors.joining(" | "));
                return String.format("Mode: %s | Phases: %s", nightSelectionType, phases);
            case INTERVAL:
                return String.format("Mode: %s | Interval %d of %d with Prob %d", nightSelectionType, curInterval, interval - 1, intervalProbability);
            case PHASE:
                phases = getPhaseCustom().entrySet().stream()
                        .map(e -> e.getKey() + ":" + e.getValue())
                        .collect(Collectors.joining(" | "));
                return String.format("Mode: %s | Phases: %s", nightSelectionType, phases);
            case CURVE:
                return String.format("Mode: %s | Pos %d on curve between %d and %d", nightSelectionType, currCurvePos, minCurveVal, maxCurveVal);
        }
        return "NightSelection{}";
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
