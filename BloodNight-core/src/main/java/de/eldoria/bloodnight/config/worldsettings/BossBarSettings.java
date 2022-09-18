package de.eldoria.bloodnight.config.worldsettings;

import com.google.common.collect.Lists;
import de.eldoria.eldoutilities.serialization.SerializationUtil;
import de.eldoria.eldoutilities.serialization.TypeResolvingMap;
import de.eldoria.eldoutilities.utils.EnumUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter
@Setter
@SerializableAs("bloodNightBossBarSettings")
public class BossBarSettings implements ConfigurationSerializable {
    private boolean enabled = true;
    /**
     * Boss bar title with § as color identifier
     */
    private String title = "§c§lBlood Night";
    private BarColor color = BarColor.RED;
    private List<BarFlag> effects = new ArrayList<>() {
        {
            add(BarFlag.CREATE_FOG);
            add(BarFlag.DARKEN_SKY);
        }
    };

    public BossBarSettings() {
    }

    public BossBarSettings(Map<String, Object> objectMap) {
        TypeResolvingMap map = SerializationUtil.mapOf(objectMap);
        this.enabled = map.getValue("enabled");
        setTitle(map.getValue("title"));
        this.color = map.getValue("color", v -> EnumUtil.parse(v, BarColor.class).orElse(BarColor.RED));
        List<String> effects = map.getValue("effects");
        this.effects = effects.stream().map(v -> EnumUtil.parse(v, BarFlag.class).orElse(null)).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public void toggleEffect(BarFlag flag) {
        if (effects.contains(flag)) {
            effects.remove(flag);
        } else {
            effects.add(flag);
        }
    }

    public BarFlag[] getEffects() {
        return effects.toArray(new BarFlag[0]);
    }

    public boolean isEffectEnabled(BarFlag flag) {
        return effects.contains(flag);
    }

    public void setTitle(String title) {
        this.title = title.replace("&", "§");
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
