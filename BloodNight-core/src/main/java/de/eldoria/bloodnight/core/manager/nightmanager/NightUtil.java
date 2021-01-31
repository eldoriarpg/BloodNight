package de.eldoria.bloodnight.core.manager.nightmanager;

import de.eldoria.bloodnight.config.worldsettings.NightSettings;
import de.eldoria.bloodnight.config.worldsettings.WorldSettings;
import de.eldoria.eldoutilities.utils.EMath;
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
     * @return the night progress where 0 ist the end and 1 ist the start.
     */
    public double getNightProgress(World world, WorldSettings worldSettings) {
        NightSettings settings = worldSettings.getNightSettings();
        long total = getDiff(settings.getNightBegin(), settings.getNightEnd());
        long left = getDiff(world.getFullTime(), settings.getNightEnd());
        return EMath.clamp(0, 1, left / (double) total);
    }

    public int getSecondsRemaining(World world, WorldSettings worldSettings) {
        NightSettings ns = worldSettings.getNightSettings();
        int nightSeconds = ns.getCurrentNightDuration() / 20;
        return (int) (nightSeconds * getNightProgress(world, worldSettings));
    }

    public double getNightTicksPerTick(World world, WorldSettings worldSettings) {
        NightSettings ns = worldSettings.getNightSettings();
        long nightDurationTicks = ns.getCurrentNightDuration();
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
        return openInTicks > closedInTicks;
    }
}