package de.eldoria.bloodnight.specialmobs.mobs.skeleton;

import de.eldoria.bloodnight.specialmobs.SpecialMobUtil;
import org.bukkit.entity.Skeleton;
import org.bukkit.potion.PotionEffectType;

public class InvisibleSkeleton extends AbstractSkeleton {
    public InvisibleSkeleton(Skeleton skeleton) {
        super(skeleton);
    }

    @Override
    public void tick() {
        SpecialMobUtil.addPotionEffect(getSkeleton(), PotionEffectType.INVISIBILITY, 1, false);
    }
}
