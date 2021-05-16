package de.eldoria.bloodnight.specialmob.node.effect;

import de.eldoria.bloodnight.specialmobs.SpecialMobUtil;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffectType;

public class PotionEffect implements EffectNode {
    PotionEffectType type;
    int amplifier;
    boolean visible;

    @Override
    public void apply(LivingEntity entity) {
        SpecialMobUtil.addPotionEffect(entity, type, amplifier, visible);
    }
}