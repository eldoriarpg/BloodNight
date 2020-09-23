package de.eldoria.bloodnight.listener;

import de.eldoria.bloodnight.config.Configuration;
import de.eldoria.bloodnight.config.MobSettings;
import de.eldoria.bloodnight.core.BloodNight;
import de.eldoria.bloodnight.core.mobfactory.MobFactory;
import de.eldoria.bloodnight.core.mobfactory.WorldMobFactory;
import de.eldoria.bloodnight.events.BloodNightEndEvent;
import de.eldoria.bloodnight.specialmobs.SpecialMob;
import de.eldoria.bloodnight.specialmobs.SpecialMobUtil;
import lombok.NonNull;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

public class MobModifier implements Listener, Runnable {
    private final Map<String, WorldMobs> mobRegistry = new HashMap<>();
    private final NightListener nightListener;
    private final Configuration configuration;
    private final ThreadLocalRandom random = ThreadLocalRandom.current();
    private final Map<String, WorldMobFactory> worldFucktories = new HashMap<>();

    public MobModifier(NightListener nightListener, Configuration configuration) {
        this.nightListener = nightListener;
        this.configuration = configuration;
    }

    @EventHandler
    public void onMobspawn(EntitySpawnEvent event) {
        if (!nightListener.isBloodNightActive(event.getEntity().getWorld())) return;
        // This is most likely a mounted special mob. We wont change this.
        if (SpecialMobUtil.isSpecialMob(event.getEntity())) return;

        World world = event.getLocation().getWorld();

        MobSettings mobSettings = configuration.getWorldSettings(world.getName()).getMobSettings();

        Optional<MobFactory> mobFactory = getWorldMobFactory(world).getRandomFactory(event.getEntity());

        if (!mobFactory.isPresent()) return;

        wrapMob(event.getEntity(), mobFactory.get());
    }

    public void wrapMob(Entity entity, MobFactory mobFactory) {
        if (!(entity instanceof LivingEntity)) return;

        SpecialMob specialMob = mobFactory.wrap((LivingEntity) entity);

        entity.setCustomNameVisible(true);
        entity.setCustomName(mobFactory.getDisplayName());

        BloodNight.logger().info("Special Mob " + mobFactory.getDisplayName() + " spawned.");

        getWorldMobs(entity.getWorld()).put(entity.getUniqueId(), specialMob);
    }

    private WorldMobFactory getWorldMobFactory(World world) {
        return worldFucktories.computeIfAbsent(world.getName(),
                k -> new WorldMobFactory(configuration.getWorldSettings(world)));
    }

    @Override
    public void run() {
        // TODO: Probably split ping into ticks to reduce computation in one tick.
        for (World bloodWorld : nightListener.getBloodWorlds()) {
            getWorldMobs(bloodWorld).invokeAll(SpecialMob::tick);
        }
    }

    @EventHandler
    public void onBloodNightEnd(BloodNightEndEvent event) {
        WorldMobs worldMobs = getWorldMobs(event.getWorld());
        worldMobs.invokeAll(SpecialMob::onEnd);
        worldMobs.clear();
    }

    @EventHandler
    public void onEntityTeleport(EntityTeleportEvent event) {
        getWorldMobs(event.getEntity().getWorld()).invokeIfPresent(event.getEntity(), v -> v.onTeleport(event));
    }

    @EventHandler
    public void onProjectileShoot(ProjectileLaunchEvent event) {
        getWorldMobs(event.getEntity().getWorld()).invokeIfPresent(event.getEntity(), v -> v.onProjectileShoot(event));
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        getWorldMobs(event.getEntity().getWorld()).invokeIfPresent(event.getEntity(), m -> m.onProjectileHit(event));
    }

    @EventHandler
    public void onDead(EntityDeathEvent event) {
        getWorldMobs(event.getEntity().getWorld()).invokeIfPresent(event.getEntity(), m -> m.onDeath(event));
    }

    @EventHandler
    public void onKill(EntityDeathEvent event) {
        getWorldMobs(event.getEntity().getWorld()).invokeIfPresent(event.getEntity(), m -> m.onKill(event));
    }

    @EventHandler
    public void onExplosionPrimeEvent(ExplosionPrimeEvent event) {
        getWorldMobs(event.getEntity().getWorld()).invokeIfPresent(event.getEntity(), m -> m.onExplosionPrimeEvent(event));
    }

    @EventHandler
    public void onExplosionEvent(EntityExplodeEvent event) {
        getWorldMobs(event.getEntity().getWorld()).invokeIfPresent(event.getEntity(), m -> m.onExplosionEvent(event));
    }

    @EventHandler
    public void onTargetEvent(EntityTargetEvent event) {
        if (event.getTarget() != null && event.getTarget().getType() != EntityType.PLAYER) return;
        getWorldMobs(event.getEntity().getWorld()).invokeIfPresent(event.getEntity(), m -> m.onTargetEvent(event));
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        getWorldMobs(event.getEntity().getWorld()).invokeIfPresent(event.getEntity(), m -> m.onDamage(event));
    }

    @EventHandler
    public void onDamageByEntity(EntityDamageByEntityEvent event) {
        getWorldMobs(event.getEntity().getWorld()).invokeIfPresent(event.getEntity(), m -> m.onDamageByEntity(event));
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        getWorldMobs(event.getEntity().getWorld()).invokeIfPresent(event.getDamager(), m -> m.onHit(event));
    }

    @NonNull
    private WorldMobs getWorldMobs(World world) {
        return mobRegistry.computeIfAbsent(world.getName(), k -> new WorldMobs());
    }

    private static class WorldMobs {
        private final Map<UUID, SpecialMob> mobs = new HashMap<>();

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

        public void put(UUID key, SpecialMob value) {
            value.tick();
            mobs.put(key, value);
        }

        public SpecialMob remove(UUID key) {
            return mobs.remove(key);
        }

        public void clear() {
            mobs.clear();
        }
    }

}
