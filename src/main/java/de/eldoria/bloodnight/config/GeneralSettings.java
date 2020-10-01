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
    private String language = "en_US";
    private BroadcastLevel broadcastLevel = BroadcastLevel.SERVER;
    private boolean joinWorldWarning = true;
    private int mobTick = 5;

    public GeneralSettings(Map<String, Object> objectMap) {
        TypeResolvingMap map = SerializationUtil.mapOf(objectMap);
        language = map.getValueOrDefault("language", language);
        broadcastLevel = EnumUtil.parse(map.getValueOrDefault("broadcastLevel", broadcastLevel.name()), BroadcastLevel.class);
        joinWorldWarning = map.getValueOrDefault("joinWorldWarning", joinWorldWarning);
        mobTick = map.getValueOrDefault("mobTick", mobTick);
    }

    public GeneralSettings() {
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        return SerializationUtil.newBuilder()
                .add("language", language)
                .add("broadcastLevel", broadcastLevel)
                .add("joinWorldWarning", joinWorldWarning)
                .add("mobTick", mobTick)
                .build();
    }
}
