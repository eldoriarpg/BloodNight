package de.eldoria.bloodnight.specialmobs.mobs.skeleton;

import de.eldoria.bloodnight.bloodmob.utils.BloodMobUtil;
import org.bukkit.entity.Skeleton;
import org.bukkit.potion.PotionEffectType;

public class InvisibleSkeleton extends AbstractSkeleton {
    public InvisibleSkeleton(Skeleton skeleton) {
        super(skeleton);
    }

    @Override
    public void tick() {
        BloodMobUtil.addPotionEffect(getBaseEntity(), PotionEffectType.INVISIBILITY, 1, false);
    }
}
