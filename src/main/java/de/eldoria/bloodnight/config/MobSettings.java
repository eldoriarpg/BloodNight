package de.eldoria.bloodnight.config;

import de.eldoria.bloodnight.specialmobs.SpecialMobType;
import de.eldoria.eldoutilities.serialization.SerializationUtil;
import de.eldoria.eldoutilities.serialization.TypeResolvingMap;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.Map;

@Getter
@Setter
@SerializableAs("bloodNightMobSettings")
public class MobSettings implements ConfigurationSerializable {

    private final int spawnPercentage;
    private Map<SpecialMobType, MobSetting> mobTypes = new EnumMap<>(SpecialMobType.class);

    public MobSettings(Map<String, Object> objectMap) {
        TypeResolvingMap map = SerializationUtil.mapOf(objectMap);
        spawnPercentage = map.getValueOrDefault("spawnPercentage", 80);
        for (SpecialMobType value : SpecialMobType.values()) {
            mobTypes.put(value, map.getValueOrDefault(value.toString(), new MobSetting()));
        }
    }

    public MobSettings() {
        spawnPercentage = 80;
        for (SpecialMobType value : SpecialMobType.values()) {
            mobTypes.put(value, new MobSetting());
        }
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        SerializationUtil.Builder builder = SerializationUtil.newBuilder();
        builder.add("spawnPercentage", spawnPercentage);
        for (Map.Entry<SpecialMobType, MobSetting> entry : mobTypes.entrySet()) {
            builder.add(entry.getKey().toString(), entry.getValue());
        }
        return builder.build();
    }

    public boolean isActive(SpecialMobType mobType) {
        return mobTypes.get(mobType).isActive();
    }
}
