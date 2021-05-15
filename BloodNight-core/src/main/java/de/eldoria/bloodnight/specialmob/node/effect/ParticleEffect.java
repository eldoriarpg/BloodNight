package de.eldoria.bloodnight.specialmob.node.effect;

import de.eldoria.bloodnight.specialmobs.SpecialMobUtil;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;

public class ParticleEffect implements Effect {
    Particle particle;
    int amount;
    @Override
    public void apply(LivingEntity entity) {
        SpecialMobUtil.spawnParticlesAround(entity.getLocation(), particle, amount);
    }
}