package de.eldoria.bloodnight.specialmobs.mobs.rider;

import de.eldoria.bloodnight.specialmobs.SpecialMobUtil;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.potion.PotionEffectType;

public class WitherSkeletonRider extends AbstractRider {
    public WitherSkeletonRider(Mob carrier) {
        super(carrier, SpecialMobUtil.spawnAndMount(carrier, EntityType.WITHER_SKELETON));
    }

    @Override
    public void tick() {
        SpecialMobUtil.addPotionEffect(getBaseEntity(), PotionEffectType.SPEED, 2, true);
    }
}
