package de.eldoria.bloodnight.core.manager.mobmanager;

import de.eldoria.bloodnight.config.Configuration;
import de.eldoria.bloodnight.config.worldsettings.mobsettings.MobSetting;
import de.eldoria.bloodnight.config.worldsettings.mobsettings.MobSettings;
import de.eldoria.bloodnight.core.BloodNight;
import de.eldoria.bloodnight.core.manager.nightmanager.NightManager;
import de.eldoria.bloodnight.core.mobfactory.MobFactory;
import de.eldoria.bloodnight.events.BloodNightEndEvent;
import de.eldoria.bloodnight.hooks.mythicmobs.MythicMobUtil;
import de.eldoria.bloodnight.specialmobs.SpecialMob;
import de.eldoria.bloodnight.specialmobs.SpecialMobUtil;
import de.eldoria.eldoutilities.entities.projectiles.ProjectileSender;
import de.eldoria.eldoutilities.entities.projectiles.ProjectileUtil;
import de.eldoria.eldoutilities.threading.IteratingTask;
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
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;

public class SpecialMobManager extends BukkitRunnable implements Listener {
    private final Map<String, WorldMobs> mobRegistry = new HashMap<>();
    private final NightManager nightManager;
    private final Configuration configuration;
    private final Queue<Entity> lostEntities = new ArrayDeque<>();


    public SpecialMobManager(NightManager nightManager, Configuration configuration) {
        this.nightManager = nightManager;
        this.configuration = configuration;
    }

    public void wrapMob(Entity entity, MobFactory mobFactory) {
        // We wont wrap anything dead... Except zombies and all the other dead stuff.
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

        BloodNight.logger().config("Wrapped mob " + entity.getType() + " into " + mobFactory.getMobName());

        SpecialMob<?> specialMob = mobFactory.wrap((LivingEntity) entity, mobSettings, mobSetting.get());

        registerMob(specialMob);
    }

    @Override
    public void run() {
        for (World bloodWorld : nightManager.getBloodWorldsSet()) {
            getWorldMobs(bloodWorld).tick(configuration.getGeneralSettings().mobTick());
        }

        for (int i = 0; i < Math.min(lostEntities.size(), 10); i++) {
            Entity poll = lostEntities.poll();
            if (poll.isValid()) {
                poll.remove();
            }
        }
    }

    @NonNull
    public WorldMobs getWorldMobs(World world) {
        return mobRegistry.computeIfAbsent(world.getName(), k -> new WorldMobs());
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


    public void registerMob(SpecialMob<?> mob) {
        World world = mob.getBaseEntity().getLocation().getWorld();
        getWorldMobs(world).put(mob.getBaseEntity().getUniqueId(), mob);
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
        }, stats -> BloodNight.logger().config(String.format("Marked %d lost enties for removal in %dms",
                stats.getProcessedElements(),
                stats.getTime())));

        iteratingTask.runTaskTimer(BloodNight.getInstance(), 5, 1);
    }

    public void remove(Entity entity) {
        getWorldMobs(entity.getWorld()).remove(entity.getUniqueId());
    }
}
