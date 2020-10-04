package de.eldoria.bloodnight.specialmobs.mobs.rider;

import de.eldoria.bloodnight.specialmobs.SpecialMob;
import de.eldoria.bloodnight.specialmobs.SpecialMobUtil;
import lombok.Getter;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
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
    }

    @Override
    public void onDeath(EntityDeathEvent event) {
        passenger.damage(passenger.getHealth());
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

}
