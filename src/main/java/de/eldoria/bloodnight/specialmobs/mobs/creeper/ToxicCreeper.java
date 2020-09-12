package de.eldoria.bloodnight.specialmobs.mobs.creeper;

import de.eldoria.bloodnight.specialmobs.SpecialMobUtil;
import org.bukkit.Particle;
import org.bukkit.entity.Creeper;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ToxicCreeper extends AbstractCreeper {

    public ToxicCreeper(Creeper creeper) {
        super(creeper);
    }

    @Override
    public void tick() {
        SpecialMobUtil.spawnParticlesAround(getCreeper(), Particle.SNEEZE, 10);
    }

    @Override
    public void onSpawn() {
        setMaxFuseTicks(20);
    }


    @Override
    public void onExplosionEvent(EntityExplodeEvent event) {
        SpecialMobUtil.spawnPotionAt(event.getLocation(), new PotionEffect(PotionEffectType.POISON, 5, 2, true, true));
    }
}
