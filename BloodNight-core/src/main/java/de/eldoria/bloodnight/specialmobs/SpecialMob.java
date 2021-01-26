package de.eldoria.bloodnight.specialmobs;

import lombok.Getter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.*;

import java.time.Instant;

public abstract class SpecialMob<T extends LivingEntity> {
    @Getter
    private final T baseEntity;

    public SpecialMob(T baseEntity) {
        this.baseEntity = baseEntity;
    }

    /**
     * Called at a fixed amount of ticks while the blood night is active.
     * <p>
     * Counting ticks is not a best practice, since the tick speed is not fixed and can be changed.
     * <p>
     * Use {@link Instant} to measure time since the last action.
     */
    public void tick() {
    }

    /**
     * Called when a blood night ends and the special mob is going to be removed in the next step.
     * <p>
     * The mob will be removed by the {@link #remove()} method. Therefore this method should not remove the mob.
     */
    public void onEnd() {

    }

    /**
     * Called when the special mob teleports.
     *
     * @param event Event which was dispatched for this mob
     */
    public void onTeleport(EntityTeleportEvent event) {
    }

    /**
     * Called when the special mob launches a projectile.
     *
     * @param event Event which was dispatched for this mob
     */
    public void onProjectileShoot(ProjectileLaunchEvent event) {
    }

    /**
     * Called when a projectile launched by the special mob hit something.
     *
     * @param event Event which was dispatched for this mob
     */
    public void onProjectileHit(ProjectileHitEvent event) {
    }

    /**
     * Called when the special mob dies.
     *
     * @param event The death event of the death of the special mob.
     */
    public void onDeath(EntityDeathEvent event) {
    }

    /**
     * Called when the special mob kills another entity.
     *
     * @param event The death event of the killed entity.
     */
    public void onKill(EntityDeathEvent event) {
    }

    /**
     * Called when a special mob starts to explode.
     *
     * @param event event of the special mob starting to explode
     */
    public void onExplosionPrimeEvent(ExplosionPrimeEvent event) {
    }

    /**
     * Called when a special mob exploded.
     *
     * @param event event of the explosion of the special mob
     */
    public void onExplosionEvent(EntityExplodeEvent event) {
    }

    /**
     * Called when a special mob changes its target.
     * <p>
     * This will only be called, when the new target is of type player or null.
     * <p>
     * A special mob will never target something else then a player.
     *
     * @param event event containing the new target
     */
    public void onTargetEvent(EntityTargetEvent event) {
    }

    /**
     * Called when the special mob takes damage
     * <p>
     * This is a less specific version of {@link #onDamageByEntity(EntityDamageByEntityEvent)}. Do not implement both.
     *
     * @param event damage event of the special mob taking damage
     */
    public void onDamage(EntityDamageEvent event) {
    }

    /**
     * Called when the entity takes damage from another entity
     * <p>
     * This is a more specific version of {@link #onDamage(EntityDamageEvent)}. Do not implement both.
     *
     * @param event damage event of the special mob taking damage
     */
    public void onDamageByEntity(EntityDamageByEntityEvent event) {
    }

    /**
     * Called when the entity damages another entity
     *
     * @param event event of the special mob dealing damage
     */
    public void onHit(EntityDamageByEntityEvent event) {
    }

    /**
     * Attemts to remove the base entity.
     * <p>
     * This should not be overridden unless your entity has a extension.
     * <p>
     * If you override this just remove the extension and call super afterwards.
     */
    public void remove() {
        if (getBaseEntity().isValid()) {
            getBaseEntity().remove();
        }
    }

    /**
     * This event is called when a entity which is tagged as special mob extension receives damage. This will be most
     * likely the passenger or the carrier of a special mob.
     * <p>
     * This event should be used for damage synchronization.
     * <p>
     * Best practise should be that the damage to the extension is forwarded to the base mob.
     * <p>
     * Dont implement this if the special mob doesn't has an extension.
     *
     * @param event damage event of the extension taking damage,
     */
    public void onExtensionDamage(EntityDamageEvent event) {
    }

    /**
     * This event is called when a entity which is tagged as special mob extension is killed.
     * <p>
     * This will be most likely the passenger or the carrier of a special mob.
     * <p>
     * This event should be used to kill the remaining entity.
     * <p>
     * Dont implement this if the mob doesn't has an extension.
     *
     * @param event damage event of the extension taking damage,
     */
    public void onExtensionDeath(EntityDeathEvent event) {
    }

    /**
     * Checks if the entity is valid.
     * <p>
     * The entity is valid if the base entity is valid.
     *
     * @return true when the base entity is valid.
     */
    public boolean isValid() {
        return baseEntity.isValid();
    }
}
