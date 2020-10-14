package de.eldoria.bloodnight.core.manager;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.eldoria.bloodnight.config.Configuration;
import de.eldoria.bloodnight.config.worldsettings.WorldSettings;
import de.eldoria.bloodnight.config.worldsettings.mobsettings.MobSetting;
import de.eldoria.bloodnight.config.worldsettings.mobsettings.MobSettings;
import de.eldoria.bloodnight.config.worldsettings.mobsettings.VanillaMobSettings;
import de.eldoria.bloodnight.core.BloodNight;
import de.eldoria.bloodnight.core.events.BloodNightEndEvent;
import de.eldoria.bloodnight.core.mobfactory.MobFactory;
import de.eldoria.bloodnight.core.mobfactory.WorldMobFactory;
import de.eldoria.bloodnight.specialmobs.SpecialMob;
import de.eldoria.bloodnight.specialmobs.SpecialMobUtil;
import de.eldoria.eldoutilities.entityutils.ProjectileSender;
import de.eldoria.eldoutilities.entityutils.ProjectileUtil;
import de.eldoria.eldoutilities.threading.IteratingTask;
import lombok.NonNull;
import org.bukkit.World;
import org.bukkit.entity.Boss;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;

public class MobManager implements Listener, Runnable {
    private final Map<String, WorldMobs> mobRegistry = new HashMap<>();
    private final NightManager nightManager;
    private final Configuration configuration;
    private final ThreadLocalRandom random = ThreadLocalRandom.current();
    private final Map<String, WorldMobFactory> worldFucktories = new HashMap<>();

    private final List<Runnable> executeLater = new ArrayList<>();
    private final List<Runnable> executeNow = new ArrayList<>();
    private final Cache<Integer, Player> lastDamage = CacheBuilder.newBuilder().expireAfterAccess(10L, TimeUnit.MINUTES).build();

    private final Queue<Entity> lostEntities = new ArrayDeque<>();

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
            return;
        }

        if (mobSettings.getSpawnPercentage() < random.nextInt(101)) {
            return;
        }

        mobSettings.getMobByName(mobFactory.get().getMobName());

        executeLater.add(() -> wrapMob(event.getEntity(), mobFactory.get()));
    }

    public void wrapMob(Entity entity, MobFactory mobFactory) {
        if (!(entity instanceof LivingEntity)) return;

        // I will not try to wrap a special mob
        if (SpecialMobUtil.isSpecialMob(entity)) return;
        MobSettings mobSettings = configuration.getWorldSettings(entity.getWorld().getName()).getMobSettings();
        Optional<MobSetting> mobSetting = mobSettings.getMobByName(mobFactory.getMobName());

        if (!mobSetting.isPresent()) {
            BloodNight.logger().warning("Tried to create " + mobFactory.getMobName() + " but no settings were present.");
            return;
        }

        SpecialMob<?> specialMob = mobFactory.wrap((LivingEntity) entity, mobSettings, mobSetting.get());

        if (BloodNight.isDebug()) {
            BloodNight.logger().info("Special Mob " + mobSetting.get().getDisplayName() + " spawned.");
        }

        getWorldMobs(entity.getWorld()).put(entity.getUniqueId(), specialMob);
    }

    private WorldMobFactory getWorldMobFactory(World world) {
        return worldFucktories.computeIfAbsent(world.getName(),
                k -> new WorldMobFactory(configuration.getWorldSettings(world)));
    }

    @Override
    public void run() {
        for (World bloodWorld : nightManager.getBloodWorlds()) {
            getWorldMobs(bloodWorld).tick(configuration.getGeneralSettings().getMobTick());
        }

        // run runnables which should be executed this tick
        executeNow.forEach(Runnable::run);
        // clear executed runnables
        executeNow.clear();
        // schedule runnables for next tick
        executeNow.addAll(executeLater);
        // clear the queue
        executeLater.clear();

        for (int i = 0; i < Math.min(lostEntities.size(), 10); i++) {
            Entity poll = lostEntities.poll();
            if (poll.isValid()) {
                poll.remove();
            }
        }
    }

    @EventHandler
    public void onBloodNightEnd(BloodNightEndEvent event) {
        WorldMobs worldMobs = getWorldMobs(event.getWorld());
        worldMobs.invokeAll(SpecialMob::onEnd);
        worldMobs.invokeAll(SpecialMob::remove);
        worldMobs.clear();
        IteratingTask<Entity> iteratingTask = new IteratingTask<>(event.getWorld().getEntities(), (e) ->
        {
            if (!(e instanceof LivingEntity)) {
                return false;
            }
            if (SpecialMobUtil.isSpecialMob(e)) {
                lostEntities.add(e);
                return true;
            }
            return false;
        }, stats -> {
            if (BloodNight.isDebug()) {
                BloodNight.logger().info(String.format("Marked %d lost enties for removal in %dms",
                        stats.getProcessedElements(),
                        stats.getTime()));
            }
        });

        iteratingTask.runTaskTimer(BloodNight.getInstance(), 5, 1);
    }

    /*
    START OF EVENT REDIRECTING SECTION
     */
    @EventHandler
    public void onEntityTeleport(EntityTeleportEvent event) {
        getWorldMobs(event.getEntity().getWorld()).invokeIfPresent(event.getEntity(), v -> v.onTeleport(event));
    }

    @EventHandler
    public void onProjectileShoot(ProjectileLaunchEvent event) {
        ProjectileSender projectileSource = ProjectileUtil.getProjectileSource(event.getEntity());
        if (projectileSource.isEntity()) {
            getWorldMobs(event.getEntity().getWorld()).invokeIfPresent(projectileSource.getEntity(), v -> v.onProjectileShoot(event));
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        ProjectileSender projectileSource = ProjectileUtil.getProjectileSource(event.getEntity());
        if (projectileSource.isEntity()) {
            getWorldMobs(event.getEntity().getWorld()).invokeIfPresent(projectileSource.getEntity(), m -> m.onProjectileHit(event));
        }
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        if (SpecialMobUtil.isSpecialMob(event.getEntity())) {
            if (SpecialMobUtil.isExtension(event.getEntity())) {
                Optional<UUID> baseUUID = SpecialMobUtil.getBaseUUID(event.getEntity());
                if (!baseUUID.isPresent()) {
                    return;
                }
                getWorldMobs(event.getEntity().getWorld()).invokeIfPresent(baseUUID.get(), m -> m.onExtensionDeath(event));
            } else {
                getWorldMobs(event.getEntity().getWorld()).invokeIfPresent(event.getEntity(), m -> m.onDeath(event));
            }
        }
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
        if (event.getTarget() != null && SpecialMobUtil.isSpecialMob(event.getTarget())) {
            event.setCancelled(true);
            return;
        }

        if (!SpecialMobUtil.isSpecialMob(event.getEntity())) {
            return;
        }

        // Block that special mobs target something else than players.
        // Otherwise they will probably kill each other.
        if (event.getTarget() != null && event.getTarget().getType() != EntityType.PLAYER) {
            event.setCancelled(true);
            return;
        }
        if (event.getTarget() == null || event.getTarget() instanceof LivingEntity) {
            getWorldMobs(event.getEntity().getWorld()).invokeIfPresent(event.getEntity(), m -> m.onTargetEvent(event));
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (SpecialMobUtil.isSpecialMob(event.getEntity())) {
            if (SpecialMobUtil.isExtension(event.getEntity())) {
                Optional<UUID> baseUUID = SpecialMobUtil.getBaseUUID(event.getEntity());
                if (!baseUUID.isPresent()) {
                    return;
                }
                getWorldMobs(event.getEntity().getWorld()).invokeIfPresent(baseUUID.get(), m -> m.onExtensionDamage(event));
            } else {
                getWorldMobs(event.getEntity().getWorld()).invokeIfPresent(event.getEntity(), m -> m.onDamage(event));
            }
        }
    }

    @EventHandler
    public void onDamageByEntity(EntityDamageByEntityEvent event) {
        if (SpecialMobUtil.isSpecialMob(event.getEntity())) {
            if (SpecialMobUtil.isExtension(event.getEntity())) {
                Optional<UUID> baseUUID = SpecialMobUtil.getBaseUUID(event.getEntity());
                if (!baseUUID.isPresent()) {
                    return;
                }
                getWorldMobs(event.getEntity().getWorld()).invokeIfPresent(baseUUID.get(), m -> m.onDamageByEntity(event));
            } else {
                getWorldMobs(event.getEntity().getWorld()).invokeIfPresent(event.getEntity(), m -> m.onDamageByEntity(event));
            }
        }
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        getWorldMobs(event.getEntity().getWorld()).invokeIfPresent(event.getDamager(), m -> m.onHit(event));
    }

    /*
    END OF EVENT REDIRECTION SECTION
     */

    /*
     * Handling of vanilla mobs damaging to players during a blood night
     */
    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        // If no blood night is active in this world we dont care at all.
        if (!nightManager.isBloodNightActive(event.getDamager().getWorld())) return;

        // Check if the entity is a projectile.
        ProjectileSender sender = ProjectileUtil.getProjectileSource(event.getDamager());

        Entity damager = sender.isEntity() ? sender.getEntity() : event.getDamager();
        Entity oponent = event.getEntity();

        MobSettings settings = configuration.getWorldSettings(oponent.getLocation().getWorld().getName()).getMobSettings();

        // If it is a special mob it already has a custom health.
        if (SpecialMobUtil.isSpecialMob(damager)) return;

        if (oponent.getType() != EntityType.PLAYER) return;

        VanillaMobSettings vanillaMobSettings = settings.getVanillaMobSettings();

        /*
        This is a important section.
        The damage should only be changed on monsters and boss entities not on general entities.
         */
        if (damager instanceof Monster || damager instanceof Boss) {
            // the damager is hostile monster. Multiply damage by damage multiplier
            event.setDamage(event.getDamage() * vanillaMobSettings.getDamageMultiplier());
        }
    }

    /*
    Handling of players dealing damage to entities during blood night.
     */
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {

        if (!nightManager.isBloodNightActive(event.getDamager().getWorld())) return;

        // Check if the entity is a projectile.
        ProjectileSender sender = ProjectileUtil.getProjectileSource(event.getDamager());

        Entity damager = sender.isEntity() ? sender.getEntity() : event.getDamager();
        Entity oponent = event.getEntity();

        if (damager.getType() != EntityType.PLAYER) {
            if (!SpecialMobUtil.isSpecialMob(damager)) {
                lastDamage.invalidate(oponent.getEntityId());
            }
            return;
        }

        int entityId = oponent.getEntityId();

        // We just care about monsters and boss monsters
        if (!(oponent instanceof Monster || oponent instanceof Boss)) return;

        // Reduce damage for vanilla mobs
        if (!SpecialMobUtil.isSpecialMob(oponent)) {
            VanillaMobSettings vanillaMobSettings = configuration.getWorldSettings(oponent.getLocation().getWorld().getName()).getMobSettings().getVanillaMobSettings();
            event.setDamage(event.getDamage() / vanillaMobSettings.getHealthMultiplier());
        }

        // Register player on entity
        lastDamage.put(entityId, (Player) damager);
    }

    @EventHandler
    public void onEntityKill(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Player player = lastDamage.getIfPresent(entity.getEntityId());
        lastDamage.invalidate(entity.getEntityId());


        if (!(entity instanceof Monster || entity instanceof Boss)) return;

        if (!nightManager.isBloodNightActive(entity.getWorld())) return;

        MobSettings mobSettings = configuration.getWorldSettings(entity.getWorld()).getMobSettings();
        VanillaMobSettings vanillaMobSettings = mobSettings.getVanillaMobSettings();
        event.setDroppedExp((int) (event.getDroppedExp() * mobSettings.getExperienceMultiplier()));

        // Just remove the entity.
        if (player == null) {
            if (BloodNight.isDebug()) {
                BloodNight.logger().info("Entity " + entity.getCustomName() + " was not killed by a player.");
            }
            getWorldMobs(event.getEntity().getWorld()).remove(event.getEntity().getUniqueId());
            return;
        }

        if (SpecialMobUtil.isExtension(entity)) {
            if (BloodNight.isDebug()) {
                BloodNight.logger().info("Mob is extension. Ignore.");
            }
            return;
        }

        BloodNight.logger().info("Entity " + entity.getCustomName() + " was killed by " + player.getName());

        BloodNight.logger().info("Attemt to drop items.");

        if (SpecialMobUtil.isSpecialMob(entity)) {
            BloodNight.logger().info("Mob is special mob.");
            if (!mobSettings.isNaturalDrops()) {
                if (BloodNight.isDebug()) {
                    BloodNight.logger().info("Natural Drops are disabled. Clear loot.");
                }
                event.getDrops().clear();
            } else {
                if (BloodNight.isDebug()) {
                    BloodNight.logger().info("Natural Drops are enabled. Multiply loot.");
                }
                // Raise amount of drops by multiplier
                for (ItemStack drop : event.getDrops()) {
                    drop.setAmount((int) (drop.getAmount() * vanillaMobSettings.getDropMultiplier()));
                }
            }

            // add custom drops
            Optional<String> specialMob = SpecialMobUtil.getSpecialMobType(entity);
            if (!specialMob.isPresent()) {
                BloodNight.logger().log(Level.WARNING, "No special type name was received from special mob.", new IllegalStateException());
                return;
            }
            Optional<MobSetting> mobByName = mobSettings.getMobByName(specialMob.get());
            if (mobByName.isPresent()) {
                List<ItemStack> drops = mobSettings.getDrops(mobByName.get());
                if (BloodNight.isDebug()) {
                    BloodNight.logger().info("Added " + drops.size() + " drops to " + event.getDrops().size() + " drops.");
                }
                event.getDrops().addAll(drops);
            } else {
                if (BloodNight.isDebug()) {
                    BloodNight.logger().info("No mob found for " + specialMob.get() + " in group ");
                }
            }
        } else {
            // If it is a vanilla mob just increase the drops.
            switch (vanillaMobSettings.getVanillaDropMode()) {
                case VANILLA:
                    for (ItemStack drop : event.getDrops()) {
                        drop.setAmount((int) (drop.getAmount() * vanillaMobSettings.getDropMultiplier()));
                    }
                    break;
                case COMBINE:
                    for (ItemStack drop : event.getDrops()) {
                        drop.setAmount((int) (drop.getAmount() * vanillaMobSettings.getDropMultiplier()));
                    }
                    event.getDrops().addAll(mobSettings.getDrops(vanillaMobSettings.getDropAmount()));
                    break;
                case CUSTOM:
                    event.getDrops().clear();
                    event.getDrops().addAll(mobSettings.getDrops(vanillaMobSettings.getDropAmount()));
                    break;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityExplode(EntityExplodeEvent event) {
        WorldSettings worldSettings = configuration.getWorldSettings(event.getLocation().getWorld());
        if(!worldSettings.isEnabled()) return;

        if (!worldSettings.isCreeperBlockDamage()) {
            event.blockList().clear();
            if (BloodNight.isDebug()) {
                BloodNight.logger().info("Explosion is canceled? " + event.isCancelled());
                BloodNight.logger().info("Prevented " + event.blockList().size() + " from destruction");
            }
        }
        executeLater.add(() ->
                getWorldMobs(event.getLocation().getWorld())
                        .remove(event.getEntity().getUniqueId()));
    }

    @EventHandler
    private void onChunkLoad(ChunkLoadEvent event) {
        WorldMobs worldMobs = getWorldMobs(event.getWorld());
        for (Entity entity : event.getChunk().getEntities()) {
            SpecialMob<?> remove = worldMobs.remove(entity.getUniqueId());
            if (SpecialMobUtil.isSpecialMob(entity)) {
                remove.remove();
            }
        }
    }

    @NonNull
    private WorldMobs getWorldMobs(World world) {
        return mobRegistry.computeIfAbsent(world.getName(), k -> new WorldMobs());
    }

    private static class WorldMobs {
        private final Map<UUID, SpecialMob<?>> mobs = new HashMap<>();
        private final Queue<SpecialMob<?>> tickQueue = new ArrayDeque<>();

        private double entityTick = 0;

        public void invokeIfPresent(Entity entity, Consumer<SpecialMob<?>> invoke) {
            invokeIfPresent(entity.getUniqueId(), invoke);
        }

        public void invokeIfPresent(UUID uuid, Consumer<SpecialMob<?>> invoke) {
            SpecialMob<?> specialMob = mobs.get(uuid);
            if (specialMob != null) {
                invoke.accept(specialMob);
            }
        }

        public void invokeAll(Consumer<SpecialMob<?>> invoke) {
            mobs.values().forEach(invoke);
        }

        public void tick(int tickDelay) {
            if (tickQueue.isEmpty()) return;
            entityTick += tickQueue.size() / (double) tickDelay;
            while (entityTick > 0) {
                if (tickQueue.isEmpty()) return;
                SpecialMob<?> poll = tickQueue.poll();
                if (!poll.getBaseEntity().isValid()) {
                    remove(poll.getBaseEntity().getUniqueId());
                    poll.remove();
                    if (BloodNight.isDebug()) {
                        BloodNight.logger().info("Removed invalid entity.");
                    }
                } else {
                    poll.tick();
                    tickQueue.add(poll);
                }
                entityTick--;
            }
        }

        public boolean isEmpty() {
            return mobs.isEmpty();
        }

        public void put(UUID key, SpecialMob<?> value) {
            mobs.put(key, value);
            tickQueue.add(value);
        }

        /**
         * Attemts to remove an entity from world mobs and the world.
         *
         * @param key uid of entity
         *
         * @return special mob if present.
         */
        public SpecialMob<?> remove(UUID key) {
            if (!mobs.containsKey(key)) return null;
            SpecialMob<?> removed = mobs.remove(key);
            tickQueue.remove(removed);
            removed.remove();
            return removed;
        }

        public void clear() {
            mobs.clear();
            tickQueue.clear();
        }
    }
}
