package de.eldoria.bloodnight.specialmob.node.effect;

import de.eldoria.bloodnight.specialmobs.SpecialMobUtil;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;

public class DustParticleEffect implements EffectNode {
    Color color;
    int amount;
    int size;

    @Override
    public void apply(LivingEntity entity) {
        SpecialMobUtil.spawnParticlesAround(entity.getLocation(), Particle.REDSTONE,
                new Particle.DustOptions(color, size), amount);
    }
}
