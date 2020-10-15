package de.eldoria.bloodnight.specialmobs.mobs.rider;

import de.eldoria.bloodnight.specialmobs.SpecialMobUtil;
import de.eldoria.bloodnight.specialmobs.mobs.ExtendedSpecialMob;
import org.bukkit.Particle;
import org.bukkit.entity.Mob;

public abstract class AbstractRider extends ExtendedSpecialMob<Mob, Mob> {

    public AbstractRider(Mob carrier, Mob passenger) {
        super(carrier, passenger);
    }

    @Override
    public void tick() {
        if (getBaseEntity().isDead() || !getBaseEntity().isValid()) {
            remove();
        }
    }

    @Override
    public void onEnd() {
        SpecialMobUtil.spawnParticlesAround(getBaseEntity(), Particle.CAMPFIRE_COSY_SMOKE, 30);
    }
}
