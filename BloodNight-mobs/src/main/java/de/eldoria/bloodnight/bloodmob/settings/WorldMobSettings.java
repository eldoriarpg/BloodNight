package de.eldoria.bloodnight.bloodmob.settings;

import de.eldoria.bloodnight.bloodmob.drop.Drop;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WorldMobSettings implements ConfigurationSerializable {
    /**
     * Enabled or disables mob names for special mobs.
     */
    private boolean displayMobNames = true;

    /**
     * The modifier which will be multiplied with monster damage when dealing damage to players.
     */
    private double damageMultiplier = 2;

    /**
     * The modifier which will be applied to special Mobs health on spawn.
     */
    private double healthModifier = 2;

    /**
     * The modifier which will be muliplied with the dropped exp of a monster.
     */
    private double experienceMultiplier = 4;

    /**
     * The general drops during blood night.
     */
    private List<Drop> defaultDrops = new ArrayList<>();

    /**
     * If true drops will be added to vanilla drops. If false vanilla drops will be removed.
     */
    private boolean naturalDrops = true;

    /**
     * Max Amount of custom drops which can be dropped on death.
     */
    private int dropAmount = 3;

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        return null;
    }

    public boolean isdisplayMobNames() {
        return displayMobNames;
    }

    public double damageMultiplier() {
        return damageMultiplier;
    }

    public double healthModifier() {
        return healthModifier;
    }

    public double experienceMultiplier() {
        return experienceMultiplier;
    }

    public List<Drop> defaultDrops() {
        return defaultDrops;
    }

    public boolean isnaturalDrops() {
        return naturalDrops;
    }

    public int dropAmount() {
        return dropAmount;
    }
}
