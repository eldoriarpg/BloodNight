package de.eldoria.bloodnight.specialmob.node.action;

import de.eldoria.bloodnight.specialmob.ISpecialMob;
import de.eldoria.bloodnight.specialmob.node.Node;
import de.eldoria.bloodnight.specialmob.node.context.ContextContainer;
import de.eldoria.bloodnight.specialmobs.SpecialMobUtil;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class TeleportToTarget implements Node {
    private int cooldown;
    private Instant lastTeleport = Instant.now();
    private static final int MAX_DIST_SQRT = 40000;
    private static final int MIN_DIST_SQRT = 25;

    @Override
    public void handle(ISpecialMob mob, ContextContainer context) {
        if (lastTeleport.isBefore(Instant.now().minusSeconds(cooldown))) return;
        LivingEntity target = mob.getBase().getTarget();

        if (target == null) return;

        double distSqrt = target.getLocation().distanceSquared(mob.getBase().getLocation());

        if (distSqrt > MIN_DIST_SQRT && distSqrt > MAX_DIST_SQRT) {
            Location loc = target.getLocation();
            ThreadLocalRandom rand = ThreadLocalRandom.current();

            // find a fuzzy target around.
            Vector fuzz = new Vector(rand.nextDouble(-2, 2), 0, rand.nextDouble(-2, 2));
            Block first = loc.getWorld().getBlockAt(loc.add(fuzz));
            Block second = first.getRelative(0, 1, 0);
            Location targetLoc = null;
            // check if there is space
            if (first.getType() == Material.AIR && second.getType() == Material.AIR) {
                targetLoc = first.getLocation();
            } else if (lastTeleport.isBefore(Instant.now().minusSeconds(cooldown + 3))) {
                // if teleport wasn't successful for some time we teleport to the target directly.
                targetLoc = target.getLocation();
            }

            if (targetLoc != null) {
                mob.getBase().teleport(targetLoc);
                lastTeleport = Instant.now();
                SpecialMobUtil.spawnParticlesAround(loc, Particle.PORTAL, 10);
                mob.getBase().playEffect(EntityEffect.ENTITY_POOF);
            }
        }
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        return null;
    }
}
