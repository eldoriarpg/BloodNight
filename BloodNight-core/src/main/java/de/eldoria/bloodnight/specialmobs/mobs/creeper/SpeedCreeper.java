package de.eldoria.bloodnight.specialmobs.mobs.creeper;

import de.eldoria.bloodnight.bloodmob.utils.BloodMobUtil;
import org.bukkit.entity.Creeper;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.potion.PotionEffectType;

public class SpeedCreeper extends AbstractCreeper {
    public SpeedCreeper(Creeper creeper) {
        super(creeper);
        setMaxFuseTicks(10);
    }

    @Override
    public void tick() {
        BloodMobUtil.addPotionEffect(getBaseEntity(), PotionEffectType.SPEED, 4, true);
    }

    @Override
    public void onEnd() {
        setMaxFuseTicks(0);
        ignite();
    }

    @Override
    public void onExplosionPrimeEvent(ExplosionPrimeEvent event) {
        event.setFire(true);
    }
}
