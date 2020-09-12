package de.eldoria.bloodnight.specialmobs.mobs.zombie;

import org.bukkit.Material;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

public class ArmoredZombie extends AbstractZombie {
    protected ArmoredZombie(Zombie zombie) {
        super(zombie);
        EntityEquipment equipment = zombie.getEquipment();
        equipment.setHelmet(new ItemStack(Material.DIAMOND_HELMET));
        equipment.setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
        equipment.setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
        equipment.setBoots(new ItemStack(Material.DIAMOND_BOOTS));
        zombie.setAbsorptionAmount(zombie.getHealth());
    }
}
