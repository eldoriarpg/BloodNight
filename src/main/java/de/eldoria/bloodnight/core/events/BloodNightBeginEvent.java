package de.eldoria.bloodnight.core.events;

import lombok.Getter;
import org.bukkit.World;
import org.bukkit.event.HandlerList;
import org.bukkit.event.world.WorldEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Event which is fired when a blood nights begins.
 */
@Getter
public class BloodNightBeginEvent extends WorldEvent {

    private static final HandlerList HANDLERS = new HandlerList();

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
}
