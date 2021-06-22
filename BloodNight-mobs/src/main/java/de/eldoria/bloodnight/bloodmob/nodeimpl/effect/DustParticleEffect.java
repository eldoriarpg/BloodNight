package de.eldoria.bloodnight.bloodmob.nodeimpl.effect;

import de.eldoria.bloodnight.bloodmob.node.effect.EffectNode;
import de.eldoria.bloodnight.bloodmob.serialization.annotation.NumberProperty;
import de.eldoria.bloodnight.bloodmob.serialization.annotation.NumericProperty;
import de.eldoria.bloodnight.bloodmob.serialization.annotation.Property;
import de.eldoria.bloodnight.bloodmob.utils.BloodMobUtil;
import lombok.NoArgsConstructor;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@NoArgsConstructor
public class DustParticleEffect implements EffectNode {
    @Property(name = "", descr = "")
    Color color;
    @NumberProperty(name = "", descr = "", max = 64)
    int amount;
    @NumericProperty(name = "", descr = "", max = 5)
    float size;

    public DustParticleEffect(Color color, int amount, float size) {
        this.color = color;
        this.amount = amount;
        this.size = size;
    }

    @Override
    public void apply(LivingEntity entity) {
        BloodMobUtil.spawnParticlesAround(entity.getLocation(), Particle.REDSTONE,
                new Particle.DustOptions(color, size), amount);
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

        DustParticleEffect that = (DustParticleEffect) o;

        if (amount != that.amount) return false;
        if (Float.compare(that.size, size) != 0) return false;
        return color != null ? color.equals(that.color) : that.color == null;
    }

    @Override
    public int hashCode() {
        int result = color != null ? color.hashCode() : 0;
        result = 31 * result + amount;
        result = 31 * result + (size != +0.0f ? Float.floatToIntBits(size) : 0);
        return result;
    }
}
