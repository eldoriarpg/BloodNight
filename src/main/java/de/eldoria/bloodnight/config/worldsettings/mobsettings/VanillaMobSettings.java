package de.eldoria.bloodnight.config.worldsettings.mobsettings;

import de.eldoria.eldoutilities.serialization.SerializationUtil;
import de.eldoria.eldoutilities.serialization.TypeResolvingMap;
import de.eldoria.eldoutilities.utils.EnumUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@Setter
@Getter
public class VanillaMobSettings implements ConfigurationSerializable {
    /**
     * The modifier which will be multiplied with monster damage when a non special mob deals damage to players.
     */
    private double damageMultiplier = 2;
    /**
     * The value the damage will be divided by when a player damages a non special mob
     */
    private double healthMultiplier = 2;

    /**
     * The modifier which will be multiplied with the dropped item amount.
     */
    private double dropMultiplier = 2;

    private VanillaDropMode vanillaDropMode = VanillaDropMode.VANILLA;

    private int dropAmount = 1;

    public VanillaMobSettings() {
    }

    public VanillaMobSettings(Map<String, Object> objectMap) {
        TypeResolvingMap map = SerializationUtil.mapOf(objectMap);
        damageMultiplier = map.getValueOrDefault("damageMultiplier", damageMultiplier);
        healthMultiplier = map.getValueOrDefault("healthMultiplier", healthMultiplier);
        dropMultiplier = map.getValueOrDefault("dropMultiplier", dropMultiplier);
        vanillaDropMode = EnumUtil.parse(map.getValueOrDefault("vanillaDropMode", vanillaDropMode.name()), VanillaDropMode.class);
        dropAmount = map.getValueOrDefault("extraDrops", dropAmount);
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        return SerializationUtil.newBuilder()
                .add("damageMultiplier", damageMultiplier)
                .add("healthMultiplier", healthMultiplier)
                .add("dropMultiplier", dropMultiplier)
                .add("vanillaDropMode", vanillaDropMode.toString())
                .add("extraDrops", dropAmount)
                .build();
    }
}
