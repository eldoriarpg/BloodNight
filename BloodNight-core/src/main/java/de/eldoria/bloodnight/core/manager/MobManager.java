package de.eldoria.bloodnight.core.manager;

import de.eldoria.bloodnight.config.Configuration;
import de.eldoria.bloodnight.config.worldsettings.WorldSettings;
import de.eldoria.bloodnight.config.worldsettings.deathactions.subsettings.ShockwaveSettings;
import de.eldoria.bloodnight.config.worldsettings.mobsettings.MobSetting;
import de.eldoria.bloodnight.config.worldsettings.mobsettings.MobSettings;
import de.eldoria.bloodnight.config.worldsettings.mobsettings.VanillaMobSettings;
import de.eldoria.bloodnight.core.BloodNight;
import de.eldoria.bloodnight.core.mobfactory.MobFactory;
import de.eldoria.bloodnight.core.mobfactory.WorldMobFactory;
import de.eldoria.bloodnight.events.BloodNightEndEvent;
import de.eldoria.bloodnight.hooks.mythicmobs.MythicMobUtil;
import de.eldoria.bloodnight.specialmobs.SpecialMob;
import de.eldoria.bloodnight.specialmobs.SpecialMobUtil;
import de.eldoria.eldoutilities.entityutils.ProjectileSender;
import de.eldoria.eldoutilities.entityutils.ProjectileUtil;
import de.eldoria.eldoutilities.threading.IteratingTask;
import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Beehive;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.logging.Level;

public class MobManager implements Listener, Runnable {
    private static final NamespacedKey SPAWNER_SPAWNED = BloodNight.getNamespacedKey("spawnerSpawned");
    private static final NamespacedKey PICKED_UP = BloodNight.getNamespacedKey("pickedUp");
    private final Map<String, WorldMobs> mobRegistry = new HashMap<>();
    private final NightManager nightManager;
    private final Configuration configuration;
    private final ThreadLocalRandom random = ThreadLocalRandom.current();
    private final Map<String, WorldMobFactory> worldFucktories = new HashMap<>();
    private final List<Runnable> executeLater = new ArrayList<>();
    private final List<Runnable> executeNow = new ArrayList<>();
    private final Queue<Entity> lostEntities = new ArrayDeque<>();

    public MobManager(NightManager nightManager, Configuration configuration) {
        this.nightManager = nightManager;
        this.configuration = configuration;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMobSpawn(CreatureSpawnEvent event) {
        if (!nightManager.isBloodNightActive(event.getEntity().getWorld())) return;

        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER
                && configuration.getGeneralSettings().isSpawnerDropSuppression()) {
            PersistentDataContainer data = event.getEntity().getPersistentDataContainer();
            data.set(SPAWNER_SPAWNED, PersistentDataType.BYTE, (byte) 1);
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
        // This is most likely a mounted special mob. We wont change this.
        if (!(entity instanceof LivingEntity)) return;

        // I will not try to wrap a special or mythic mob
        if (SpecialMobUtil.isSpecialMob(entity)) return;
        if (MythicMobUtil.isMythicMob(entity)) return;

        MobSettings mobSettings = configuration.getWorldSettings(entity.getWorld().getName()).getMobSettings();
        Optional<MobSetting> mobSetting = mobSettings.getMobByName(mobFactory.getMobName());

        if (!mobSetting.isPresent()) {
            BloodNight.logger().warning("Tried to create " + mobFactory.getMobName() + " but no settings were present.");
            return;
        }

        SpecialMob<?> specialMob = mobFactory.wrap((LivingEntity) entity, mobSettings, mobSetting.get());

        getWorldMobs(entity.getWorld()).put(entity.getUniqueId(), specialMob);
    }

    private WorldMobFactory getWorldMobFactory(World world) {
        return worldFucktories.computeIfAbsent(world.getName(),
                k -> new WorldMobFactory(configuration.getWorldSettings(world)));
    }

    @Override
    public void run() {
        for (World bloodWorld : nightManager.getBloodWorldsSet()) {
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
            BloodNight.logger().config(String.format("Marked %d lost enties for removal in %dms",
                    stats.getProcessedElements(),
                    stats.getTime()));
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
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Monster) && !(event.getEntity() instanceof Boss)) return;
        if (!configuration.getWorldSettings(event.getEntity().getWorld()).isEnabled()) return;
        addPickupTag(event.getItem().getItemStack());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemPickup(EntityDropItemEvent event) {
        if (!(event.getEntity() instanceof Monster) && !(event.getEntity() instanceof Boss)) return;
        if (!configuration.getWorldSettings(event.getEntity().getWorld()).isEnabled()) return;
        removePickupTag(event.getItemDrop().getItemStack());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityKill(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Player player = event.getEntity().getKiller();

        if (!(entity instanceof Monster || entity instanceof Boss)) return;

        if (!nightManager.isBloodNightActive(entity.getWorld())) {
            event.getDrops().forEach(this::removePickupTag);
            return;
        }

        WorldSettings worldSettings = configuration.getWorldSettings(entity.getWorld());
        ShockwaveSettings shockwaveSettings = worldSettings.getDeathActionSettings().getMobDeathActions().getShockwaveSettings();
        SpecialMobUtil.dispatchShockwave(shockwaveSettings, event.getEntity().getLocation());
        SpecialMobUtil.dispatchLightning(worldSettings.getDeathActionSettings().getMobDeathActions().getLightningSettings(), event.getEntity().getLocation());

        if (configuration.getGeneralSettings().isSpawnerDropSuppression()) {
            if (entity.getPersistentDataContainer().has(SPAWNER_SPAWNED, PersistentDataType.BYTE)) {
                return;
            }
        }

        //TODO: Decide what to do on mythic mob death related to drops
        if (MythicMobUtil.isMythicMob(event.getEntity())) {
            return;
        }

        MobSettings mobSettings = worldSettings.getMobSettings();
        VanillaMobSettings vanillaMobSettings = mobSettings.getVanillaMobSettings();
        event.setDroppedExp((int) (event.getDroppedExp() * mobSettings.getExperienceMultiplier()));

        // Just remove the entity.
        if (player == null) {
            BloodNight.logger().fine("Entity " + entity.getCustomName() + " was not killed by a player.");
            getWorldMobs(event.getEntity().getWorld()).remove(event.getEntity().getUniqueId());
            return;
        }

        if (SpecialMobUtil.isExtension(entity)) {
            BloodNight.logger().finer("Mob is extension. Ignore.");
            return;
        }

        BloodNight.logger().fine("Entity " + entity.getCustomName() + " was killed by " + player.getName());
        BloodNight.logger().fine("Attemt to drop items.");

        if (SpecialMobUtil.isSpecialMob(entity)) {
            if (!mobSettings.isNaturalDrops()) {
                BloodNight.logger().fine("Natural Drops are disabled. Clear loot.");
                event.getDrops().clear();
            } else {
                BloodNight.logger().fine("Natural Drops are enabled. Multiply loot.");
                // Raise amount of drops by multiplier
                for (ItemStack drop : event.getDrops()) {
                    if (isPickedUp(drop)) continue;
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
                BloodNight.logger().finer("Added " + drops.size() + " drops to " + event.getDrops().size() + " drops.");
                event.getDrops().addAll(drops);
            } else {
                BloodNight.logger().config("No mob found for " + specialMob.get() + " in group ");
            }
        } else {
            // If it is a vanilla mob just increase the drops.
            switch (vanillaMobSettings.getVanillaDropMode()) {
                case VANILLA:
                    for (ItemStack drop : event.getDrops()) {
                        if (isPickedUp(drop)) continue;
                        drop.setAmount((int) (drop.getAmount() * vanillaMobSettings.getDropMultiplier()));
                    }
                    break;
                case COMBINE:
                    for (ItemStack drop : event.getDrops()) {
                        if (isPickedUp(drop)) continue;
                        drop.setAmount((int) (drop.getAmount() * vanillaMobSettings.getDropMultiplier()));
                    }
                    event.getDrops().addAll(mobSettings.getDrops(vanillaMobSettings.getExtraDrops()));
                    break;
                case CUSTOM:
                    event.getDrops().clear();
                    event.getDrops().addAll(mobSettings.getDrops(vanillaMobSettings.getExtraDrops()));
                    break;
            }
            // Add extra drops
            if (vanillaMobSettings.getExtraDrops() > 0) {
                List<ItemStack> drops = mobSettings.getDrops(vanillaMobSettings.getExtraDrops());
                event.getDrops().addAll(drops);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityExplode(EntityExplodeEvent event) {
        WorldSettings worldSettings = configuration.getWorldSettings(event.getLocation().getWorld());
        if (!worldSettings.isEnabled()) return;

        if (event.getEntity().getType() != EntityType.CREEPER) return;

        boolean bloodNightActive = nightManager.isBloodNightActive(event.getLocation().getWorld());
        if (worldSettings.isAlwaysManageCreepers() || bloodNightActive) {
            if (!worldSettings.isCreeperBlockDamage()) {
                int size = event.blockList().size();
                event.blockList().clear();
                BloodNight.logger().finest("Explosion is canceled? " + event.isCancelled());
                BloodNight.logger().finest("Prevented " + size + " from destruction");

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
            Optional<SpecialMob<?>> remove = worldMobs.remove(entity.getUniqueId());
            if (SpecialMobUtil.isSpecialMob(entity)) {
                if (remove.isPresent()) {
                    remove.get().remove();
                } else {
                    entity.remove();
                }
            }
        }

        if (!configuration.getGeneralSettings().isBeeFix()) return;
        AtomicInteger hives = new AtomicInteger(0);
        AtomicInteger entites = new AtomicInteger(0);

        // Bugfix for the sin of flying creepers with bees.
        IteratingTask<BlockState> blockStateIterator = new IteratingTask<>(
                Arrays.asList(event.getChunk().getTileEntities()),
                e -> {
                    if (e instanceof Beehive) {
                        Beehive state = (Beehive) e;
                        hives.incrementAndGet();
                        for (Bee entity : state.releaseEntities()) {
                            entites.incrementAndGet();
                            BloodNight.logger().finer("Checking entity with id " + entity.getEntityId() + " and " + entity.getUniqueId().toString());
                            if (SpecialMobUtil.isSpecialMob(entity)) {
                                entity.remove();
                            }
                            return true;
                        }
                        e.update(true);

                        Beehive newState = (Beehive) e.getBlock().getState();
                        if (newState.getEntityCount() != 0) {
                            BloodNight.logger().config("Bee Hive is not empty but should.");
                            BlockData blockData = e.getBlockData();
                            e.getBlock().setType(Material.AIR);
                            e.getBlock().setType(e.getType());
                            e.setBlockData(blockData);

                            newState = (Beehive) e.getBlock().getState();
                            if (newState.getEntityCount() != 0) {
                                BloodNight.logger().config("§cBee Hive is still not empty but should.");
                            } else {
                                BloodNight.logger().config("§2Bee Hive is empty now.");
                            }
                        }
                    }
                    return false;
                },
                s -> {
                    if (hives.get() != 0) {
                        BloodNight.logger().config("Checked " + hives.get() + " Hive/s with " + entites.get() + " Entities and removed " + s.getProcessedElements() + " lost bees in " + s.getTime() + "ms.");
                    }
                });

        blockStateIterator.runTaskTimer(BloodNight.getInstance(), 0, 1);
    }

    @NonNull
    private WorldMobs getWorldMobs(World world) {
        return mobRegistry.computeIfAbsent(world.getName(), k -> new WorldMobs());
    }

    private void addPickupTag(ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) {
            PersistentDataContainer container = itemMeta.getPersistentDataContainer();
            container.set(PICKED_UP, PersistentDataType.BYTE, (byte) 1);
        }
        itemStack.setItemMeta(itemMeta);
    }

    private void removePickupTag(ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) {
            PersistentDataContainer container = itemMeta.getPersistentDataContainer();
            if (container.has(PICKED_UP, PersistentDataType.BYTE))
                container.remove(PICKED_UP);
        }
        itemStack.setItemMeta(itemMeta);
    }

    private boolean isPickedUp(ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) {
            PersistentDataContainer container = itemMeta.getPersistentDataContainer();
            return container.has(PICKED_UP, PersistentDataType.BYTE);
        }
        return false;
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
         * @return special mob if present.
         */
        public Optional<SpecialMob<?>> remove(UUID key) {
            if (!mobs.containsKey(key)) return Optional.empty();
            SpecialMob<?> removed = mobs.remove(key);
            tickQueue.remove(removed);
            removed.remove();
            return Optional.of(removed);
        }

        public void clear() {
            mobs.clear();
            tickQueue.clear();
        }
    }
}
