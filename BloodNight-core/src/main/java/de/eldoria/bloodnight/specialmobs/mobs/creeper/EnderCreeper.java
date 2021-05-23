package de.eldoria.bloodnight.specialmobs.mobs.creeper;

import de.eldoria.bloodnight.bloodmob.utils.BloodMobUtil;
import de.eldoria.bloodnight.core.BloodNight;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.util.Vector;

import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

public class EnderCreeper extends AbstractCreeper {
    private final ThreadLocalRandom rand = ThreadLocalRandom.current();
    private Instant lastTeleport = Instant.now();

    public EnderCreeper(Creeper creeper) {
        super(creeper);
    }

    @Override
    public void tick() {
        BloodMobUtil.spawnParticlesAround(getBaseEntity().getLocation(), Particle.REDSTONE, new Particle.DustOptions(Color.PURPLE, 2), 5);
        teleportToTarget();
    }

    @Override
    public void onTargetEvent(EntityTargetEvent event) {
        teleportToTarget();
    }

    @Override
    public void onDamageByEntity(EntityDamageByEntityEvent event) {
        if (getBaseEntity().getTarget() == event.getDamager()) {
            return;
        }

        if (event.getDamager() instanceof LivingEntity) {
            getBaseEntity().setTarget((LivingEntity) event.getDamager());
        }

        teleportToTarget();
    }

    private void teleportToTarget() {
        if (lastTeleport.isBefore(Instant.now().minusSeconds(5))) return;
        LivingEntity target = getBaseEntity().getTarget();

        if (target == null) return;

        double distance = target.getLocation().distance(getBaseEntity().getLocation());

        if (distance > 5) {
            Location loc = target.getLocation();
            Vector fuzz = new Vector(rand.nextDouble(-2, 2), 0, rand.nextDouble(-2, 2));
            Block first = loc.getWorld().getBlockAt(loc.add(fuzz));
            Block second = first.getRelative(0, 1, 0);
            if (first.getType() == Material.AIR && second.getType() == Material.AIR) {
                Location newLoc = first.getLocation();
                BloodNight.logger().finer("Teleport from " + getBaseEntity().getLocation() + " to " + newLoc);
                getBaseEntity().teleport(newLoc);
                lastTeleport = Instant.now();
                BloodMobUtil.spawnParticlesAround(loc, Particle.PORTAL, 10);
                getBaseEntity().playEffect(EntityEffect.ENTITY_POOF);
            }
            if (lastTeleport.isBefore(Instant.now().minusSeconds(8))) {
                getBaseEntity().teleport(target.getLocation());
                lastTeleport = Instant.now();
                BloodMobUtil.spawnParticlesAround(loc, Particle.PORTAL, 10);
                getBaseEntity().playEffect(EntityEffect.ENTITY_POOF);
            }
        }
    }
}
