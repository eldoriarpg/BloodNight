package de.eldoria.bloodnight.specialmobs.mobs;

import de.eldoria.bloodnight.specialmobs.SpecialMob;
import de.eldoria.bloodnight.specialmobs.SpecialMobUtil;
import de.eldoria.bloodnight.specialmobs.StatSource;
import de.eldoria.eldoutilities.utils.AttributeUtil;
import lombok.Getter;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;

public class ExtendedSpecialMob<T extends Mob, U extends Mob> extends SpecialMob<T> {
    @Getter
    private final U passenger;

    /**
     * Create a new special mob from a carrier and passender entity.
     * <p>
     * {@link ExtendedSpecialMob#ExtendedSpecialMob(Mob, EntityType)} and {@link ExtendedSpecialMob#ExtendedSpecialMob(EntityType,
     * Mob)} take precendence.
     *
     * @param carrier    carrier
     * @param passenger  passenger
     * @param statSource definex which of the both entity should provide the stats for the other entity
     */
    public ExtendedSpecialMob(T carrier, U passenger, StatSource statSource) {
        super(carrier);
        this.passenger = passenger;
        Mob source = statSource == StatSource.PASSENGER ? passenger : carrier;
        Mob target = statSource == StatSource.PASSENGER ? carrier : passenger;

        target.setCustomName(carrier.getCustomName());
        target.setCustomNameVisible(carrier.isCustomNameVisible());
        AttributeUtil.syncAttributeValue(source, target, Attribute.GENERIC_ATTACK_DAMAGE);
        AttributeUtil.syncAttributeValue(source, target, Attribute.GENERIC_MAX_HEALTH);
    }

    /**
     * Create a new extended special mob from a carrier with a passenger.
     *
     * @param carrier   carrier to bind
     * @param passenger to spawn
     */
    public ExtendedSpecialMob(T carrier, EntityType passenger) {
        this(carrier, SpecialMobUtil.spawnAndMount(carrier, passenger), StatSource.CARRIER);
    }

    /**
     * Create a new extended special mob from passenger with a carrier.
     *
     * @param carrier   carrier to spawn
     * @param passenger passenger to bind
     */
    public ExtendedSpecialMob(EntityType carrier, U passenger) {
        this(SpecialMobUtil.spawnAndMount(carrier, passenger), passenger, StatSource.PASSENGER);
    }

    /**
     * This method already kills the extention.
     * <p>
     * {@inheritDoc}
     *
     * @param event The death event of the death of the special mob.
     */
    @Override
    public void onDeath(EntityDeathEvent event) {
        getBaseEntity().damage(getPassenger().getHealth(), getBaseEntity());
    }

    /**
     * This method already synchronises the target between carrier and extention.
     * <p>
     * {@inheritDoc}
     *
     * @param event event containing the new target
     */
    @Override
    public void onTargetEvent(EntityTargetEvent event) {
        if (event.getTarget() == null) {
            getPassenger().setTarget(null);
            return;
        }

        if (event.getTarget() instanceof LivingEntity) {
            getPassenger().setTarget((LivingEntity) event.getTarget());
        }
    }

    /**
     * This method already forwards damage to the extension.
     * <p>
     * {@inheritDoc}
     *
     * @param event damage event of the special mob taking damage
     */
    @Override
    public void onDamage(EntityDamageEvent event) {
        SpecialMobUtil.handleExtendedEntityDamage(getBaseEntity(), getPassenger(), event);
    }

    /**
     * This method already forwards damage to the carrier.
     * <p>
     * {@inheritDoc}
     *
     * @param event damage event of the extension taking damage,
     */
    @Override
    public void onExtensionDamage(EntityDamageEvent event) {
        SpecialMobUtil.handleExtendedEntityDamage(getPassenger(), getBaseEntity(), event);
    }

    /**
     * This method already forwards damage to the carrier.
     * <p>
     * {@inheritDoc}
     *
     * @param event damage event of the extension taking damage,
     */
    @Override
    public void onExtensionDeath(EntityDeathEvent event) {
        getBaseEntity().damage(getBaseEntity().getHealth(), event.getEntity().getKiller());
    }

    /**
     * This method already removes the extension.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public void remove() {
        passenger.remove();
        super.remove();
    }

    /**
     * {@inheritDoc} The extended special mob is only valid when the base entity has an passenger and the passenger is
     * valid as well.
     *
     * @return true when the base entity and passenger is valid and the base entity has a passenger.
     */
    @Override
    public boolean isValid() {
        return super.isValid() && getPassenger().isValid() && !getBaseEntity().getPassengers().isEmpty();
    }
}
