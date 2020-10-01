package de.eldoria.bloodnight.specialmobs.mobs.creeper;

import de.eldoria.bloodnight.specialmobs.SpecialMobUtil;
import de.eldoria.bloodnight.specialmobs.effects.ParticleCloud;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.Creeper;
import org.bukkit.potion.PotionEffectType;

public class NervousPoweredCreeper extends AbstractCreeper {
    private final ParticleCloud cloud;

    public NervousPoweredCreeper(Creeper creeper) {
        super(creeper);
        setPowered(true);
        setMaxFuseTicks(0);
        cloud = ParticleCloud.builder(creeper).ofColor(Color.RED).withParticle(Particle.REDSTONE, new Particle.DustOptions(Color.RED, 1)).build();
    }

    @Override
    public void tick() {
        SpecialMobUtil.addPotionEffect(getCreeper(), PotionEffectType.SPEED, 2, false);
        cloud.tick();
    }

    @Override
    public void onEnd() {
        setMaxFuseTicks(0);
        ignite();
    }
}
