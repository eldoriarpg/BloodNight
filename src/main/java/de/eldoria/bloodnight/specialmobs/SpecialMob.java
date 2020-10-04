package de.eldoria.bloodnight.specialmobs;

import lombok.Getter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;

public abstract class SpecialMob<T extends LivingEntity> {
    @Getter
    private final T baseEntity;

    public SpecialMob(T baseEntity) {
        this.baseEntity = baseEntity;
    }

    /**
     * Called when the entity is spawned.
     */
    public void onSpawn() {
    }

    /**
     * Called at a fixed amount of ticks while the blood night is active.
     */
    public void tick() {
    }

    /**
     * Called when a blood night ends.
     */
    public void onEnd() {

    }

    /**
     * Called when the entity teleports.
     */
    public void onTeleport(EntityTeleportEvent event) {
    }

    /**
     * Called when the entity launches a projectile.
     */
    public void onProjectileShoot(ProjectileLaunchEvent event) {
    }

    /**
     * Called when the a projectile of the entity hit something.
     */
    public void onProjectileHit(ProjectileHitEvent event) {
    }

    /**
     * Called when the entity dies.
     */
    public void onDeath(EntityDeathEvent event) {
    }

    /**
     * Called when the entity kills another entity.
     */
    public void onKill(EntityDeathEvent event) {
    }

    /**
     * Called when a entitz starts to explode
     *
     * @param event
     */
    public void onExplosionPrimeEvent(ExplosionPrimeEvent event) {
    }

    /**
     * Called when a entity exploded
     *
     * @param event
     */
    public void onExplosionEvent(EntityExplodeEvent event) {
    }

    /**
     * Called when a entity changes its target.
     * This will only be called, when the new target is of type player or null.
     *
     * @param event event to handle
     */
    public void onTargetEvent(EntityTargetEvent event) {
    }

    /**
     * Called when the entity takes damage
     *
     * @param event
     */
    public void onDamage(EntityDamageEvent event) {
    }

    /**
     * Called when the entity takes damage from another entity
     *
     * @param event
     */
    public void onDamageByEntity(EntityDamageByEntityEvent event) {
    }

    /**
     * Called when the entity damages another entity
     *
     * @param event
     */
    public void onHit(EntityDamageByEntityEvent event) {
    }

    public final void remove() {
        if (getBaseEntity().isValid()) {
            getBaseEntity().remove();
        }
    }
}
