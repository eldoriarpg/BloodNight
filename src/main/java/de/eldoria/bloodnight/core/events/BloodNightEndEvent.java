package de.eldoria.bloodnight.core.events;

import lombok.Getter;
import org.bukkit.World;
import org.bukkit.event.HandlerList;
import org.bukkit.event.world.WorldEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Event which is fired when a blood nights ends.
 */
@Getter
public class BloodNightEndEvent extends WorldEvent {

	private static final HandlerList HANDLERS = new HandlerList();

	/**
	 * Create a new Blood Night End Event.
	 *
	 * @param world world where the blood night has ended.
	 */
	public BloodNightEndEvent(World world) {
		super(world);
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return HANDLERS;
	}
}
