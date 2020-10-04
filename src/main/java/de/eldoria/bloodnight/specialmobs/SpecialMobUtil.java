package de.eldoria.bloodnight.specialmobs;

import de.eldoria.bloodnight.core.BloodNight;
import de.eldoria.bloodnight.util.VectorUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public final class SpecialMobUtil {
    private static final ThreadLocalRandom RAND = ThreadLocalRandom.current();


    private SpecialMobUtil() {
    }

    @Deprecated
    public static void spawnLingeringPotionAt(Location location, PotionEffect potionEffect) {
        spawnPotionAt(location, potionEffect, Material.LINGERING_POTION);
    }

    @Deprecated
    public static void spawnPotionAt(Location location, PotionEffect potionEffect, Material potionType) {
        ItemStack potion = new ItemStack(potionType);
        PotionMeta potionMeta = (PotionMeta) potion.getItemMeta();
        potionMeta.addCustomEffect(potionEffect, false);
        potion.setItemMeta(potionMeta);

        LivingEntity entity = (LivingEntity) location.getWorld().spawnEntity(location.add(0, 1, 0), EntityType.CHICKEN);
        ThrownPotion thrownPotion = entity.launchProjectile(ThrownPotion.class, new Vector(0, -4, 0));
        thrownPotion.setItem(potion);
        entity.remove();
    }

    public static void addPotionEffect(LivingEntity entity, PotionEffectType type, int amplifier, boolean visible) {
        entity.addPotionEffect(new PotionEffect(type, 60 * 20, amplifier, visible, visible));
    }

    public static void spawnParticlesAround(Entity entity, Particle particle, int amount) {
        spawnParticlesAround(entity.getLocation(), particle, amount);
    }

    public static void spawnParticlesAround(Location location, Particle particle, int amount) {
        spawnParticlesAround(location, particle, null, amount);
    }

    public static <T> void spawnParticlesAround(Location location, Particle particle, T data, int amount) {
        World world = location.getWorld();
        assert world != null;
        for (int i = 0; i < amount; i++) {
            if (data != null) {
                world.spawnParticle(particle,
                        location.clone()
                                .add(
                                        RAND.nextDouble(-3, 3),
                                        RAND.nextDouble(-3, 3),
                                        RAND.nextDouble(-3, 3)),
                        1, data);
            } else {
                world.spawnParticle(particle,
                        location.clone()
                                .add(
                                        RAND.nextDouble(-3, 3),
                                        RAND.nextDouble(-3, 3),
                                        RAND.nextDouble(-3, 3)),
                        1);
            }
        }
    }

    public static void spawnEffectArea(Location location, PotionData potionData, int duration) {
        AreaEffectCloud entity = (AreaEffectCloud) location.getWorld().spawnEntity(location, EntityType.AREA_EFFECT_CLOUD);
        entity.setBasePotionData(potionData);
        entity.setDuration(duration * 20);
    }

    public static void launchProjectileOnTarget(Mob source, Class<? extends Projectile> projectile, double speed) {
        launchProjectileOnTarget(source, source.getTarget(), projectile, speed);
    }

    public static void launchProjectileOnTarget(Mob source, Entity target, Class<? extends Projectile> projectile, double speed) {
        if (target != null) {
            Vector vel = VectorUtil.getDirectionVector(source.getLocation(), target.getLocation())
                    .normalize()
                    .multiply(speed);
            source.launchProjectile(projectile, vel);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends Entity> T spawnAndMount(Entity carrier, EntityType riderType) {
        T rider = spawnAndTagEntity(carrier.getLocation(), riderType);
        assert rider == null;
        carrier.addPassenger(rider);
        return rider;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Entity> T spawnAndMount(EntityType carrierType, Entity rider) {
        T carrier = spawnAndTagEntity(rider.getLocation(), carrierType);
        assert carrier == null;
        carrier.addPassenger(rider);
        return carrier;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Entity> T spawnAndTagEntity(Location location, EntityType entityType) {
        Entity entity = location.getWorld().spawnEntity(location, entityType);
        assert entity == null;
        tagEntity(entity);
        return (T) entity;
    }

    public static void tagEntity(Entity entity) {
        PersistentDataContainer dataContainer = entity.getPersistentDataContainer();
        dataContainer.set(BloodNight.getNamespacedKey("specialMob"), PersistentDataType.BYTE, (byte) 1);
        if (entity instanceof LivingEntity) {
            ((LivingEntity) entity).setRemoveWhenFarAway(true);
        }
        entity.setPersistent(false);
    }

    public static void setSpecialMob(Entity entity, String name) {
        PersistentDataContainer dataContainer = entity.getPersistentDataContainer();
        dataContainer.set(BloodNight.getNamespacedKey("specialMobName"), PersistentDataType.STRING, name);
    }

    public static Optional<String> getSpecialMob(Entity entity) {
        PersistentDataContainer dataContainer = entity.getPersistentDataContainer();
        if (dataContainer.has(BloodNight.getNamespacedKey("specialMobName"), PersistentDataType.STRING)) {
            return Optional.ofNullable(dataContainer.get(BloodNight.getNamespacedKey("specialMobName"), PersistentDataType.STRING));
        }
        return Optional.empty();
    }

    public static boolean isSpecialMob(Entity entity) {
        PersistentDataContainer dataContainer = entity.getPersistentDataContainer();
        if (dataContainer.has(BloodNight.getNamespacedKey("specialMob"), PersistentDataType.BYTE)) {
            Byte specialMob = dataContainer.get(BloodNight.getNamespacedKey("specialMob"), PersistentDataType.BYTE);
            return specialMob != null && specialMob == (byte) 1;
        }
        return false;
    }
}
