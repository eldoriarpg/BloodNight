package de.eldoria.bloodnight.specialmobs.mobs.zombie;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

import static io.lumine.xikage.mythicmobs.util.RandomUtil.random;

public class KnightZombie extends AbstractZombie {

    public KnightZombie(Zombie zombie){
        super(zombie);
        EntityEquipment equipment = zombie.getEquipment();
        if (equipment != null) {
            equipment.setHelmet(new ItemStack(Material.CHAINMAIL_HELMET));
        }
        if (equipment != null) {
            equipment.setChestplate(new ItemStack(Material.CHAINMAIL_CHESTPLATE));
        }
        if (equipment != null) {
            equipment.setLeggings(new ItemStack(Material.CHAINMAIL_LEGGINGS));
        }
        if (equipment != null) {
            equipment.setBoots(new ItemStack(Material.CHAINMAIL_BOOTS));
        }
    }
    @Override
    public void onDamageByEntity(@NotNull EntityDamageByEntityEvent e){
        if (e.getDamager() instanceof Player) {
            if (e.getEntity() instanceof KnightZombie) {
                int random = ThreadLocalRandom.current().nextInt(10);
                if (random < 2) { //change the number to increase/decrease the frequency.
                    e.setCancelled(true);
                    Player player = (Player) e.getDamager();
                    player.playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_STEP, 10, 3);
                    //blocks the attack.
                }
            }
        }
        if (e.getDamager() instanceof Zombie && e.getEntity() instanceof Player) {
            if (e.getDamager() instanceof KnightZombie) {
                int random = ThreadLocalRandom.current().nextInt(10);
                if (random < 4) { //change the number to increase/decrease the frequency.
                    e.setCancelled(true);
                    Player player = (Player) e.getEntity();
                    player.setVelocity(new Vector(0, 2, 0));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 1, false, false));
                    //pushes them up two blocks in the air
                }
            }
        }
    }
    public void onDeath(@NotNull EntityDeathEvent z) {
        if  (z.getEntity() instanceof KnightZombie ){
            z.setDroppedExp(6);
        }
        LivingEntity entity = z.getEntity();
        if (entity instanceof KnightZombie) {
            double reward1 = 30.0 / 100.0;
            if (random.nextDouble() <= reward1) {
                entity.getLocation().getWorld().dropItem(entity.getLocation(), new ItemStack(Material.IRON_NUGGET, 3));
            }
            double reward2 = 15.0 / 100.0;
            if (random.nextDouble() <= reward2) {
                entity.getLocation().getWorld().dropItem(entity.getLocation(), new ItemStack(Material.EXPERIENCE_BOTTLE, 12));
            }
            double reward3 = 60.0 / 100.0;
            if (random.nextDouble() <= reward3) {
                entity.getLocation().getWorld().dropItem(entity.getLocation(), new ItemStack(Material.ROTTEN_FLESH, 2));
            }
        }
    }
}
