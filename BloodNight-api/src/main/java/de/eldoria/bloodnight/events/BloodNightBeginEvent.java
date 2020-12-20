package de.eldoria.bloodnight.events;

import lombok.Getter;
import org.bukkit.World;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.world.WorldEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Event which is fired when a blood nights begins.
 * <p>
 * This event is {@link Cancellable}. If the event is canceled, no Blood Night will be initialized.
 */
@Getter
public class BloodNightBeginEvent extends WorldEvent implements Cancellable {

	private static final HandlerList HANDLERS = new HandlerList();
	private boolean cancelled;

	/**
	 * Create a new Blood Night Begin Event.
	 *
	 * @param world world where the blood night has begun.
	 */
	public BloodNightBeginEvent(World world) {
		super(world);
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return HANDLERS;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}
}
