package de.eldoria.bloodnight.config.worldsettings.deathactions;

import de.eldoria.eldoutilities.serialization.SerializationUtil;
import de.eldoria.eldoutilities.serialization.TypeResolvingMap;
import lombok.Getter;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@Getter
@SerializableAs("bloodNightPotionEffectSettings")
public class PotionEffectSettings implements ConfigurationSerializable {
    private final PotionEffectType effectType;
    private int duration = 10;

    public PotionEffectSettings(PotionEffectType effectType, int duration) {
        this.effectType = effectType;
        this.duration = duration;
    }

    public PotionEffectSettings(Map<String, Object> objectMap) {
        TypeResolvingMap map = SerializationUtil.mapOf(objectMap);
        effectType = map.getValue("effectType", PotionEffectType::getByName);
        duration = map.getValueOrDefault("duration", duration);
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        return SerializationUtil.newBuilder()
                .add("effectType", effectType.getName())
                .add("duration", duration)
                .build();
    }
}
