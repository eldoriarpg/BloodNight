package de.eldoria.bloodnight.specialmobs.mobs.zombie;

import de.eldoria.bloodnight.specialmobs.SpecialMobUtil;
import org.bukkit.Material;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.time.Instant;

public class InvisibleZombie extends AbstractZombie {
    private Instant lastDamage = Instant.now();

    public InvisibleZombie(Zombie zombie) {
        super(zombie);

        EntityEquipment equipment = zombie.getEquipment();
        equipment.setItemInMainHand(new ItemStack(Material.GOLDEN_SWORD));
        tick();
    }

    @Override
    public void tick() {
        if (lastDamage.isBefore(Instant.now().minusSeconds(10))) {
            SpecialMobUtil.addPotionEffect(getZombie(), PotionEffectType.INVISIBILITY, 1, true);
        }
    }

    @Override
    public void onDamageByEntity(EntityDamageByEntityEvent event) {
        lastDamage = Instant.now();
        getZombie().removePotionEffect(PotionEffectType.INVISIBILITY);
    }
}
