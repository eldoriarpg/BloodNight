package de.eldoria.bloodnight.specialmobs.mobs.zombie;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;


public class KnightZombie extends AbstractZombie {

    public KnightZombie(Zombie zombie) {
        super(zombie);
        var equipment = zombie.getEquipment();
        if (equipment != null) {
            equipment.setHelmet(new ItemStack(Material.CHAINMAIL_HELMET, 1));
            equipment.setChestplate(new ItemStack(Material.CHAINMAIL_CHESTPLATE, 1));
            equipment.setLeggings(new ItemStack(Material.CHAINMAIL_LEGGINGS, 1));
            equipment.setBoots(new ItemStack(Material.CHAINMAIL_BOOTS, 1));
            equipment.setItemInMainHand(new ItemStack(Material.GOLDEN_AXE, 1));
        }
    }

    @Override
    public void onHit(@NotNull EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player player) {
            //changes the chance to increase/decrease the custom push
            if (ThreadLocalRandom.current().nextDouble() > 0.15) return;
            e.setCancelled(true);
            //pushes the player up into the air
            player.getVelocity().setY(Math.max(player.getVelocity().getY() + 1, 1));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20, 2, true, true, true));
            player.playSound(player.getLocation(), Sound.BLOCK_BUBBLE_COLUMN_BUBBLE_POP, 15, 3);
        }
    }

    @Override
    public void onDamageByEntity(@NotNull EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player player) {
            //changes the chance to increase/decrease the block the attack
            if ((ThreadLocalRandom.current().nextDouble() > 0.12)) return;
            e.setCancelled(true);
            player.playSound(player.getLocation(), Sound.ITEM_SHIELD_BLOCK, 15, 1);
        }
    }
}
