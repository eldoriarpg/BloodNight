package de.eldoria.bloodnight.specialmobs.mobs.witch;

import de.eldoria.bloodnight.specialmobs.SpecialMob;
import de.eldoria.bloodnight.specialmobs.SpecialMobUtil;
import org.bukkit.Particle;
import org.bukkit.entity.Witch;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public abstract class AbstractWitch extends SpecialMob<Witch> {
	private Instant lastShot = Instant.now();

	public AbstractWitch(Witch witch) {
		super(witch);
	}

	@Override
	public void onEnd() {
		SpecialMobUtil.spawnParticlesAround(getBaseEntity(), Particle.CAMPFIRE_COSY_SMOKE, 30);
	}

	protected void shot() {
		lastShot = Instant.now();
	}

	/**
	 * check if last shot is in the past more than the delay
	 *
	 * @param delay delay in seconds
	 *
	 * @return true if entity can shoot again
	 */
	protected boolean canShoot(int delay) {
		return lastShot.isBefore(Instant.now().minus(delay, ChronoUnit.SECONDS));
	}
}
