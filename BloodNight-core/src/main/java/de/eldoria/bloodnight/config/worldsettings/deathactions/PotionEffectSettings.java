package de.eldoria.bloodnight.config.worldsettings.deathactions;

import de.eldoria.eldoutilities.serialization.SerializationUtil;
import de.eldoria.eldoutilities.serialization.TypeResolvingMap;
import de.eldoria.eldoutilities.utils.EnumUtil;
import lombok.Getter;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@Getter
public class PotionEffectSettings implements ConfigurationSerializable {
    private PotionType effectType;
    private int duration = 10;

    public PotionEffectSettings(PotionType effectType, int duration) {
        this.effectType = effectType;
        this.duration = duration;
    }

    public PotionEffectSettings(Map<String, Object> objectMap) {
        TypeResolvingMap map = SerializationUtil.mapOf(objectMap);
        effectType = map.getValue("effectType", s -> EnumUtil.parse(s, PotionType.class));
        duration = map.getValueOrDefault("duration", duration);
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        return SerializationUtil.newBuilder()
                .add("effectType", effectType.name())
                .add("duration", duration)
                .build();
    }
}
