package de.eldoria.bloodnight.util;

import org.bukkit.World;

public class C {
    public static String unescapeWorldName(String world) {
        return world.replace(":", " ");
    }

    public static String escapeWorldName(String world) {
        return world.replace(" ", ":");
    }

    public static String escapeWorldName(World world) {
        return escapeWorldName(world.getName());
    }
}
