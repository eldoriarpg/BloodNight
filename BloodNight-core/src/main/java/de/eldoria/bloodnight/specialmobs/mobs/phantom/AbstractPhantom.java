package de.eldoria.bloodnight.specialmobs.mobs.phantom;

import de.eldoria.bloodnight.specialmobs.SpecialMob;
import de.eldoria.bloodnight.specialmobs.SpecialMobUtil;
import org.bukkit.Particle;
import org.bukkit.entity.Phantom;

public abstract class AbstractPhantom extends SpecialMob<Phantom> {

	protected AbstractPhantom(Phantom phantom) {
		super(phantom);
	}

	@Override
	public void onEnd() {
		SpecialMobUtil.spawnParticlesAround(getBaseEntity(), Particle.CAMPFIRE_COSY_SMOKE, 30);
	}
}
