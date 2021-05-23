package de.eldoria.bloodnight.specialmobs.mobs.skeleton;

import de.eldoria.bloodnight.specialmobs.SpecialMob;
import de.eldoria.bloodnight.bloodmob.utils.BloodMobUtil;
import org.bukkit.Particle;
import org.bukkit.entity.Skeleton;

public abstract class AbstractSkeleton extends SpecialMob<Skeleton> {
    public AbstractSkeleton(Skeleton skeleton) {
        super(skeleton);
    }

    @Override
    public void onEnd() {
        BloodMobUtil.spawnParticlesAround(getBaseEntity(), Particle.CAMPFIRE_COSY_SMOKE, 30);
    }
}
