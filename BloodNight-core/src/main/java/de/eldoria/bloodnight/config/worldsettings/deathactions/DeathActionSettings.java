package de.eldoria.bloodnight.config.worldsettings.deathactions;

import de.eldoria.eldoutilities.serialization.SerializationUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@Getter
@Setter
@SerializableAs("bloodNightDeathActionSettings")
public class DeathActionSettings implements ConfigurationSerializable {
	private MobDeathActions mobDeathActions = new MobDeathActions();
	private PlayerDeathActions playerDeathActions = new PlayerDeathActions();

	@Override
	public @NotNull Map<String, Object> serialize() {
		return SerializationUtil.newBuilder()
				.add("mobDeathActions", mobDeathActions)
				.add("playerDeathActions", playerDeathActions)
				.build();
	}
}
