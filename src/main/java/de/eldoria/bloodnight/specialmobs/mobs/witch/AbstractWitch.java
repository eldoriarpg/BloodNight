package de.eldoria.bloodnight.specialmobs.mobs.witch;

import de.eldoria.bloodnight.specialmobs.SpecialMob;
import de.eldoria.bloodnight.specialmobs.SpecialMobUtil;
import lombok.Getter;
import org.bukkit.Particle;
import org.bukkit.entity.Witch;

public abstract class AbstractWitch implements SpecialMob {
    @Getter
    private final Witch witch;

    public AbstractWitch(Witch witch) {
        this.witch = witch;
    }

    @Override
    public void onEnd() {
        SpecialMobUtil.spawnParticlesAround(witch, Particle.CAMPFIRE_COSY_SMOKE, 30);
        witch.remove();
    }
}
