package de.eldoria.bloodnight.util;

import de.eldoria.bloodnight.core.BloodNight;
import lombok.experimental.UtilityClass;
import org.bukkit.NamespacedKey;
import org.bukkit.World;

@UtilityClass
public class C {
    public static NamespacedKey getBossBarNamespace(World world) {
        return BloodNight.getNamespacedKey("bossBar" + world.getName());
    }

}
