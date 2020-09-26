package de.eldoria.bloodnight.config;

import de.eldoria.bloodnight.core.mobfactory.MobFactory;
import de.eldoria.bloodnight.core.mobfactory.SpecialMobRegistry;
import de.eldoria.eldoutilities.serialization.SerializationUtil;
import de.eldoria.eldoutilities.serialization.TypeResolvingMap;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Getter
@Setter
@SerializableAs("bloodNightMobSettings")
public class MobSettings implements ConfigurationSerializable {

    private final int spawnPercentage;
    private Map<String, MobSetting> mobTypes = new HashMap<>();

    public MobSettings(Map<String, Object> objectMap) {
        TypeResolvingMap map = SerializationUtil.mapOf(objectMap);
        spawnPercentage = map.getValueOrDefault("spawnPercentage", 80);
        for (MobFactory value : SpecialMobRegistry.getRegisteredMobs()) {
            mobTypes.put(value.getMobName(), map.getValueOrDefault(value.getMobName(), new MobSetting()));
        }
    }

    public MobSettings() {
        spawnPercentage = 80;
        for (MobFactory value : SpecialMobRegistry.getRegisteredMobs()) {
            mobTypes.put(value.getMobName(), new MobSetting());
        }
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        SerializationUtil.Builder builder = SerializationUtil.newBuilder();
        builder.add("spawnPercentage", spawnPercentage);
        for (Map.Entry<String, MobSetting> entry : mobTypes.entrySet()) {
            builder.add(entry.getKey(), entry.getValue());
        }
        return builder.build();
    }

    public boolean isActive(String mobName) {
        return mobTypes.get(mobName).isActive();
    }

    public Optional<MobSetting> getMobByName(String string) {
        for (Map.Entry<String, MobSetting> entry : mobTypes.entrySet()) {
            if (string.equalsIgnoreCase(entry.getKey())) {
                return Optional.ofNullable(entry.getValue());
            }
        }
        return Optional.empty();
    }
}
