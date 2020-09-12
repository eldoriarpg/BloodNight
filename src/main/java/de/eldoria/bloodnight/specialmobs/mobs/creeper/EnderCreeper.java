package de.eldoria.bloodnight.specialmobs.mobs.creeper;

import de.eldoria.bloodnight.specialmobs.SpecialMobUtil;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.util.Vector;

import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

public class EnderCreeper extends AbstractCreeper {
    private Instant lastTeleport = Instant.now();
    private ThreadLocalRandom rand = ThreadLocalRandom.current();

    public EnderCreeper(Creeper creeper) {
        super(creeper);
    }

    @Override
    public void tick() {
        SpecialMobUtil.spawnParticlesAround(getCreeper(), Particle.PORTAL, 10);
        teleportToTarget();
    }

    @Override
    public void onTargetEvent(EntityTargetEvent event) {
        teleportToTarget();
    }

    @Override
    public void onDamage(EntityDamageEvent event) {
    }

    @Override
    public void onDamageByEntity(EntityDamageByEntityEvent event) {
        if (getCreeper().getTarget() == event.getDamager()) {
            return;
        }

        if (event.getDamager() instanceof LivingEntity) {
            getCreeper().setTarget((LivingEntity) event.getDamager());
        }

        teleportToTarget();
    }

    private void teleportToTarget() {
        if (lastTeleport.isBefore(Instant.now().minusSeconds(10))) return;
        LivingEntity target = getCreeper().getTarget();

        if (target == null) return;

        double distance = target.getLocation().distance(getCreeper().getLocation());

        if (distance > 10) {
            Location loc = target.getLocation();
            Vector vector = new Vector(loc.getX() + rand.nextDouble(-2, 2), loc.getY(), loc.getZ());
            getCreeper().teleport(loc.add(vector));
            lastTeleport = Instant.now();
            SpecialMobUtil.spawnParticlesAround(loc, Particle.PORTAL, 10);
            getCreeper().playEffect(EntityEffect.TELEPORT_ENDER);
        }
    }
}
