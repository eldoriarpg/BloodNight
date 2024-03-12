package de.eldoria.bloodnight.hooks.worldmanager;

import org.bukkit.World;

public interface WorldManager {
    WorldManager DEFAULT = World::getName;

    String getAlias(World world);
}
