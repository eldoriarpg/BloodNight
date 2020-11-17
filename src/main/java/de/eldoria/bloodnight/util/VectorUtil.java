package de.eldoria.bloodnight.util;

import lombok.experimental.UtilityClass;
import org.bukkit.Location;
import org.bukkit.util.Vector;

@UtilityClass
public class VectorUtil {

	public static Vector getDirectionVector(Location start, Location target) {
		return getDirectionVector(start.toVector(), target.toVector());
	}

	public static Vector getDirectionVector(Vector start, Vector target) {
		return new Vector(target.getX() - start.getX(), target.getY() - start.getY(), target.getZ() - start.getZ());
	}
}
