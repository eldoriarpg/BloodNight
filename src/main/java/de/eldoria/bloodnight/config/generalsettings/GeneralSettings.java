package de.eldoria.bloodnight.config.generalsettings;

import de.eldoria.bloodnight.core.BloodNight;
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
@SerializableAs("bloodNightGeneralSettings")
public class GeneralSettings implements ConfigurationSerializable {
	private String language = "en_US";
	private String prefix = "§4[BN]";
	private BroadcastLevel broadcastLevel = BroadcastLevel.SERVER;
	private BroadcastMethod broadcastMethod = BroadcastMethod.SUBTITLE;
	private BroadcastMethod messageMethod = BroadcastMethod.SUBTITLE;
	private int mobTick = 5;
	private boolean blindness = true;
	private boolean joinWorldWarning = true;
	private boolean updateReminder = true;
	private boolean autoUpdater = false;
	private boolean beeFix = false;
	private boolean spawnerDropSuppression = false;

	public GeneralSettings(Map<String, Object> objectMap) {
		TypeResolvingMap map = SerializationUtil.mapOf(objectMap);
		language = map.getValueOrDefault("language", language);
		prefix = map.getValueOrDefault("prefix", prefix.replace("&", "§"));
		broadcastLevel = map.getValueOrDefault("broadcastLevel", broadcastLevel, BroadcastLevel.class);
		broadcastMethod = map.getValueOrDefault("broadcastMethod", broadcastMethod, BroadcastMethod.class);
		messageMethod = map.getValueOrDefault("messageMethod", messageMethod, BroadcastMethod.class);
		mobTick = map.getValueOrDefault("mobTick", mobTick);
		joinWorldWarning = map.getValueOrDefault("joinWorldWarning", joinWorldWarning);
		blindness = map.getValueOrDefault("blindness", blindness);
		updateReminder = map.getValueOrDefault("updateReminder", updateReminder);
		autoUpdater = map.getValueOrDefault("autoUpdater", autoUpdater);
		beeFix = map.getValueOrDefault("beeFix", beeFix);
		spawnerDropSuppression = map.getValueOrDefault("spawnerDropSuppression", spawnerDropSuppression);
		if (beeFix) {
			BloodNight.logger().info("§4Bee Fix is enabled. This feature should be used with care.");
		}
	}

	public GeneralSettings() {
	}

	@Override
	public @NotNull Map<String, Object> serialize() {
		return SerializationUtil.newBuilder()
				.add("language", language)
				.add("broadcastLevel", broadcastLevel.name())
				.add("broadcastMethod", broadcastMethod.name())
				.add("messageMethod", messageMethod.name())
				.add("mobTick", mobTick)
				.add("joinWorldWarning", joinWorldWarning)
				.add("blindness", blindness)
				.add("updateReminder", updateReminder)
				.add("autoUpdater", autoUpdater)
				.add("beeFix", beeFix)
				.add("spawnerDropSuppression", spawnerDropSuppression)
				.build();
	}
}
