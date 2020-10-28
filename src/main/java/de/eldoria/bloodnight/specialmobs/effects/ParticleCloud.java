package de.eldoria.bloodnight.specialmobs.effects;

import lombok.Getter;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public class ParticleCloud {
    private final AreaEffectCloud effectCloud;

    public ParticleCloud(AreaEffectCloud effectCloud) {
        this.effectCloud = effectCloud;
    }

    public static Builder builder(Entity targetEntity) {
        Location loc = targetEntity.getLocation();
        AreaEffectCloud entity = (AreaEffectCloud) loc.getWorld().spawnEntity(loc, EntityType.AREA_EFFECT_CLOUD);
        targetEntity.addPassenger(entity);
        return new Builder(entity);
    }

    public void tick() {
        effectCloud.setDuration(60 * 20);
    }

    public static class Builder {

        protected final AreaEffectCloud entity;

        public Builder(AreaEffectCloud entity) {
            this.entity = entity;
            entity.setRadiusPerTick(0);
            entity.setRadiusOnUse(0);
            entity.setDurationOnUse(0);
            entity.setDuration(60 * 20);
            entity.setColor(Color.WHITE);
            entity.setParticle(Particle.SPELL);
            entity.setReapplicationDelay(20);
            entity.setDurationOnUse(0);
            entity.setRadiusOnUse(0);
            entity.setRadius(3);
        }

        public Builder ofColor(Color color) {
            entity.setColor(color);
            return this;
        }

        public Builder withRadius(float radius) {
            entity.setRadius(radius);
            return this;
        }

        public Builder withParticle(@NotNull Particle particle) {
            entity.setParticle(particle);
            return this;
        }

        public <T> Builder withParticle(@NotNull Particle particle, @Nullable T data) {
            entity.setParticle(particle, data);
            return this;
        }


        public ParticleCloud build() {
            return new ParticleCloud(entity);
        }
    }
}
