package de.eldoria.bloodnight.bloodmob.nodeimpl.action;

import de.eldoria.bloodnight.bloodmob.IBloodMob;
import de.eldoria.bloodnight.bloodmob.node.Node;
import de.eldoria.bloodnight.bloodmob.node.contextcontainer.ContextContainer;
import de.eldoria.bloodnight.bloodmob.serialization.annotation.Property;
import de.eldoria.bloodnight.bloodmob.utils.BloodMobUtil;
import lombok.NoArgsConstructor;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@NoArgsConstructor
public class TeleportToTarget implements Node {
    private static final int MAX_DIST_SQRT = 40000;
    private static final int MIN_DIST_SQRT = 25;
    @Property(name = "", descr = "")
    private Particle particle;
    @Property(name = "", descr = "")
    private EntityEffect effect;

    public TeleportToTarget(Particle particle, EntityEffect effect) {
        this.particle = particle;
        this.effect = effect;
    }

    @Override
    public void handle(IBloodMob mob, ContextContainer context) {
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

            Location targetLoc;
            // check if there is space
            if (first.getType() == Material.AIR && second.getType() == Material.AIR) {
                targetLoc = first.getLocation();
            } else {
                // if teleport wasn't successful for some time we teleport to the target directly.
                targetLoc = target.getLocation();
            }

            mob.getBase().teleport(targetLoc);
            if (particle != null) BloodMobUtil.spawnParticlesAround(loc, particle, 10);
            if (effect != null) mob.getBase().playEffect(effect);
        }
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TeleportToTarget that = (TeleportToTarget) o;

        if (particle != that.particle) return false;
        return effect == that.effect;
    }

    @Override
    public int hashCode() {
        int result = particle != null ? particle.hashCode() : 0;
        result = 31 * result + (effect != null ? effect.hashCode() : 0);
        return result;
    }
}
