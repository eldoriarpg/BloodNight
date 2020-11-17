package de.eldoria.bloodnight.core.manager.nightmanager;

import de.eldoria.bloodnight.config.worldsettings.NightSettings;
import de.eldoria.bloodnight.config.worldsettings.WorldSettings;
import lombok.experimental.UtilityClass;
import org.bukkit.World;

@UtilityClass
public class NightUtil {
	public int getMoonPhase(World world) {
		int days = (int) Math.floor(world.getFullTime() / 24000d);
		return days % 8;
	}

	/**
	 * Get the progress of the night
	 *
	 * @param world         world which refers to the settings
	 * @param worldSettings settings of the world
	 *
	 * @return the night progress where 0 ist the end and 1 ist the start.
	 */
	public double getNightProgress(World world, WorldSettings worldSettings) {
		NightSettings settings = worldSettings.getNightSettings();
		long total = getDiff(settings.getNightBegin(), settings.getNightEnd());
		long left = getDiff(world.getFullTime(), settings.getNightEnd());
		return Math.max(Math.min(left / (double) total, 1), 0);
	}

	public int getTicksRemaining(World world, WorldSettings worldSettings) {
		double nightProgress = getNightProgress(world, worldSettings);
		NightSettings ns = worldSettings.getNightSettings();

		int nightSeconds = ns.getCurrentNightDuration();
		return (int) (nightSeconds * getNightProgress(world, worldSettings));
	}

	public double getNightTicksPerTick(World world, WorldSettings worldSettings) {
		NightSettings ns = worldSettings.getNightSettings();
		long nightDurationTicks;
		if (ns.isCustomNightDuration()) {
			nightDurationTicks = ns.getCurrentNightDuration() * 20;
		} else {
			nightDurationTicks = getDiff(ns.getNightBegin(), ns.getNightEnd());
		}
		long normalTicks = getDiff(ns.getNightBegin(), ns.getNightEnd());
		return (double) normalTicks / nightDurationTicks;
	}

	public long getDiff(long fullTime, long nextTime) {
		long currentTime = fullTime % 24000;
		return currentTime > nextTime ? 24000 - currentTime + nextTime : nextTime - currentTime;
	}

	public boolean isNight(World world, WorldSettings worldSettings) {
		long openInTicks = getDiff(world.getFullTime(), worldSettings.getNightSettings().getNightBegin());
		long closedInTicks = getDiff(world.getFullTime(), worldSettings.getNightSettings().getNightEnd());
		// check if door should be open
		return openInTicks > closedInTicks;
	}
}
