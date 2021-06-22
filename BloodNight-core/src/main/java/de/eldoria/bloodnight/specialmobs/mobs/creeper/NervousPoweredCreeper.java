package de.eldoria.bloodnight.specialmobs.mobs.creeper;

import de.eldoria.bloodnight.bloodmob.utils.BloodMobUtil;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.Creeper;
import org.bukkit.potion.PotionEffectType;

public class NervousPoweredCreeper extends AbstractCreeper {

    public NervousPoweredCreeper(Creeper creeper) {
        super(creeper);
        setPowered(true);
        setMaxFuseTicks(1);
        BloodMobUtil.spawnParticlesAround(getBaseEntity().getLocation(), Particle.REDSTONE, new Particle.DustOptions(Color.RED, 5), 10);
    }

    @Override
    public void tick() {
        BloodMobUtil.addPotionEffect(getBaseEntity(), PotionEffectType.SPEED, 2, false);
    }

    @Override
    public void onEnd() {
        setMaxFuseTicks(0);
        ignite();
    }
}
