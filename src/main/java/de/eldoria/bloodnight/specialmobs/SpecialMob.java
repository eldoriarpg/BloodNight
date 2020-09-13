package de.eldoria.bloodnight.specialmobs;

import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;

public interface SpecialMob {
    /**
     * Called when the entity is spawned.
     */
    default void onSpawn() {
    }

    ;

    /**
     * Called at a fixed amount of ticks while the blood night is active.
     */
    default void tick() {
    }

    /**
     * Called when a blood night ends.
     * Should kill or normalize the entity.
     */
    void onEnd();

    /**
     * Called when the entity teleports.
     */
    default void onTeleport(EntityTeleportEvent event) {
    }

    /**
     * Called when the entity launches a projectile.
     */
    default void onProjectileShoot(ProjectileLaunchEvent event) {
    }

    /**
     * Called when the a projectile of the entity hit something.
     */
    default void onProjectileHit(ProjectileHitEvent event) {
    }

    /**
     * Called when the entity dies.
     */
    default void onDeath(EntityDeathEvent event) {
    }

    /**
     * Called when the entity kills another entity.
     */
    default void onKill(EntityDeathEvent event) {
    }

    /**
     * Called when a entitz starts to explode
     *
     * @param event
     */
    default void onExplosionPrimeEvent(ExplosionPrimeEvent event) {
    }

    /**
     * Called when a entity exploded
     *
     * @param event
     */
    default void onExplosionEvent(EntityExplodeEvent event) {
    }

    /**
     * Called when a entity changes its target
     *
     * @param event
     */
    default void onTargetEvent(EntityTargetEvent event) {

    }

    /**
     * Called when the entity takes damage
     *
     * @param event
     */
    default void onDamage(EntityDamageEvent event) {

    }

    /**
     * Called when the entity takes damage from another entity
     *
     * @param event
     */
    default void onDamageByEntity(EntityDamageByEntityEvent event) {

    }

    /**
     * Called when the entity damages another entity
     * @param event
     */
    default void onHit(EntityDamageByEntityEvent event){

    }
}
