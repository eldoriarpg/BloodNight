package de.eldoria.bloodnight.config.worldsettings.deathactions;

import de.eldoria.bloodnight.config.worldsettings.deathactions.subsettings.LightningSettings;
import de.eldoria.bloodnight.config.worldsettings.deathactions.subsettings.ShockwaveSettings;
import de.eldoria.eldoutilities.serialization.SerializationUtil;
import de.eldoria.eldoutilities.serialization.TypeResolvingMap;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@Getter
@Setter
@SerializableAs("bloodNightDeathActions")
public class DeathActions implements ConfigurationSerializable {
	protected LightningSettings lightningSettings = new LightningSettings();

	protected ShockwaveSettings shockwaveSettings = new ShockwaveSettings();

	public DeathActions(Map<String, Object> objectMap) {
		TypeResolvingMap map = SerializationUtil.mapOf(objectMap);
		lightningSettings = map.getValueOrDefault("lightningSettings", lightningSettings);
		shockwaveSettings = map.getValueOrDefault("shockwaveSettings", shockwaveSettings);
	}

	public DeathActions() {
	}

	@Override
	@NotNull
	public Map<String, Object> serialize() {
		return SerializationUtil.newBuilder()
				.add("lightningSettings", lightningSettings)
				.add("shockwaveSettings", shockwaveSettings)
				.build();
	}
}
