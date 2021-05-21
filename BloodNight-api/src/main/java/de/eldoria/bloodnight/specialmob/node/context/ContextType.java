package de.eldoria.bloodnight.specialmob.node.context;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;

public interface ContextType<T extends IContext> {
    /**
     * A context which provides a {@link Cancellable} state to change.
     */
    ContextType<ICancelableContext> CANCELABLE = () -> ICancelableContext.class;
    /**
     * A context which provides a {@link DamageCause} of a {@link EntityDamageEvent}
     */
    ContextType<IDamageCauseContext> DAMAGE_CAUSE = () -> IDamageCauseContext.class;
    /**
     * A context which provides a {@link Entity} from an {@link EntityEvent}
     */
    ContextType<IEntityContext> ENTITY = () -> (IEntityContext.class);
    /**
     * A context which provides a {@link Entity} from an {@link EntityEvent}.
     * This entity will always be of type {@link LivingEntity}.
     */
    ContextType<ILivingEntityContext> LIVING_ENTITY = () -> (ILivingEntityContext.class);
    /**
     * A context which provides a {@link Location} from events like {@link EntityTeleportEvent}, {@link PlayerMoveEvent},
     * {@link VehicleMoveEvent}, {@link }.
     * Can also wrap things like {@link Player#getLocation()}.
     */
    ContextType<ILocationContext> LOCATION = () -> (ILocationContext.class);
    /**
     * A context which provides information from event like {@link EntityTeleportEvent}, {@link PlayerMoveEvent}.
     */
    ContextType<IMoveContext> MOVE = () -> (IMoveContext.class);
    /**
     * A context which provides information from {@link PlayerEvent}.
     */
    ContextType<IPlayerContext> PLAYER = () -> (IPlayerContext.class);

    /**
     * Returns the class of the context.
     *
     * @return class of {@link IContext}
     */
    Class<T> contextClazz();
}
