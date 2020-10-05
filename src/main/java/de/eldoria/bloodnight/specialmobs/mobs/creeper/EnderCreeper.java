package de.eldoria.bloodnight.specialmobs.mobs.creeper;

import de.eldoria.bloodnight.specialmobs.SpecialMobUtil;
import org.bukkit.Color;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Particle;
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
        SpecialMobUtil.spawnParticlesAround(getBaseEntity().getLocation(), Particle.REDSTONE, new Particle.DustOptions(Color.PURPLE, 2), 5);
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
            Vector vector = new Vector(rand.nextDouble(-2, 2), loc.getY(), rand.nextDouble(-2, 2));
            Block highestBlockAt = loc.add(vector).getWorld().getHighestBlockAt(loc);
            getBaseEntity().teleport(highestBlockAt.getLocation(loc).add(0, 1, 0));
            lastTeleport = Instant.now();
            SpecialMobUtil.spawnParticlesAround(loc, Particle.PORTAL, 10);
            getBaseEntity().playEffect(EntityEffect.ENTITY_POOF);
        }
    }
}
