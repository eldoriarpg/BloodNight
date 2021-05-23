package de.eldoria.bloodnight.bloodmob.node.contextcontainer;

import de.eldoria.bloodnight.bloodmob.node.context.*;
import de.eldoria.eldoutilities.entityutils.ProjectileSender;
import de.eldoria.eldoutilities.entityutils.ProjectileUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.entity.*;

import java.util.function.Function;

public class ContextContainerFactory {
    public static ContextContainer of(EntityTeleportEvent event) {
        return ContextContainer.builder()
                .add(ContextType.MOVE, IMoveContext.of(event.getFrom(), event.getTo()), "")
                .add(ContextType.LIVING_ENTITY, ILivingEntityContext.of((LivingEntity) event.getEntity()), "")
                .add(ContextType.CANCELABLE, ICancelableContext.of(event), "")
                .build();
    }

    public static ContextContainer of(ProjectileLaunchEvent event) {
        ProjectileSender projectileSource = ProjectileUtil.getProjectileSource(event.getEntity());

        return ofCancelable(event)
                .add(ContextType.ENTITY, IEntityContext.of(event.getEntity()), "")
                .add(ContextType.LOCATION, ILocationContext.of(event.getLocation()),"")
                .add(ContextType.LIVING_ENTITY, ILivingEntityContext.of((LivingEntity) projectileSource.getEntity()), "")
                .build();
    }

    public static ContextContainer of(ProjectileHitEvent event) {
        return ofCancelable(event)
                .add(ContextType.ENTITY, IEntityContext.of(event.getEntity()), "")
                .add(ContextType.LIVING_ENTITY, ILivingEntityContext.of((LivingEntity) event.getHitEntity()), "")
                .build();
    }

    public static ContextContainer of(EntityDamageEvent event) {
        return ofCancelable(event)
                .add(ContextType.DAMAGE_CAUSE, IDamageCauseContext.of(event.getCause()), "")
                .add(ContextType.LIVING_ENTITY, ILivingEntityContext.of((LivingEntity) event.getEntity()), "")
                .build();
    }

    public static ContextContainer of(ExplosionPrimeEvent event) {
        return ofCancelable(event).build();
    }

    public static ContextContainer of(EntityExplodeEvent event) {
        return ofCancelable(event)
                .add(ContextType.LOCATION, ILocationContext.of(event.getLocation()), "")
                .add(ContextType.ENTITY, IEntityContext.of(event.getEntity()), "")
                .build();
    }

    public static ContextContainer of(EntityTargetEvent event) {
        return ofCancelable(event)
                .add(ContextType.LIVING_ENTITY, ILivingEntityContext.of((LivingEntity) event.getTarget()), "")
                .build();
    }

    public static ContextContainer of(EntityDamageByEntityEvent event, Function<EntityDamageByEntityEvent, Entity> entity) {
        return ofCancelable(event)
                .add(ContextType.LIVING_ENTITY, ILivingEntityContext.of(entity.apply(event)), "")
                .build();
    }

    private static ContextContainer.Builder ofCancelable(Cancellable cancellable) {
        return ContextContainer.builder(ContextType.CANCELABLE, ICancelableContext.of(cancellable), "");
    }
}
