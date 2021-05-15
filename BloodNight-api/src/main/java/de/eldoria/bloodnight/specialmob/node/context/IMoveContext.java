package de.eldoria.bloodnight.specialmob.node.context;

import org.bukkit.Location;

public interface IMoveContext extends ILocationContext {
    Location getNewLocation();
}
