package de.eldoria.bloodnight.specialmobs.mobs.witch;

import de.eldoria.bloodnight.specialmobs.SpecialMob;
import de.eldoria.bloodnight.specialmobs.SpecialMobUtil;
import org.bukkit.Particle;
import org.bukkit.entity.Witch;

public abstract class AbstractWitch extends SpecialMob<Witch> {

    public AbstractWitch(Witch witch) {
        super(witch);
    }

    @Override
    public void onEnd() {
        SpecialMobUtil.spawnParticlesAround(getBaseEntity(), Particle.CAMPFIRE_COSY_SMOKE, 30);
    }
}
