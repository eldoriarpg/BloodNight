package de.eldoria.bloodnight.config;

import com.google.common.base.Objects;
import de.eldoria.eldoutilities.serialization.SerializationUtil;
import de.eldoria.eldoutilities.serialization.TypeResolvingMap;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
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
     * Indicates if this mob can be spawned
     */
    @Setter
    private boolean active;
    /**
     * Amount of drops.
     */
    @Setter
    private int dropAmount;
    /**
     * If this is true only drops from mobs are choosen and default drops will not drop.
     * if false the drops will be added to default drops.
     */
    @Getter
    @Setter
    private boolean overrideDefaultDrops;
    @Getter
    @Setter
    private List<Drop> drops = new ArrayList<>();

    public MobSetting(Map<String, Object> objectMap) {
        TypeResolvingMap map = SerializationUtil.mapOf(objectMap);
        mobName = map.getValue("mobName");
        assert mobName == null;
        active = map.getValueOrDefault("active", true);
        dropAmount = map.getValueOrDefault("dropAmount", -1);
        overrideDefaultDrops = map.getValueOrDefault("overrideDefaultDrops", false);
        List<Drop> drops = (List<Drop>) map.getOrDefault("drops", Collections.emptyList());
    }

    public MobSetting(String mobName) {
        this.mobName = mobName;
        active = true;
        dropAmount = -1;
        overrideDefaultDrops = false;
    }

    public int getOverridenDropAmount(int dropAmount) {
        return this.dropAmount == -1 ? dropAmount : this.dropAmount;
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        return SerializationUtil.newBuilder()
                .add("mobName", mobName)
                .add("active", active)
                .add("dropAmount", dropAmount)
                .add("drops", drops)
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MobSetting that = (MobSetting) o;
        return Objects.equal(mobName, that.mobName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(mobName);
    }
}
