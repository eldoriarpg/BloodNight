package de.eldoria.bloodnight.core.manager.mobmanager;

import de.eldoria.bloodnight.config.Configuration;
import de.eldoria.bloodnight.config.worldsettings.WorldSettings;
import de.eldoria.bloodnight.config.worldsettings.deathactions.subsettings.ShockwaveSettings;
import de.eldoria.bloodnight.config.worldsettings.mobsettings.MobSetting;
import de.eldoria.bloodnight.config.worldsettings.mobsettings.MobSettings;
import de.eldoria.bloodnight.config.worldsettings.mobsettings.VanillaDropMode;
import de.eldoria.bloodnight.config.worldsettings.mobsettings.VanillaMobSettings;
import de.eldoria.bloodnight.core.BloodNight;
import de.eldoria.bloodnight.core.manager.nightmanager.NightManager;
import de.eldoria.bloodnight.core.mobfactory.MobFactory;
import de.eldoria.bloodnight.core.mobfactory.WorldMobFactory;
import de.eldoria.bloodnight.hooks.mythicmobs.MythicMobUtil;
import de.eldoria.bloodnight.specialmobs.SpecialMob;
import de.eldoria.bloodnight.specialmobs.SpecialMobUtil;
import de.eldoria.eldoutilities.entityutils.ProjectileSender;
import de.eldoria.eldoutilities.entityutils.ProjectileUtil;
import de.eldoria.eldoutilities.scheduling.DelayedActions;
import de.eldoria.eldoutilities.threading.IteratingTask;
import de.eldoria.eldoutilities.utils.DataContainerUtil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Beehive;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public class MobManager implements Listener {
    private static final NamespacedKey SPAWNER_SPAWNED = BloodNight.getNamespacedKey("spawnerSpawned");
    private static final NamespacedKey PICKED_UP = BloodNight.getNamespacedKey("pickedUp");
    private final NightManager nightManager;
    private final Configuration configuration;
    private final ThreadLocalRandom random = ThreadLocalRandom.current();
    private final Map<String, WorldMobFactory> worldFucktories = new HashMap<>();
    private final SpecialMobManager specialMobManager;
    private final DelayedActions delayedActions;

    public MobManager(NightManager nightManager, Configuration configuration) {
        this.nightManager = nightManager;
        this.configuration = configuration;
        specialMobManager = new SpecialMobManager(nightManager, configuration);
        specialMobManager.runTaskTimer(BloodNight.getInstance(), 100, 1);
        BloodNight.getInstance().registerListener(specialMobManager);
        delayedActions = DelayedActions.start(BloodNight.getInstance());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMobSpawn(CreatureSpawnEvent event) {
        if (!nightManager.isBloodNightActive(event.getEntity().getWorld())) return;

        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER) {
            PersistentDataContainer data = event.getEntity().getPersistentDataContainer();
            data.set(SPAWNER_SPAWNED, PersistentDataType.BYTE, (byte) 1);
            if (configuration.getGeneralSettings().isIgnoreSpawnerMobs()) {
                return;
            }
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

        delayedActions.schedule(() -> specialMobManager.wrapMob(event.getEntity(), mobFactory.get()), 1);
    }


    private WorldMobFactory getWorldMobFactory(World world) {
        return worldFucktories.computeIfAbsent(world.getName(),
                k -> new WorldMobFactory(configuration.getWorldSettings(world)));
    }

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
    public void onEntityKill(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Player player = event.getEntity().getKiller();

        if (!(entity instanceof Monster || entity instanceof Boss)) return;

        if (!nightManager.isBloodNightActive(entity.getWorld())) {
            event.getDrops().forEach(this::removePickupTag);
            return;
        }

        if (configuration.getGeneralSettings().isIgnoreSpawnerMobs()) {
            if (entity.getPersistentDataContainer().has(SPAWNER_SPAWNED, PersistentDataType.BYTE)) {
                return;
            }
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
            specialMobManager.remove(event.getEntity());
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
            VanillaDropMode dropMode = vanillaMobSettings.getVanillaDropMode();
            switch (dropMode) {
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

            if (dropMode != VanillaDropMode.VANILLA && vanillaMobSettings.getExtraDrops() > 0) {
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
        delayedActions.schedule(() -> specialMobManager.remove(event.getEntity()), 1);
    }

    @EventHandler
    private void onChunkLoad(ChunkLoadEvent event) {
        WorldMobs worldMobs = specialMobManager.getWorldMobs(event.getWorld());
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
        new IteratingTask<>(
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
                        BloodNight.logger().fine("Checked " + hives.get() + " Hive/s with " + entites.get() + " Entities and removed " + s.getProcessedElements() + " lost bees in " + s.getTime() + "ms.");
                    }
                }).runTaskTimer(BloodNight.getInstance(), 0, 1);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemPickup(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player) {
            // Remove remaining picked up tags when a player picks up an item.
            // this is a bugfix and can be removed later.
            removePickupTag(event.getItem().getItemStack());
            return;
        }
        if (!configuration.getWorldSettings(event.getEntity().getWorld()).isEnabled()) return;
        addPickupTag(event.getItem().getItemStack());
    }

    public void onHopperPickUp(InventoryPickupItemEvent event) {
        removePickupTag(event.getItem().getItemStack());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemDrop(EntityDropItemEvent event) {
        // Remove the picked up tag from all items dropped by non players.
        if (event.getEntity() instanceof Player) return;
        if (!configuration.getWorldSettings(event.getEntity().getWorld()).isEnabled()) return;
        removePickupTag(event.getItemDrop().getItemStack());
    }

    private void addPickupTag(ItemStack itemStack) {
        DataContainerUtil.setIfAbsent(itemStack, PICKED_UP, PersistentDataType.BYTE, DataContainerUtil.booleanToByte(true));
    }

    private void removePickupTag(ItemStack itemStack) {
        DataContainerUtil.remove(itemStack, PICKED_UP, PersistentDataType.BYTE);
    }

    private boolean isPickedUp(ItemStack itemStack) {
        return DataContainerUtil.hasKey(itemStack, PICKED_UP, PersistentDataType.BYTE);
    }

    public SpecialMobManager getSpecialMobManager() {
        return specialMobManager;
    }
}
