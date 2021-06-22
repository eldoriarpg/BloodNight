package de.eldoria.bloodnight.bloodmob.node.contextcontainer;

import de.eldoria.bloodnight.bloodmob.node.context.ICancelableContext;
import de.eldoria.bloodnight.bloodmob.node.context.IDamageCauseContext;
import de.eldoria.bloodnight.bloodmob.node.context.IEntityContext;
import de.eldoria.bloodnight.bloodmob.node.context.ILivingEntityContext;
import de.eldoria.bloodnight.bloodmob.node.context.ILocationContext;
import de.eldoria.bloodnight.bloodmob.node.context.IMoveContext;
import de.eldoria.bloodnight.bloodmob.node.context.IPlayerContext;
import de.eldoria.bloodnight.bloodmob.settings.BehaviourNodeType;
import de.eldoria.eldoutilities.entityutils.ProjectileSender;
import de.eldoria.eldoutilities.entityutils.ProjectileUtil;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;

public class ContextContainerFactory {
    public static ContextContainer ofTick() {
        return ContextContainer.builder().build();
    }

    public static ContextContainer ofOnEnd() {
        return ContextContainer.builder().build();
    }

    public static ContextContainer ofOnTeleport(EntityTeleportEvent event) {
        return ofCancelable(event)
                .add(ContextType.MOVE, IMoveContext.of(event.getFrom(), event.getTo()), "")
                .build();
    }

    public static ContextContainer ofOnProjectileShoot(ProjectileLaunchEvent event) {
        ProjectileSender projectileSource = ProjectileUtil.getProjectileSource(event.getEntity());
        return ofCancelable(event)
                .add(ContextType.ENTITY, IEntityContext.of(event.getEntity()), "")
                .add(ContextType.LOCATION, ILocationContext.of(event.getLocation()), "")
                .add(ContextType.LIVING_ENTITY, ILivingEntityContext.of((LivingEntity) projectileSource.getEntity()), "")
                .build();
    }

    public static ContextContainer ofOnProjectileHit(ProjectileHitEvent event) {
        return ofCancelable(event)
                .add(ContextType.ENTITY, IEntityContext.of(event.getEntity()), "")
                .add(ContextType.LIVING_ENTITY, ILivingEntityContext.of((LivingEntity) event.getHitEntity()), "")
                .build();
    }

    public static ContextContainer ofOnDeath(EntityDeathEvent event) {
        return ContextContainer.builder()
                .add(ContextType.PLAYER, IPlayerContext.of(event.getEntity().getKiller()), "")
                .build();
    }

    public static ContextContainer ofOnKill(EntityDeathEvent event) {
        return ContextContainer.builder()
                .add(ContextType.LIVING_ENTITY, ILivingEntityContext.of(event.getEntity()), "")
                .build();
    }

    public static ContextContainer ofOnExplosionPrime(ExplosionPrimeEvent event) {
        return ofCancelable(event).build();
    }

    public static ContextContainer ofOnExplosion(EntityExplodeEvent event) {
        return ofCancelable(event)
                .add(ContextType.LOCATION, ILocationContext.of(event.getLocation()), "")
                .add(ContextType.ENTITY, IEntityContext.of(event.getEntity()), "")
                .build();
    }

    public static ContextContainer ofOnTarget(EntityTargetEvent event) {
        return ofCancelable(event)
                .add(ContextType.LIVING_ENTITY, ILivingEntityContext.of((LivingEntity) event.getTarget()), "")
                .build();
    }

    public static ContextContainer ofOnDamage(EntityDamageEvent event) {
        return ofCancelable(event)
                .add(ContextType.DAMAGE_CAUSE, IDamageCauseContext.of(event.getCause()), "")
                .build();
    }

    public static ContextContainer ofOnDamageByEntity(EntityDamageByEntityEvent event) {
        return ofCancelable(event)
                .add(ContextType.DAMAGE_CAUSE, IDamageCauseContext.of(event.getCause()), "")
                .add(ContextType.LIVING_ENTITY, ILivingEntityContext.of(event.getDamager()), "")
                .build();
    }

    public static ContextContainer ofOnHit(EntityDamageByEntityEvent event) {
        return ofCancelable(event)
                .add(ContextType.LIVING_ENTITY, ILivingEntityContext.of(event.getEntity()), "")
                .build();
    }

    private static ContextContainer.Builder ofCancelable(Cancellable cancellable) {
        return ContextContainer.builder(ContextType.CANCELABLE, ICancelableContext.of(cancellable), "");
    }

    public static ContextContainer mock(BehaviourNodeType type) {
        switch (type) {
            case TICK:
                return ofTick();
            case ON_END:
                return ofOnEnd();
            case ON_TELEPORT:
                return ofOnTeleport(new EntityTeleportEvent(null, null, null));
            case ON_PROJECTILE_SHOOT:
                return ofOnProjectileShoot(new ProjectileLaunchEvent(null));
            case ON_PROJECTILE_HIT:
                return ofOnProjectileHit(new ProjectileHitEvent(null));
            case ON_DEATH:
                return ofOnDeath(new EntityDeathEvent(null, null));
            case ON_KILL:
                return ofOnKill(new EntityDeathEvent(null, null));
            case ON_EXPLOSION_PRIME:
                return ofOnExplosionPrime(new ExplosionPrimeEvent(null));
            case ON_EXPLOSION:
                return ofOnExplosion(new EntityExplodeEvent(null, null, null, 0));
            case ON_TARGET:
                return ofOnTarget(new EntityTargetEvent(null, null, null));
            case ON_DAMAGE:
                return ofOnDamage(new EntityDamageByEntityEvent(null, null, null, 0));
            case ON_DAMAGE_BY_ENTITY:
                return ofOnDamageByEntity(new EntityDamageByEntityEvent(null, null, null, 0));
            case ON_HIT:
                return ofOnHit(new EntityDamageByEntityEvent(null, null, null, 0));
            default:
                throw new IllegalStateException("Unexpected value: " + type);
        }
    }
}
