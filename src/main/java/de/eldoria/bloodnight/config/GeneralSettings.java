package de.eldoria.bloodnight.config;

import de.eldoria.eldoutilities.serialization.SerializationUtil;
import de.eldoria.eldoutilities.serialization.TypeResolvingMap;
import de.eldoria.eldoutilities.utils.EnumUtil;
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
    private String language;
    private BroadcastLevel broadcastLevel;
    private boolean joinWorldWarning;

    public GeneralSettings(Map<String, Object> objectMap) {
        TypeResolvingMap map = SerializationUtil.mapOf(objectMap);
        language = map.getValueOrDefault("language", "en_US");
        broadcastLevel = EnumUtil.parse(map.getValueOrDefault("broadcastLevel", "SERVER"), BroadcastLevel.class);
        joinWorldWarning = map.getValueOrDefault("joinWorldWarning", true);
    }

    public GeneralSettings() {
        language = "en_US";
        broadcastLevel = BroadcastLevel.SERVER;
        joinWorldWarning = true;
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        return SerializationUtil.newBuilder()
                .add("language", language)
                .add("broadcastLevel", broadcastLevel)
                .add("joinWorldWarning", joinWorldWarning)
                .build();
    }
}
