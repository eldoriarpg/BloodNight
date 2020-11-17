package de.eldoria.bloodnight.util;

import lombok.experimental.UtilityClass;
import org.bukkit.Sound;

@UtilityClass
public class Sounds {
	public static final Sound[] SPOOKY = {
			Sound.AMBIENT_BASALT_DELTAS_ADDITIONS,
			Sound.AMBIENT_CRIMSON_FOREST_ADDITIONS,
			Sound.AMBIENT_NETHER_WASTES_ADDITIONS,
			Sound.AMBIENT_SOUL_SAND_VALLEY_ADDITIONS
	};
	public static final Sound[] LOOPS = {
			Sound.AMBIENT_BASALT_DELTAS_LOOP,
			Sound.AMBIENT_WARPED_FOREST_LOOP,
			Sound.AMBIENT_SOUL_SAND_VALLEY_LOOP
	};
	public static final Sound[] START = {
			Sound.ENTITY_LIGHTNING_BOLT_THUNDER,
	};
}
