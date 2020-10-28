package de.eldoria.bloodnight.specialmobs.mobs;

import de.eldoria.bloodnight.specialmobs.SpecialMob;
import de.eldoria.bloodnight.specialmobs.SpecialMobUtil;
import de.eldoria.eldoutilities.utils.AttributeUtil;
import lombok.Getter;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;

public class ExtendedSpecialMob<T extends Mob, U extends Mob> extends SpecialMob<T> {
    @Getter
    private final U passenger;

    public ExtendedSpecialMob(T carrier, U passenger) {
        super(carrier);
        this.passenger = passenger;
        passenger.setCustomName(carrier.getCustomName());
        passenger.setCustomNameVisible(carrier.isCustomNameVisible());
        AttributeUtil.syncAttributeValue(carrier, passenger, Attribute.GENERIC_ATTACK_DAMAGE);
        AttributeUtil.syncAttributeValue(carrier, passenger, Attribute.GENERIC_MAX_HEALTH);
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
}
