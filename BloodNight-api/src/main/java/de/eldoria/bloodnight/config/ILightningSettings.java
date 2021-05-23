package de.eldoria.bloodnight.config;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

public interface ILightningSettings extends ConfigurationSerializable {
    boolean isDoLightning();

    int getLightning();

    boolean isDoThunder();

    int getThunder();
}
