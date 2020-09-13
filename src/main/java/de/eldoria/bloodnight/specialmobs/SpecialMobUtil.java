package de.eldoria.bloodnight.specialmobs;

import de.eldoria.bloodnight.util.VectorUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;

public final class SpecialMobUtil {
    private static final ThreadLocalRandom RAND = ThreadLocalRandom.current();


    private SpecialMobUtil() {
    }

    public static void spawnLingeringPotionAt(Location location, PotionEffect potionEffect) {
        spawnPotionAt(location, potionEffect, Material.LINGERING_POTION);
    }

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
        entity.addPotionEffect(new PotionEffect(type, 60, amplifier, visible, visible));
    }

    public static void spawnParticlesAround(Entity entity, Particle particle, int amount) {
        spawnParticlesAround(entity.getLocation(), particle, amount);
    }

    public static void spawnParticlesAround(Location location, Particle particle, int amount) {
        World world = location.getWorld();
        for (int i = 0; i < amount; i++) {
            world.spawnParticle(particle,
                    location.clone()
                            .add(
                                    RAND.nextDouble(-3, 3),
                                    RAND.nextDouble(-3, 3),
                                    RAND.nextDouble(-3, 3)),
                    1);
        }
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
    public static <T extends Entity> T spawnAndMount(Entity carrier, EntityType mount) {
        T mountedEntity = (T) carrier.getLocation().getWorld().spawnEntity(carrier.getLocation(), mount);
        carrier.addPassenger(mountedEntity);
        return mountedEntity;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Entity> T spawnAndMount(EntityType carrier, Entity mount) {
        T mountedEntity = (T) mount.getLocation().getWorld().spawnEntity(mount.getLocation(), carrier);
        mount.addPassenger(mountedEntity);
        return mountedEntity;
    }
}
