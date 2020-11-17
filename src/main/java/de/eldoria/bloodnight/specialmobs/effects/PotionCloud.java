package de.eldoria.bloodnight.specialmobs.effects;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.potion.PotionData;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PotionCloud extends ParticleCloud {

	public PotionCloud(AreaEffectCloud effectCloud) {
		super(effectCloud);
	}

	public static Builder builder(Entity targetEntity) {
		Location loc = targetEntity.getLocation();
		AreaEffectCloud entity = (AreaEffectCloud) loc.getWorld().spawnEntity(loc, EntityType.AREA_EFFECT_CLOUD);
		targetEntity.addPassenger(entity);
		return new PotionCloud.Builder(entity);
	}

	public static Builder builder(Location loc) {
		AreaEffectCloud entity = (AreaEffectCloud) loc.getWorld().spawnEntity(loc, EntityType.AREA_EFFECT_CLOUD);
		return new PotionCloud.Builder(entity);
	}

	public static class Builder extends ParticleCloud.Builder {

		public Builder(AreaEffectCloud entity) {
			super(entity);
			entity.setDuration(10 * 20);
		}

		public Builder setPotionType(PotionData potionType) {
			entity.setBasePotionData(potionType);
			return this;
		}

		/**
		 * Set duration
		 *
		 * @param duration duration in seconds
		 *
		 * @return builder with changed duration
		 */
		public Builder setDuration(int duration) {
			entity.setDuration(duration * 20);
			return this;
		}

		public Builder withReapplyDelay(int delay) {
			entity.setReapplicationDelay(delay);
			return this;
		}

		public Builder withDuractionDecreaseOnApply(int duration) {
			entity.setDurationOnUse(duration);
			return this;
		}

		public Builder withRadiusDecreaseOnApply(float radius) {
			entity.setRadiusOnUse(radius);
			return this;
		}

		public Builder setRadiusPerTick(float radius) {
			entity.setRadiusPerTick(radius);
			return this;
		}

		public Builder fromSource(@Nullable ProjectileSource source) {
			entity.setSource(source);
			return this;
		}

		@Override
		public Builder ofColor(Color color) {
			super.ofColor(color);
			return this;
		}

		@Override
		public Builder withRadius(float radius) {
			super.withRadius(radius);
			return this;
		}

		@Override
		public Builder withParticle(@NotNull Particle particle) {
			super.withParticle(particle);
			return this;
		}

		@Override
		public <T> Builder withParticle(@NotNull Particle particle, @Nullable T data) {
			super.withParticle(particle, data);
			return this;
		}

		@Override
		public PotionCloud build() {
			return new PotionCloud(entity);
		}
	}
}
