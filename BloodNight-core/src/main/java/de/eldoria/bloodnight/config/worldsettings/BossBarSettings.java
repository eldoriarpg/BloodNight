package de.eldoria.bloodnight.config.worldsettings;

import com.google.common.collect.Lists;
import de.eldoria.eldoutilities.messages.conversion.MiniMessageConversion;
import de.eldoria.eldoutilities.serialization.SerializationUtil;
import de.eldoria.eldoutilities.serialization.TypeResolvingMap;
import de.eldoria.eldoutilities.utils.EnumUtil;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@SerializableAs("bloodNightBossBarSettings")
public class BossBarSettings implements ConfigurationSerializable {
    private boolean enabled = true;
    /**
     * Boss bar title with § as color identifier
     */
    private String title = "<red><bold>Blood Night";
    private BossBar.Color color = BossBar.Color.RED;
    private List<BossBar.Flag> effects = new ArrayList<>() {
        {
            add(BossBar.Flag.CREATE_WORLD_FOG);
            add(BossBar.Flag.DARKEN_SCREEN);
        }
    };

    public BossBarSettings() {
    }

    public BossBarSettings(Map<String, Object> objectMap) {
        TypeResolvingMap map = SerializationUtil.mapOf(objectMap);
        this.enabled = map.getValue("enabled");
        setTitle(map.getValue("title"));
        this.color = map.getValue("color", v -> EnumUtil.parse(v, BossBar.Color.class).orElse(BossBar.Color.RED));
        List<String> effects = map.getValue("effects");
        this.effects = effects.stream().map(v -> EnumUtil.parse(v, BossBar.Flag.class).orElse(null)).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public void toggleEffect(BossBar.Flag flag) {
        if (effects.contains(flag)) {
            effects.remove(flag);
        } else {
            effects.add(flag);
        }
    }

    public Set<BossBar.Flag> getEffects() {
        return Set.copyOf(effects);
    }

    public boolean isEffectEnabled(BossBar.Flag flag) {
        return effects.contains(flag);
    }

    public void setTitle(String title) {
        this.title = MiniMessageConversion.convertLegacyColorCodes(title);
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        return SerializationUtil.newBuilder()
                .add("enabled", enabled)
                .add("title", title)
                .add("color", color)
                .add("effects", Lists.newArrayList(effects).stream().map(BossBar.Flag::name).collect(Collectors.toList()))
                .build();
    }
}
