package de.eldoria.bloodnight.bloodmob.settings;

import de.eldoria.bloodnight.bloodmob.serialization.annotation.NumericProperty;
import de.eldoria.bloodnight.bloodmob.serialization.annotation.Property;
import de.eldoria.bloodnight.config.ConfigCheck;
import de.eldoria.bloodnight.config.ConfigException;
import lombok.Setter;

public class Stats implements ConfigCheck {
    @Setter
    @Property(name = "", descr = "")
    private MobValueModifier healthModifier = MobValueModifier.DEFAULT;
    /**
     * The max health of a mob. -1 is disabled
     */
    @Setter
    @NumericProperty(name = "", descr = "", max = 64)
    private double health = 2;

    @Setter
    @Property(name = "", descr = "")
    private MobValueModifier damageModifier = MobValueModifier.DEFAULT;
    /**
     * The damage a mob makes. -1 is disabled
     */
    @Setter
    @NumericProperty(name = "", descr = "", max = 64)
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Stats stats = (Stats) o;

        if (Double.compare(stats.health, health) != 0) return false;
        if (Double.compare(stats.damage, damage) != 0) return false;
        if (healthModifier != stats.healthModifier) return false;
        return damageModifier == stats.damageModifier;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = healthModifier != null ? healthModifier.hashCode() : 0;
        temp = Double.doubleToLongBits(health);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (damageModifier != null ? damageModifier.hashCode() : 0);
        temp = Double.doubleToLongBits(damage);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
