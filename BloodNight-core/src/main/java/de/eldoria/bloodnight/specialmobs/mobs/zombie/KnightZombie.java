package de.eldoria.bloodnight.specialmobs.mobs.zombie;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;


public class KnightZombie extends AbstractZombie {

    public KnightZombie(Zombie zombie){
        super(zombie);
        EntityEquipment equipment = zombie.getEquipment();
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
        if (e.getEntity() instanceof Player) {
            if (ThreadLocalRandom.current().nextDouble() <= 0.4) { //changes the chance to increase/decrease the custom push
                e.setCancelled(true);
                Player player = (Player) e.getEntity();
                player.getVelocity().add(new Vector(0, 2, 0));   //pushes the player up two blocks in the air
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 1, false, false));
            }
        }
    }
    @Override
    public void onDamageByEntity(@NotNull EntityDamageByEntityEvent e){
        if (e.getDamager() instanceof Player) {
            if ((ThreadLocalRandom.current().nextDouble() <= 0.2)) { //changes the chance to increase/decrease the block the attack
                e.setCancelled(true); //blocks the attack.
                Player player = (Player) e.getDamager();
                player.playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_STEP, 10, 3);
            }
        }
    }
}