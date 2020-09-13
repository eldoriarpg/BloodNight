package de.eldoria.bloodnight.specialmobs.mobs.phantom;

import de.eldoria.bloodnight.specialmobs.SpecialMob;
import de.eldoria.bloodnight.specialmobs.SpecialMobUtil;
import lombok.Getter;
import org.bukkit.Particle;
import org.bukkit.entity.Phantom;

public abstract class AbstractPhantom implements SpecialMob {
    @Getter
    private final Phantom phantom;

    protected AbstractPhantom(Phantom phantom) {
        this.phantom = phantom;
    }

    @Override
    public void onEnd() {
        SpecialMobUtil.spawnParticlesAround(phantom, Particle.CAMPFIRE_COSY_SMOKE, 30);
        phantom.remove();
    }
}
