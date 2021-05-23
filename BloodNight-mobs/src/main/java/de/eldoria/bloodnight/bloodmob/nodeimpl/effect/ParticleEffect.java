package de.eldoria.bloodnight.bloodmob.nodeimpl.effect;

import de.eldoria.bloodnight.bloodmob.node.effect.EffectNode;
import de.eldoria.bloodnight.bloodmob.serialization.annotation.NumberProperty;
import de.eldoria.bloodnight.bloodmob.serialization.annotation.Property;
import de.eldoria.bloodnight.bloodmob.utils.BloodMobUtil;
import lombok.NoArgsConstructor;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@NoArgsConstructor
public class ParticleEffect implements EffectNode {
    @Property(name = "", descr = "")
    Particle particle;
    @NumberProperty(name = "", descr = "")
    int amount;

    public ParticleEffect(Particle particle, int amount) {
        this.particle = particle;
        this.amount = amount;
    }

    @Override
    public void apply(LivingEntity entity) {
        BloodMobUtil.spawnParticlesAround(entity.getLocation(), particle, amount);
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ParticleEffect that = (ParticleEffect) o;

        if (amount != that.amount) return false;
        return particle == that.particle;
    }

    @Override
    public int hashCode() {
        int result = particle != null ? particle.hashCode() : 0;
        result = 31 * result + amount;
        return result;
    }
}