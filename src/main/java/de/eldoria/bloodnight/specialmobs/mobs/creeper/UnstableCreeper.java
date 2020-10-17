package de.eldoria.bloodnight.specialmobs.mobs.creeper;

import de.eldoria.bloodnight.specialmobs.SpecialMobUtil;
import org.bukkit.Particle;
import org.bukkit.entity.Creeper;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 * Unstable creeper explodes on damage. Can only be killed by critical attacks.
 */
public class UnstableCreeper extends AbstractCreeper {
    public UnstableCreeper(Creeper creeper) {
        super(creeper);
        setExplosionRadius(10);
        setPowered(true);
        setMaxFuseTicks(50);
    }

    @Override
    public void tick() {
        SpecialMobUtil.spawnParticlesAround(getBaseEntity().getLocation(), Particle.END_ROD, 2);
    }

    @Override
    public void onDamage(EntityDamageEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK || event.getCause() == EntityDamageEvent.DamageCause.PROJECTILE) {
            explode();
        }
    }
}
