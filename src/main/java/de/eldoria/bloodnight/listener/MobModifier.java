package de.eldoria.bloodnight.listener;

import de.eldoria.bloodnight.config.Configuration;
import de.eldoria.bloodnight.events.BloodNightEndEvent;
import de.eldoria.bloodnight.specialmobs.SpecialMob;
import lombok.NonNull;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class MobModifier implements Listener, Runnable {
    private final Map<String, WorldMobs> mobRegistry = new HashMap<>();
    private final NightListener nightListener;
    private final Configuration configuration;

    public MobModifier(NightListener nightListener, Configuration configuration) {
        this.nightListener = nightListener;
        this.configuration = configuration;
    }

    @EventHandler
    public void onMobspawn(EntitySpawnEvent event) {
        if (nightListener.isBloodNightActive(event.getEntity().getWorld())) return;


        //TODO: Add different types of mobs. Powered Creeper. Speed boosted Monster with potion effects.
    }

    @EventHandler
    public void onEntityTeleport(EntityTeleportEvent event) {
        getWorldMobs(event.getEntity().getWorld()).invokeIfPresent(event.getEntity(), v -> v.onTeleport(event));
    }

    @EventHandler
    public void onProjectileShoot(ProjectileHitEvent event) {
        getWorldMobs(event.getEntity().getWorld()).invokeAll(m -> m.onProjectileHit(event));
    }

    @EventHandler
    public void onDead(EntityDeathEvent event) {
        getWorldMobs(event.getEntity().getWorld()).invokeAll(m -> m.onDeath(event));
    }

    @EventHandler
    public void onKill(EntityDeathEvent event) {
        getWorldMobs(event.getEntity().getWorld()).invokeAll(m -> m.onKill(event));
    }

    @EventHandler
    public void onExplosionPrimeEvent(ExplosionPrimeEvent event) {
        getWorldMobs(event.getEntity().getWorld()).invokeAll(m -> m.onExplosionPrimeEvent(event));
    }

    public void onTargetEvent(EntityTargetEvent event) {
        getWorldMobs(event.getEntity().getWorld()).invokeAll(m -> m.onTargetEvent(event));
    }

    public void onBloodNightEnd(BloodNightEndEvent event) {
        WorldMobs worldMobs = getWorldMobs(event.getWorld());
        worldMobs.invokeAll(SpecialMob::onEnd);
        worldMobs.clear();
    }

    @Override
    public void run() {
        // TODO: Probably split ping into ticks to reduce computation in one tick.
        for (World bloodWorld : nightListener.getBloodWorlds()) {
            getWorldMobs(bloodWorld).invokeAll(SpecialMob::tick);
        }
    }

    @NonNull
    private WorldMobs getWorldMobs(World world) {
        return mobRegistry.computeIfAbsent(world.getName(), k -> new WorldMobs());
    }
    
    private static class WorldMobs {
        Map<UUID, SpecialMob> mobs = new HashMap<>();

        public void invokeIfPresent(Entity entity, Consumer<SpecialMob> invoke) {
            SpecialMob specialMob = mobs.get(entity.getUniqueId());
            if (specialMob != null) {
                invoke.accept(specialMob);
            }
        }

        public void invokeAll(Consumer<SpecialMob> invoke) {
            mobs.values().forEach(invoke);
        }

        public boolean isEmpty() {
            return mobs.isEmpty();
        }

        public SpecialMob put(UUID key, SpecialMob value) {
            return mobs.put(key, value);
        }

        public SpecialMob remove(UUID key) {
            return mobs.remove(key);
        }

        public void clear() {
            mobs.clear();
        }
    }

}
