package de.eldoria.bloodnight.hooks.worldmanager;

import org.bukkit.World;

public interface WorldManager {
    public static WorldManager DEFAULT = World::getName;

    String getAlias(World world);
}
