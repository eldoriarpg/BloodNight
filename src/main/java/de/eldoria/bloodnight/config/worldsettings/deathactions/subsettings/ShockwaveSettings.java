package de.eldoria.bloodnight.config.worldsettings.deathactions.subsettings;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

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

	protected List<PotionEffectType> shockwaveEffects = new ArrayList<PotionEffectType>() {{
		add(PotionEffectType.CONFUSION);
	}};

	/**
	 * min duration of effects when on the edge of range
	 */
	protected int minDuration = 1;

	/**
	 * Max duration of effects when in the center of shockwave.
	 */
	protected int maxDuration = 10;

	public ShockwaveSettings(Map<String, Object> objectMap) {
		TypeResolvingMap map = SerializationUtil.mapOf(objectMap);
		shockwaveProbability = map.getValueOrDefault("shockwaveProbability", shockwaveProbability);
		shockwavePower = map.getValueOrDefault("shockwavePower", shockwavePower);
		shockwaveRange = map.getValueOrDefault("shockwaveRange", shockwaveRange);
		shockwaveEffects = map.getValueOrDefault("shockwaveEffects", shockwaveEffects);
		minDuration = map.getValueOrDefault("minDuration", minDuration);
		maxDuration = map.getValueOrDefault("maxDuration", maxDuration);
	}

	public ShockwaveSettings() {
	}

	@Override
	public @NotNull Map<String, Object> serialize() {
		return SerializationUtil.newBuilder()
				.add("shockwaveProbability", shockwaveProbability)
				.add("shockwavePower", shockwavePower)
				.add("shockwaveRange", shockwaveRange)
				.add("shockwaveEffect", shockwaveEffects)
				.add("minDuration", minDuration)
				.add("maxDuration", maxDuration)
				.build();
	}

	public double getPower(Vector vector) {
		double range = Math.pow(shockwaveRange, 2);
		double dist = vector.length();
		if (dist >= range) return 0;
		return (1 - range / dist) * shockwavePower;
	}

	public void applyEffects(Entity entity) {
		if (!(entity instanceof LivingEntity)) return;
		LivingEntity livingEntity = (LivingEntity) entity;
		int duration = ThreadLocalRandom.current().nextInt(minDuration, maxDuration) * 20;
		for (PotionEffectType potionEffectType : shockwaveEffects) {
			livingEntity.addPotionEffect(new PotionEffect(potionEffectType, duration, 1));
		}
	}
}
