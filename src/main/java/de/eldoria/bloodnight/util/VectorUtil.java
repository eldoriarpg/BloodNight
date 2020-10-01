package de.eldoria.bloodnight.util;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public final class VectorUtil {

    public static Vector getDirectionVector(Location start, Location target) {
        return getDirectionVector(start.toVector(), target.toVector());
    }

    public static Vector getDirectionVector(Vector start, Vector target) {
        return new Vector(target.getX() - start.getX(), target.getX() - start.getY(), target.getZ() - start.getZ());
    }
}
