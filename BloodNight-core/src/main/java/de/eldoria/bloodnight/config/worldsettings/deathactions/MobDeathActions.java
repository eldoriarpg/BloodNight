package de.eldoria.bloodnight.config.worldsettings.deathactions;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.Map;

@Getter
@Setter
@SerializableAs("bloodNightMobDeathActions")
public class MobDeathActions extends DeathActions {

	public MobDeathActions(Map<String, Object> objectMap) {
		super(objectMap);
	}

	public MobDeathActions() {
	}
}
