package de.eldoria.bloodnight.specialmobs;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
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

    public static void spawnPotionAt(Location location, PotionEffect potionEffect) {
        ItemStack potion = new ItemStack(Material.LINGERING_POTION);
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
}
