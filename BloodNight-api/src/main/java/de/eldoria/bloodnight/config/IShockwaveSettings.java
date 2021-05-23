package de.eldoria.bloodnight.config;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;


public interface IShockwaveSettings extends ConfigurationSerializable {
    void applyEffects(Entity entity, double power);

    int getShockwaveProbability();

    int getShockwavePower();

    int getShockwaveRange();

    double getMinDuration();

    double getPower(Vector vector);
}
