package de.eldoria.bloodnight.specialmobs.mobs.creeper;

import de.eldoria.bloodnight.specialmobs.SpecialMobUtil;
import org.bukkit.Particle;
import org.bukkit.entity.Creeper;
import org.bukkit.potion.PotionEffectType;

public class NervousPoweredCreeper extends AbstractCreeper {
    public NervousPoweredCreeper(Creeper creeper) {
        super(creeper);
        setPowered(true);
        setMaxFuseTicks(0);
    }

    @Override
    public void tick() {
        SpecialMobUtil.addPotionEffect(getCreeper(), PotionEffectType.SPEED, 2, false);
        SpecialMobUtil.spawnParticlesAround(getCreeper(), Particle.REDSTONE, 10);
    }

    @Override
    public void onEnd() {
        setMaxFuseTicks(0);
        ignite();
    }
}
