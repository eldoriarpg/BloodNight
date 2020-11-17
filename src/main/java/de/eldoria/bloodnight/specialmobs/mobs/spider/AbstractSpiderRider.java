package de.eldoria.bloodnight.specialmobs.mobs.spider;

import de.eldoria.bloodnight.specialmobs.SpecialMobUtil;
import de.eldoria.bloodnight.specialmobs.StatSource;
import de.eldoria.bloodnight.specialmobs.mobs.ExtendedSpecialMob;
import org.bukkit.Particle;
import org.bukkit.entity.Mob;
import org.bukkit.event.entity.EntityDamageEvent;

public abstract class AbstractSpiderRider extends ExtendedSpecialMob<Mob, Mob> {

	public AbstractSpiderRider(Mob carrier, Mob passenger) {
		super(carrier, passenger, StatSource.CARRIER);
	}

	@Override
	public void tick() {
		if (getBaseEntity().isDead() || !getBaseEntity().isValid()) {
			remove();
		}
	}

	@Override
	public void onExtensionDamage(EntityDamageEvent event) {
		if (event.getCause() == EntityDamageEvent.DamageCause.SUFFOCATION) {
			event.setCancelled(true);
			return;
		}
		super.onExtensionDamage(event);
	}

	@Override
	public void onEnd() {
		SpecialMobUtil.spawnParticlesAround(getBaseEntity(), Particle.CAMPFIRE_COSY_SMOKE, 30);
	}
}
