package de.eldoria.bloodnight.bloodmob.nodeimpl.effect;

import de.eldoria.bloodnight.bloodmob.node.effect.EffectNode;
import de.eldoria.bloodnight.bloodmob.utils.BloodMobUtil;
import lombok.NoArgsConstructor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@NoArgsConstructor
public class PotionEffect implements EffectNode {
    PotionEffectType type;
    int amplifier;
    boolean visible;

    public PotionEffect(PotionEffectType type, int amplifier, boolean visible) {
        this.type = type;
        this.amplifier = amplifier;
        this.visible = visible;
    }

    @Override
    public void apply(LivingEntity entity) {
        BloodMobUtil.addPotionEffect(entity, type, amplifier, visible);
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

        PotionEffect that = (PotionEffect) o;

        if (amplifier != that.amplifier) return false;
        if (visible != that.visible) return false;
        return type != null ? type.equals(that.type) : that.type == null;
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + amplifier;
        result = 31 * result + (visible ? 1 : 0);
        return result;
    }
}