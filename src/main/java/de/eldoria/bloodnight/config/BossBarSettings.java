package de.eldoria.bloodnight.config;

import com.google.common.collect.Lists;
import de.eldoria.eldoutilities.serialization.SerializationUtil;
import de.eldoria.eldoutilities.serialization.TypeResolvingMap;
import de.eldoria.eldoutilities.utils.EnumUtil;
import lombok.Getter;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter
@SerializableAs("bloodNightBossBarSettings")
public class BossBarSettings implements ConfigurationSerializable {
    private boolean enabled = true;
    private String title = "§c§lBlood Night";
    private BarColor color = BarColor.RED;
    private BarFlag[] effects = {BarFlag.CREATE_FOG, BarFlag.DARKEN_SKY};

    public BossBarSettings() {
    }

    public BossBarSettings(Map<String, Object> objectMap) {
        TypeResolvingMap map = SerializationUtil.mapOf(objectMap);
        this.enabled = map.getValue("enabled");
        this.title = map.getValue("title");
        this.color = map.getValue("color", v -> EnumUtil.parse(v, BarColor.class));
        List<String> effects = map.getValue("effects");
        this.effects = effects.stream().map(v -> EnumUtil.parse(v, BarFlag.class)).filter(Objects::nonNull).toArray(BarFlag[]::new);
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        return SerializationUtil.newBuilder()
                .add("enabled", enabled)
                .add("title", title)
                .add("color", color)
                .add("effects", Lists.newArrayList(effects).stream().map(BarFlag::toString).collect(Collectors.toList()))
                .build();
    }
}
