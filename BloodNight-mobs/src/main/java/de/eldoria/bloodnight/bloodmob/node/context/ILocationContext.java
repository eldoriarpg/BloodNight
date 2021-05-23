package de.eldoria.bloodnight.bloodmob.node.context;

import org.bukkit.Location;

public interface ILocationContext extends IContext {
    static ILocationContext of(Location location) {
        return () -> location;
    }

    Location getLocation();
}
