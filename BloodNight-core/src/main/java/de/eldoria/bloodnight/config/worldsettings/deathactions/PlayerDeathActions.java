package de.eldoria.bloodnight.config.worldsettings.deathactions;

import de.eldoria.eldoutilities.serialization.SerializationUtil;
import de.eldoria.eldoutilities.serialization.TypeResolvingMap;
import de.eldoria.eldoutilities.utils.EnumUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@SerializableAs("bloodNightPlayerDeathActions")
public class PlayerDeathActions extends DeathActions {
    /**
     * Commands which will be executed when a player dies.
     * <p>
     * Should support the {@code {player}} placeholder.
     */
    private List<String> deathCommands = new ArrayList<>();

    /**
     * Probability of the player to lose and not drop its inventory.
     */
    private int loseInvProbability = 0;
    private int loseExpProbability = 0;

    Map<PotionType, PotionEffectSettings> respawnEffects = new HashMap<PotionType, PotionEffectSettings>() {{
        put(PotionType.WEAKNESS, new PotionEffectSettings(PotionType.WEAKNESS, 10));
    }};

    public PlayerDeathActions(Map<String, Object> objectMap) {
        super(objectMap);
        TypeResolvingMap map = SerializationUtil.mapOf(objectMap);
        deathCommands = map.getValueOrDefault("deathCommands", deathCommands);
        loseInvProbability = map.getValueOrDefault("loseInvProbability", loseInvProbability);
        loseExpProbability = map.getValueOrDefault("loseExpProbability", loseInvProbability);
        respawnEffects = map.getMap("respawnEffects", (key, potionEffectSettings) -> EnumUtil.parse(key, PotionType.class));
    }

    public PlayerDeathActions() {
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        return SerializationUtil.newBuilder(super.serialize())
                .add("deathCommands", deathCommands)
                .add("loseInvProbability", loseInvProbability)
                .add("loseExpProbability", loseExpProbability)
                .addMap("respawnEffects", respawnEffects,
                        (potionEffectType, potionEffectSettings) -> potionEffectType.name())
                .build();
    }
}
