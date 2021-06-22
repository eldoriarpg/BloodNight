package de.eldoria.bloodnight.core;

import de.eldoria.eldoutilities.plugin.EldoPlugin;
import org.bukkit.NamespacedKey;

public class ABloodNight extends EldoPlugin {
    public static NamespacedKey getNamespacedKey(String string) {
        return new NamespacedKey(getInstance(), string.replace(" ", "_"));
    }
}
