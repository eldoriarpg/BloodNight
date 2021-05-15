package de.eldoria.bloodnight.specialmob.settings;

import de.eldoria.bloodnight.config.ConfigCheck;
import de.eldoria.bloodnight.config.ConfigException;
import de.eldoria.bloodnight.config.worldsettings.mobsettings.MobValueModifier;
import lombok.Setter;

public class Stats implements ConfigCheck {
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

    @Override
    public void check(Object data) throws ConfigException {
        ConfigCheck.isNotNull(healthModifier, "health modifier");
        ConfigCheck.isNotNull(damageModifier, "damage modifier");
        ConfigCheck.isInRange(damage, -1, 1024, "damage");
        ConfigCheck.isInRange(health, -1, 1024, "health");
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
}
