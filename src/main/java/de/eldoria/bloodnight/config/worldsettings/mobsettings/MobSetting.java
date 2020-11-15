package de.eldoria.bloodnight.config.worldsettings.mobsettings;

import com.google.common.base.Objects;
import de.eldoria.eldoutilities.serialization.SerializationUtil;
import de.eldoria.eldoutilities.serialization.TypeResolvingMap;
import de.eldoria.eldoutilities.utils.EnumUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@SerializableAs("bloodNightMobSetting")
public class MobSetting implements ConfigurationSerializable {
    /**
     * plugin name of the mob
     */
    private final String mobName;
    /**
     * The display name of the mob. Uses § as color code identifier.
     */
    private String displayName;
    /**
     * Indicates if this mob can be spawned
     */
    @Setter
    private boolean active = true;
    /**
     * Amount of drops.
     */
    @Setter
    private int dropAmount = -1;
    /**
     * If this is true only drops from mobs are choosen and default drops will not drop. if false the drops will be
     * added to default drops.
     */
    @Setter
    private boolean overrideDefaultDrops = false;
    @Setter
    private List<Drop> drops = new ArrayList<>();

    @Setter
    private MobValueModifier healthModifier = MobValueModifier.DEFAULT;
    /**
     * The max health of a mob. -1 is disabled
     */
    @Setter
    private double health = 2;

    @Setter
    private MobValueModifier damageModifier = MobValueModifier.DEFAULT;
    /**
     * The damage a mob makes. -1 is disabled
     */
    @Setter
    private double damage = 2;

    public MobSetting(Map<String, Object> objectMap) {
        TypeResolvingMap map = SerializationUtil.mapOf(objectMap);
        mobName = map.getValue("mobName");
        if (mobName == null) {
            throw new NullPointerException("Mob name is null. This is not allowed");
        }
        setDisplayName(map.getValueOrDefault("displayName", ""));
        active = map.getValueOrDefault("active", active);
        dropAmount = map.getValueOrDefault("dropAmount", dropAmount);
        overrideDefaultDrops = map.getValueOrDefault("overrideDefaultDrops", overrideDefaultDrops);
        drops = map.getValueOrDefault("drops", drops);
        healthModifier = EnumUtil.parse(map.getValueOrDefault("healthModifier", healthModifier.name()), MobValueModifier.class);
        health = map.getValueOrDefault("health", health);
        damageModifier = EnumUtil.parse(map.getValueOrDefault("damageModifier", damageModifier.name()), MobValueModifier.class);
        damage = map.getValueOrDefault("damage", damage);
    }

    public MobSetting(String mobName) {
        this.mobName = mobName;
        this.displayName = "";
    }

    public int getOverridenDropAmount(int dropAmount) {
        return this.dropAmount <= 0 ? dropAmount : this.dropAmount;
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        return SerializationUtil.newBuilder()
                .add("mobName", mobName)
                .add("displayName", displayName)
                .add("active", active)
                .add("dropAmount", dropAmount)
                .add("overrideDefaultDrops", overrideDefaultDrops)
                .add("drops", drops)
                .add("healthModifier", healthModifier.name())
                .add("health", health)
                .add("damageModifier", damageModifier.name())
                .add("damage", damage)
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MobSetting that = (MobSetting) o;
        return mobName.equalsIgnoreCase(that.mobName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(mobName);
    }

    public double applyDamage(double baseValue, double defaultMultiplier) {
        switch (damageModifier) {
            case DEFAULT:
                return baseValue * defaultMultiplier;
            case MULTIPLY:
                return baseValue * damage;
            case VALUE:
                return damage;
            default:
                throw new IllegalStateException("Unexpected value: " + damageModifier);
        }
    }

    public double applyHealth(double baseValue, double defaultMultiplier) {
        switch (healthModifier) {
            case DEFAULT:
                return baseValue * defaultMultiplier;
            case MULTIPLY:
                return baseValue * health;
            case VALUE:
                return health;
            default:
                throw new IllegalStateException("Unexpected value: " + healthModifier);
        }
    }

    /**
     * Sets the display name.
     * <p>
     * This will replace & with §
     *
     * @param displayName display name to set.
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName.replace("&", "§");
    }
}
