package de.eldoria.bloodnight.core.api;

import de.eldoria.bloodnight.core.manager.NightManager;
import org.bukkit.World;

import java.util.Set;

/**
 * Provides safe to use methods to interact with Blood Night.
 */
public class BloodNightAPI {
    private final NightManager nightManager;

    public BloodNightAPI(NightManager nightManager) {
        this.nightManager = nightManager;
    }

    /**
     * Checks if a blood night is active.
     *
     * @param world world
     *
     * @return true if a blood night is active.
     */
    public boolean isBloodNightActive(World world) {
        return nightManager.isBloodNightActive(world);
    }

    /**
     * Force the next night to be a blood night in a world. This will not set the time in the world.
     *
     * @param world world
     */
    public void forceNight(World world) {
        nightManager.forceNight(world);
    }

    /**
     * Cancels a blood night a world if one is active.
     *
     * @param world world
     */
    public void cancelNight(World world) {
        nightManager.cancelNight(world);
    }

    /**
     * Get all worlds where a blood night is currently active.
     *
     * @return set of worlds.
     */
    public Set<World> getBloodWorlds() {
        return nightManager.getBloodWorlds();
    }
}
