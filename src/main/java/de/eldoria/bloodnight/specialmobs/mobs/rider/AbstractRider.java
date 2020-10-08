package de.eldoria.bloodnight.specialmobs.mobs.rider;

import de.eldoria.bloodnight.specialmobs.SpecialMob;
import de.eldoria.bloodnight.specialmobs.SpecialMobUtil;
import de.eldoria.eldoutilities.utils.AttributeUtil;
import lombok.Getter;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;

public abstract class AbstractRider extends SpecialMob<Mob> {
    @Getter
    private final Mob passenger;

    public AbstractRider(Mob carrier, Mob passenger) {
        super(carrier);
        this.passenger = passenger;
        passenger.setCustomName(carrier.getCustomName());
        passenger.setCustomNameVisible(carrier.isCustomNameVisible());
        AttributeUtil.syncAttributeValue(carrier, passenger, Attribute.GENERIC_ATTACK_DAMAGE);
        AttributeUtil.syncAttributeValue(carrier, passenger, Attribute.GENERIC_MAX_HEALTH);
    }

    @Override
    public void onDeath(EntityDeathEvent event) {
        passenger.damage(passenger.getHealth(), getBaseEntity());
    }

    @Override
    public void tick() {
        if (getBaseEntity().isDead() || !getBaseEntity().isValid()) {
            remove();
        }
    }

    @Override
    public void remove() {
        passenger.remove();
        super.remove();
    }

    @Override
    public void onEnd() {
        SpecialMobUtil.spawnParticlesAround(getBaseEntity(), Particle.CAMPFIRE_COSY_SMOKE, 30);
        getBaseEntity().remove();
        passenger.remove();
    }

    @Override
    public void onTargetEvent(EntityTargetEvent event) {
        if (event.getTarget() == null) {
            passenger.setTarget(null);
            return;
        }

        if (event.getTarget() instanceof LivingEntity) {
            passenger.setTarget((LivingEntity) event.getTarget());
        }
    }

    @Override
    public void onDamageByEntity(EntityDamageByEntityEvent event) {
    }

    @Override
    public void onDamage(EntityDamageEvent event) {
        SpecialMobUtil.handleExtendedEntityDamage(getBaseEntity(), getPassenger(), event);
    }

    @Override
    public void onExtensionDamage(EntityDamageEvent event) {
        SpecialMobUtil.handleExtendedEntityDamage(getPassenger(), getBaseEntity(), event);
    }

    @Override
    public void onExtensionDeath(EntityDeathEvent event) {
        getBaseEntity().damage(getBaseEntity().getHealth(), event.getEntity().getKiller());
    }
}
