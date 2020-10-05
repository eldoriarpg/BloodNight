package de.eldoria.bloodnight.specialmobs;

import de.eldoria.bloodnight.core.BloodNight;
import de.eldoria.bloodnight.util.VectorUtil;
import de.eldoria.eldoutilities.serialization.TypeConversion;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
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
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class SpecialMobUtil {
    private static final ThreadLocalRandom RAND = ThreadLocalRandom.current();
    private static final NamespacedKey IS_SPECIAL_MOB = BloodNight.getNamespacedKey("isSpecialMob");
    private static final NamespacedKey IS_MOB_EXTENSION = BloodNight.getNamespacedKey("isMobExtension");
    private static final NamespacedKey BASE_UUID = BloodNight.getNamespacedKey("baseUUID");
    private static final NamespacedKey MOB_TYPE = BloodNight.getNamespacedKey("mobType");

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
        tagExtension(rider, carrier);
        carrier.addPassenger(rider);
        return rider;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Entity> T spawnAndMount(EntityType carrierType, Entity rider) {
        T carrier = spawnAndTagEntity(rider.getLocation(), carrierType);
        assert carrier == null;
        tagExtension(carrier, rider);
        carrier.addPassenger(rider);
        return carrier;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Entity> T spawnAndTagEntity(Location location, EntityType entityType) {
        Entity entity = location.getWorld().spawnEntity(location, entityType);
        assert entity == null;
        tagSpecialMob(entity);
        return (T) entity;
    }

    /**
     * Marks a entity as a special mob extension
     *
     * @param entity   entity to mark
     * @param extended the entity which is extended
     */
    public static void tagExtension(Entity entity, Entity extended) {
        PersistentDataContainer dataContainer = entity.getPersistentDataContainer();
        dataContainer.set(IS_MOB_EXTENSION, PersistentDataType.BYTE, (byte) 1);
        dataContainer.set(BASE_UUID, PersistentDataType.BYTE_ARRAY,
                TypeConversion.getBytesFromUUID(extended.getUniqueId()));
    }

    /**
     * Checks if a entity is a extension of a special mob.
     *
     * @param entity entity to check
     * @return true if the entity is a extension
     */
    public static boolean isExtension(Entity entity) {
        PersistentDataContainer dataContainer = entity.getPersistentDataContainer();
        if (dataContainer.has(IS_MOB_EXTENSION, PersistentDataType.BYTE)) {
            Byte specialMob = dataContainer.get(BASE_UUID, PersistentDataType.BYTE);
            return specialMob != null && specialMob == (byte) 1;
        }
        return false;
    }

    /**
     * Get the UUID of the base mob.
     * This will only return a UUID if {@link #isExtension(Entity)} returns true
     *
     * @param entity entity to check
     * @return returns a uuid if the mob is a extension.
     */
    public static Optional<UUID> getBaseUUID(Entity entity) {
        PersistentDataContainer dataContainer = entity.getPersistentDataContainer();
        if (dataContainer.has(IS_MOB_EXTENSION, PersistentDataType.BYTE)) {
            byte[] specialMob = dataContainer.get(BASE_UUID, PersistentDataType.BYTE_ARRAY);
            return Optional.of(TypeConversion.getUUIDFromBytes(specialMob));
        }
        return Optional.empty();
    }

    public static void setSpecialMobType(Entity entity, String type) {
        PersistentDataContainer dataContainer = entity.getPersistentDataContainer();
        dataContainer.set(MOB_TYPE, PersistentDataType.STRING, type);
    }

    public static Optional<String> getSpecialMobType(Entity entity) {
        PersistentDataContainer dataContainer = entity.getPersistentDataContainer();
        if (dataContainer.has(MOB_TYPE, PersistentDataType.STRING)) {
            return Optional.ofNullable(dataContainer.get(MOB_TYPE, PersistentDataType.STRING));
        }
        return Optional.empty();
    }

    /**
     * Tags an entity as special mob.
     * <p>This also sets {@link Entity#setPersistent(boolean)} to {@code false}
     * and {@link LivingEntity#setRemoveWhenFarAway(boolean)} to {@code true}
     *
     * @param entity entity to tag
     */
    public static void tagSpecialMob(Entity entity) {
        PersistentDataContainer dataContainer = entity.getPersistentDataContainer();
        dataContainer.set(IS_SPECIAL_MOB, PersistentDataType.BYTE, (byte) 1);
        if (entity instanceof LivingEntity) {
            ((LivingEntity) entity).setRemoveWhenFarAway(true);
        }
        entity.setPersistent(false);
    }

    public static boolean isSpecialMob(Entity entity) {
        PersistentDataContainer dataContainer = entity.getPersistentDataContainer();
        if (dataContainer.has(IS_SPECIAL_MOB, PersistentDataType.BYTE)) {
            Byte specialMob = dataContainer.get(IS_SPECIAL_MOB, PersistentDataType.BYTE);
            return specialMob != null && specialMob == (byte) 1;
        }
        return false;
    }
}
