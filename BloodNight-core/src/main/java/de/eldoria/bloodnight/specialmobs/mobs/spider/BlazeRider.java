package de.eldoria.bloodnight.specialmobs.mobs.spider;

import de.eldoria.bloodnight.specialmobs.SpecialMobUtil;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;

public class BlazeRider extends AbstractSpiderRider {
	public BlazeRider(Mob carrier) {
		super(carrier, SpecialMobUtil.spawnAndMount(carrier, EntityType.BLAZE));
	}

}
