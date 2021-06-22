package de.eldoria.bloodnight.specialmobs.mobs.spider;

import de.eldoria.bloodnight.bloodmob.utils.BloodMobUtil;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.potion.PotionEffectType;

public class WitherSkeletonRider extends AbstractSpiderRider {
    public WitherSkeletonRider(Mob carrier) {
        super(carrier, BloodMobUtil.spawnAndMount(carrier, EntityType.WITHER_SKELETON));
    }

    @Override
    public void tick() {
        BloodMobUtil.addPotionEffect(getBaseEntity(), PotionEffectType.SPEED, 1, true);
    }
}
