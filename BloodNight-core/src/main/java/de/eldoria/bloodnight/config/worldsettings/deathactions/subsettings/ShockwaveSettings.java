package de.eldoria.bloodnight.config.worldsettings.deathactions.subsettings;

import de.eldoria.bloodnight.config.worldsettings.deathactions.PotionEffectSettings;
import de.eldoria.eldoutilities.serialization.SerializationUtil;
import de.eldoria.eldoutilities.serialization.TypeResolvingMap;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@SerializableAs("bloodNightShockwaveSettings")
public class ShockwaveSettings implements ConfigurationSerializable {
    /**
     * Probability that a shockwave is spawned at the death location.
     * <p>
     * A Shockwave will push every entity away from the death position.
     */
    protected int shockwaveProbability = 10;

    /**
     * Power of shockwave. used to multiply the velocity vector.
     * <p>
     * Power will be less depending on the distance to shockwave center.
     */
    protected int shockwavePower = 10;

    /**
     * Range where player should be affected by shockwave.
     */
    protected int shockwaveRange = 10;
    /**
     * min duration of effects when on the edge of range
     */
    protected double minDuration = 0.1;
    private Map<PotionEffectType, PotionEffectSettings> shockwaveEffects = new HashMap<>() {{
        put(PotionEffectType.CONFUSION, new PotionEffectSettings(PotionEffectType.CONFUSION, 5));
    }};

    public ShockwaveSettings(Map<String, Object> objectMap) {
        TypeResolvingMap map = SerializationUtil.mapOf(objectMap);
        shockwaveProbability = map.getValueOrDefault("shockwaveProbability", shockwaveProbability);
        shockwavePower = map.getValueOrDefault("shockwavePower", shockwavePower);
        shockwaveRange = map.getValueOrDefault("shockwaveRange", shockwaveRange);
        shockwaveEffects = map.getMap("shockwaveEffects", (key, potionEffectSettings) -> PotionEffectType.getByName(key));
        minDuration = map.getValueOrDefault("minDuration", minDuration);
    }

    public ShockwaveSettings() {
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        return SerializationUtil.newBuilder()
                .add("shockwaveProbability", shockwaveProbability)
                .add("shockwavePower", shockwavePower)
                .add("shockwaveRange", shockwaveRange)
                .addMap("shockwaveEffects", shockwaveEffects,
                        (potionEffectType, potionEffectSettings) -> potionEffectType.getName())
                .add("minDuration", minDuration)
                .build();
    }

    public double getPower(Vector vector) {
        double range = Math.pow(shockwaveRange, 2);
        double dist = vector.lengthSquared();
        if (dist >= range) return 0;
        return (1 - dist / range) * (shockwavePower / 10d);
    }

    public void applyEffects(Entity entity, double power) {
        if (!(entity instanceof LivingEntity livingEntity)) return;
        for (PotionEffectSettings potionEffectType : shockwaveEffects.values()) {
            if (potionEffectType.getEffectType() == null) continue;
            double percent = power / (shockwavePower / 10d);
            double duration = Math.max(minDuration, potionEffectType.getDuration() * percent) * 20;
            livingEntity.addPotionEffect(new PotionEffect(potionEffectType.getEffectType(),
                    (int) duration, 1, false, true));
        }
    }
}
