package de.eldoria.bloodnight.specialmobs.mobs.skeleton;

import de.eldoria.bloodnight.specialmobs.SpecialMob;
import de.eldoria.bloodnight.specialmobs.SpecialMobUtil;
import lombok.Getter;
import org.bukkit.Particle;
import org.bukkit.entity.Skeleton;

public abstract class AbstractSkeleton implements SpecialMob {
    @Getter
    private final Skeleton skeleton;

    public AbstractSkeleton(Skeleton skeleton) {
        this.skeleton = skeleton;
    }

    @Override
    public void onEnd() {
        SpecialMobUtil.spawnParticlesAround(skeleton, Particle.CAMPFIRE_COSY_SMOKE, 30);
        skeleton.remove();
    }
}
