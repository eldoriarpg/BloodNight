package de.eldoria.bloodnight.bloodmob.node.context;

import org.bukkit.Location;

public interface IMoveContext extends ILocationContext {
    static IMoveContext of(Location from, Location to) {
        return new IMoveContext() {
            @Override
            public Location getNewLocation() {
                return to;
            }

            @Override
            public Location getLocation() {
                return from;
            }
        } ;
    }

    Location getNewLocation();
}
