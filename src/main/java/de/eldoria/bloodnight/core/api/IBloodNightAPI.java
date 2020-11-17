package de.eldoria.bloodnight.core.api;

import org.bukkit.World;

import java.util.Set;

public interface IBloodNightAPI {
	/**
	 * Checks if a blood night is active.
	 *
	 * @param world world
	 *
	 * @return true if a blood night is active.
	 */
	boolean isBloodNightActive(World world);

	/**
	 * Force the next night to be a blood night in a world.
	 * <p>
	 * This will not set the time in the world.
	 *
	 * @param world world
	 */
	void forceNight(World world);

	/**
	 * Cancels a blood night a world if one is active.
	 *
	 * @param world world
	 */
	void cancelNight(World world);

	/**
	 * Get all worlds where a blood night is currently active.
	 *
	 * @return set of worlds.
	 */
	Set<World> getBloodWorlds();

	/**
	 * Returns how many seconds of the blood night are left.
	 *
	 * @param world the world to check
	 *
	 * @return the amount of seconds or 0 if not blood night is active.
	 */
	int getSecondsLeft(World world);

	/**
	 * Get the percent of blood night duration left.
	 * <p>
	 * The start is 100 and the end is 0.
	 * <p>
	 * If no blood night is active this method will always return 0.
	 *
	 * @param world the world to check
	 *
	 * @return the percent between 100 and 0.
	 */
	double getPercentleft(World world);

	/**
	 * Get the probability of the next night to become a blood night.
	 * <p>
	 * Calling this function is equal to {@link #nextProbability(World, int)} with offset 1;
	 *
	 * @param world world to check
	 *
	 * @return probability between 0 and 100. Where 100 is a guaranteed blood night.
	 */
	default int nextProbability(World world) {
		return nextProbability(world, 1);
	}

	/**
	 * Get the probability of the next night to become a blood night.
	 *
	 * @param world  world to check
	 * @param offset offset of nights. The next night has a offset of 1. The last night has a offset of 0.
	 *
	 * @return probability between 0 and 100. Where 100 is a guaranteed blood night.
	 */
	int nextProbability(World world, int offset);
}
