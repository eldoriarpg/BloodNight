package de.eldoria.bloodnight.config.worldsettings.deathactions;

import de.eldoria.eldoutilities.serialization.SerializationUtil;
import de.eldoria.eldoutilities.serialization.TypeResolvingMap;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@SerializableAs("bloodNightPlayerDeathActions")
public class PlayerDeathActions extends DeathActions {
	/**
	 * Commands which will be executed when a player dies.
	 */
	private List<String> deathCommands = new ArrayList<>();

	/**
	 * Probability of the player to lose and not drop its inventory.
	 */
	private int loseInventoryProbability = 100;

	private List<PotionEffectType> respawnEffects = new ArrayList<PotionEffectType>() {{
		add(PotionEffectType.WEAKNESS);
	}};

	private int effectDuration = 10;

	public PlayerDeathActions(Map<String, Object> objectMap) {
		super(objectMap);
		TypeResolvingMap map = SerializationUtil.mapOf(objectMap);
		deathCommands = map.getValueOrDefault("deathCommands", deathCommands);
		loseInventoryProbability = map.getValueOrDefault("loseInventoryProbability", loseInventoryProbability);
		respawnEffects = map.getValueOrDefault("respawnEffects", respawnEffects);
		effectDuration = map.getValueOrDefault("effectDuration", effectDuration);
	}

	public PlayerDeathActions() {
	}

	@Override
	public @NotNull Map<String, Object> serialize() {
		return SerializationUtil.newBuilder(super.serialize())
				.add("deathCommands", deathCommands)
				.add("loseInventoryProbability", loseInventoryProbability)
				.add("respawnEffects", respawnEffects)
				.add("effectDuration", effectDuration)
				.build();
	}
}
