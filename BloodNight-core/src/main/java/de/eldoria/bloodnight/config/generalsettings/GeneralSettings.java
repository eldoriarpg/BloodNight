package de.eldoria.bloodnight.config.generalsettings;

import de.eldoria.bloodnight.core.BloodNight;
import de.eldoria.eldoutilities.messages.conversion.MiniMessageConversion;
import de.eldoria.eldoutilities.serialization.SerializationUtil;
import de.eldoria.eldoutilities.serialization.TypeResolvingMap;
import lombok.Setter;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Setter
@SerializableAs("bloodNightGeneralSettings")
public class GeneralSettings implements ConfigurationSerializable {
    private String language = "en_US";
    private String prefix = "<red>[BN]";
    private BroadcastLevel broadcastLevel = BroadcastLevel.SERVER;
    private BroadcastMethod broadcastMethod = BroadcastMethod.SUBTITLE;
    private BroadcastMethod messageMethod = BroadcastMethod.SUBTITLE;
    private int mobTick = 5;
    private boolean blindness = true;
    private boolean joinWorldWarning = true;
    private boolean updateReminder = true;
    private boolean autoUpdater;
    private boolean beeFix;
    private boolean spawnerDropSuppression = true;
    private boolean ignoreSpawnerMobs;
    private List<String> blockedCommands = new ArrayList<>();
    private List<EntityType> noVanillaDropIncrease = new ArrayList<>();

    public GeneralSettings(Map<String, Object> objectMap) {
        TypeResolvingMap map = SerializationUtil.mapOf(objectMap);
        language = map.getValueOrDefault("language", language);
        prefix = map.getValueOrDefault("prefix", prefix.replace("&", "§"));
        broadcastLevel = map.getValueOrDefault("broadcastLevel", broadcastLevel, BroadcastLevel.class);
        broadcastMethod = map.getValueOrDefault("broadcastMethod", broadcastMethod, BroadcastMethod.class);
        messageMethod = map.getValueOrDefault("messageMethod", messageMethod, BroadcastMethod.class);
        noVanillaDropIncrease = map.getValueOrDefault("noVanillaDropIncrease", noVanillaDropIncrease, EntityType.class);
        mobTick = map.getValueOrDefault("mobTick", mobTick);
        joinWorldWarning = map.getValueOrDefault("joinWorldWarning", joinWorldWarning);
        blindness = map.getValueOrDefault("blindness", blindness);
        updateReminder = map.getValueOrDefault("updateReminder", updateReminder);
        autoUpdater = map.getValueOrDefault("autoUpdater", autoUpdater);
        beeFix = map.getValueOrDefault("beeFix", beeFix);
        spawnerDropSuppression = map.getValueOrDefault("spawnerDropSuppression", spawnerDropSuppression);
        ignoreSpawnerMobs = map.getValueOrDefault("ignoreSpawnerMobs", ignoreSpawnerMobs);
        blockedCommands = map.getValueOrDefault("blockedCommands", blockedCommands);
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
                .add("prefix", prefix)
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
                .add("ignoreSpawnerMobs", ignoreSpawnerMobs)
                .add("blockedCommands", blockedCommands)
                .build();
    }

    public String language() {
        return language;
    }

    public String prefix() {
        return MiniMessageConversion.convertLegacyColorCodes(prefix);
    }

    public BroadcastLevel broadcastLevel() {
        return broadcastLevel;
    }

    public BroadcastMethod broadcastMethod() {
        return broadcastMethod;
    }

    public BroadcastMethod messageMethod() {
        return messageMethod;
    }

    public int mobTick() {
        return mobTick;
    }

    public boolean blindness() {
        return blindness;
    }

    public boolean joinWorldWarning() {
        return joinWorldWarning;
    }

    public boolean updateReminder() {
        return updateReminder;
    }

    public boolean autoUpdater() {
        return autoUpdater;
    }

    public boolean beeFix() {
        return beeFix;
    }

    public boolean spawnerDropSuppression() {
        return spawnerDropSuppression;
    }

    public boolean ignoreSpawnerMobs() {
        return ignoreSpawnerMobs;
    }

    public List<String> blockedCommands() {
        return blockedCommands;
    }

    public List<EntityType> noVanillaDropIncrease() {
        return noVanillaDropIncrease;
    }
}
