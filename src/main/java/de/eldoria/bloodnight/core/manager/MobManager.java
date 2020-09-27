package de.eldoria.bloodnight.core.manager;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.eldoria.bloodnight.config.Configuration;
import de.eldoria.bloodnight.config.MobSettings;
import de.eldoria.bloodnight.config.NightSettings;
import de.eldoria.bloodnight.core.BloodNight;
import de.eldoria.bloodnight.core.mobfactory.MobFactory;
import de.eldoria.bloodnight.core.mobfactory.WorldMobFactory;
import de.eldoria.bloodnight.core.events.BloodNightEndEvent;
import de.eldoria.bloodnight.listener.util.ListenerUtil;
import de.eldoria.bloodnight.listener.util.ProjectileSender;
import de.eldoria.bloodnight.specialmobs.SpecialMob;
import de.eldoria.bloodnight.specialmobs.SpecialMobUtil;
import lombok.NonNull;
import org.bukkit.World;
import org.bukkit.entity.Boss;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
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
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class MobManager implements Listener, Runnable {
    private final Map<String, WorldMobs> mobRegistry = new HashMap<>();
    private final NightManager nightManager;
    private final Configuration configuration;
    private final ThreadLocalRandom random = ThreadLocalRandom.current();
    private final Map<String, WorldMobFactory> worldFucktories = new HashMap<>();

    private final List<Runnable> executeLater = new ArrayList<>();
    private final List<Runnable> executeNow = new ArrayList<>();

    public MobManager(NightManager nightManager, Configuration configuration) {
        this.nightManager = nightManager;
        this.configuration = configuration;
    }

    @EventHandler
    public void onMobspawn(EntitySpawnEvent event) {
        if (!nightManager.isBloodNightActive(event.getEntity().getWorld())) return;
        // This is most likely a mounted special mob. We wont change this.
        if (SpecialMobUtil.isSpecialMob(event.getEntity())) {
            BloodNight.logger().info("Will skip special mob");
            return;
        }

        World world = event.getLocation().getWorld();

        MobSettings mobSettings = configuration.getWorldSettings(world.getName()).getMobSettings();

        Optional<MobFactory> mobFactory = getWorldMobFactory(world).getRandomFactory(event.getEntity());

        if (!mobFactory.isPresent()) {
            BloodNight.logger().info("No mob factory found for " + event.getEntity().getClass().getSimpleName());
            return;
        }

        executeLater.add(() -> wrapMob(event.getEntity(), mobFactory.get()));

        //wrapMob(event.getEntity(), mobFactory.get());
    }

    public void wrapMob(Entity entity, MobFactory mobFactory) {
        if (!(entity instanceof LivingEntity)) return;

        if (SpecialMobUtil.isSpecialMob(entity)) return;

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
        for (World bloodWorld : nightManager.getBloodWorlds()) {
            getWorldMobs(bloodWorld).invokeAll(SpecialMob::tick);
        }

        executeNow.forEach(Runnable::run);
        executeNow.clear();
        executeNow.addAll(executeLater);
        executeLater.clear();
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

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        // If no blood night is active in this world we dont care at all.
        if (!nightManager.isBloodNightActive(event.getDamager().getWorld())) return;

        // Check if the entity is a projectile.
        ProjectileSender sender = ListenerUtil.getProjectileSource(event.getDamager());

        Entity damager = sender.isEntity() ? sender.getEntity() : event.getDamager();
        Entity oponent = event.getEntity();

        NightSettings settings = configuration.getWorldSettings(oponent.getLocation().getWorld().getName()).getNightSettings();

        // Check if opponent a monster or boss. We want also to recude non player damage.
        if (oponent instanceof Monster || oponent instanceof Boss) {
            // the damager is a player. Multiply damage by player multiplier
            event.setDamage(event.getDamage() * settings.getPlayerDamageMultiplier());
        } else if (oponent.getType() == EntityType.PLAYER
                && (damager instanceof Monster || damager instanceof Boss)) {
            event.setDamage(event.getDamage() * settings.getMonsterDamageMultiplier());
        }
    }

    private final Cache<Integer, Player> lastDamage = CacheBuilder.newBuilder().expireAfterAccess(10L, TimeUnit.MINUTES).build();

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {

        if (!nightManager.isBloodNightActive(event.getDamager().getWorld())) return;

        Entity damager = event.getDamager();
        Entity entity = event.getEntity();

        // We just care about monsters and boss monsters
        if (!(entity instanceof Monster || entity instanceof Boss)) return;

        int entityId = entity.getEntityId();

        // Register player on entity
        if (damager instanceof Player) {
            lastDamage.put(entityId, (Player) damager);
            return;
        }

        ProjectileSender sender = ListenerUtil.getProjectileSource(damager);
        if (sender.isEntity() && sender.getEntity() instanceof Player) {
            lastDamage.put(entityId, (Player) sender.getEntity());
            return;
        }

        lastDamage.invalidate(entityId);
    }

    @EventHandler
    public void onEntityKill(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Player player = lastDamage.getIfPresent(entity.getEntityId());
        lastDamage.invalidate(entity.getEntityId());

        if (player == null) {
            return;
        }

        if (!(entity instanceof Monster || entity instanceof Boss)) return;


        if (!nightManager.isBloodNightActive(entity.getWorld())) return;

        NightSettings nightSettings = configuration.getWorldSettings(entity.getWorld()).getNightSettings();

        event.setDroppedExp((int) (event.getDroppedExp() * nightSettings.getExperienceMultiplier()));
        for (ItemStack drop : event.getDrops()) {
            drop.setAmount((int) (drop.getAmount() * nightSettings.getDropMultiplier()));
        }
    }

    @EventHandler
    private void onChunkLoad(ChunkLoadEvent event) {
        WorldMobs worldMobs = getWorldMobs(event.getWorld());
        if (!nightManager.isBloodNightActive(event.getWorld())) {
            for (Entity entity : event.getChunk().getEntities()) {
                SpecialMob remove = worldMobs.remove(entity.getUniqueId());
                if (remove != null) {
                    remove.onEnd();
                }
            }
        }
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
