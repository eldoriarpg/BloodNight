package de.eldoria.bloodnight.specialmobs.mobs.spider;

import de.eldoria.bloodnight.specialmobs.SpecialMobUtil;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.potion.PotionEffectType;

public class SpeedSkeletonRider extends AbstractSpiderRider {
    public SpeedSkeletonRider(Mob carrier) {
        super(carrier, SpecialMobUtil.spawnAndMount(carrier, EntityType.SKELETON));
    }

    @Override
    public void tick() {
        SpecialMobUtil.addPotionEffect(getBaseEntity(), PotionEffectType.SPEED, 1, true);
    }
}
