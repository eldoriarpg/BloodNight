package de.eldoria.bloodnight.specialmob.settings;

import de.eldoria.eldoutilities.serialization.SerializationUtil;
import de.eldoria.eldoutilities.serialization.TypeResolvingMap;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class Equipment implements ConfigurationSerializable {
    private ItemStack mainHand = null;
    private ItemStack offHand = null;
    private ItemStack helmet = null;
    private ItemStack chestplate = null;
    private ItemStack leggings = null;
    private ItemStack boots = null;

    public Equipment() {
    }

    public Equipment(Map<String, Object> objectMap) {
        TypeResolvingMap map = SerializationUtil.mapOf(objectMap);
        mainHand = map.getValueOrDefault("mainHand", mainHand);
        mainHand = map.getValueOrDefault("offHand", offHand);
        mainHand = map.getValueOrDefault("helmet", helmet);
        mainHand = map.getValueOrDefault("chestplate", chestplate);
        mainHand = map.getValueOrDefault("leggings", leggings);
        mainHand = map.getValueOrDefault("boots", boots);
    }

    /**
     * Creates a Map representation of this class.
     * <p>
     * This class must provide a method to restore this class, as defined in
     * the {@link ConfigurationSerializable} interface javadocs.
     *
     * @return Map containing the current state of this class
     */
    @NotNull
    @Override
    public Map<String, Object> serialize() {
        return SerializationUtil.newBuilder()
                .add("mainHand", mainHand)
                .add("offHand", offHand)
                .add("helmet", helmet)
                .add("chestplate", chestplate)
                .add("leggings", leggings)
                .add("boots", boots)
                .build();
    }

    public void apply(LivingEntity entity) {
        EntityEquipment equipment = entity.getEquipment();
        if (equipment == null) return;
        equipment.setItemInMainHand(mainHand);
        equipment.setItemInOffHand(offHand);
        equipment.setHelmet(helmet);
        equipment.setChestplate(chestplate);
        equipment.setLeggings(leggings);
        equipment.setBoots(boots);

    }
}
