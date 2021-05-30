package de.eldoria.bloodnight.bloodmob.settings;

import de.eldoria.bloodnight.bloodmob.registry.items.ItemRegistry;
import de.eldoria.bloodnight.bloodmob.registry.items.SimpleItem;
import de.eldoria.bloodnight.bloodmob.serialization.annotation.ItemProperty;
import de.eldoria.bloodnight.bloodmob.serialization.annotation.NumberProperty;
import de.eldoria.bloodnight.bloodmob.serialization.annotation.Property;
import de.eldoria.eldoutilities.serialization.SerializationUtil;
import de.eldoria.eldoutilities.serialization.TypeResolvingMap;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class Equipment implements ConfigurationSerializable {
    @ItemProperty(name = "", descr = "")
    private int mainHand = -1;
    @ItemProperty(name = "", descr = "")
    private int offHand = -1;
    @ItemProperty(name = "", descr = "")
    private int helmet = -1;
    @ItemProperty(name = "", descr = "")
    private int chestplate = -1;
    @ItemProperty(name = "", descr = "")
    private int leggings = -1;
    @ItemProperty(name = "", descr = "")
    private int boots = -1;

    public Equipment() {
    }

    public Equipment(Map<String, Object> objectMap) {
        TypeResolvingMap map = SerializationUtil.mapOf(objectMap);
        mainHand = map.getValueOrDefault("mainHand", mainHand);
        offHand = map.getValueOrDefault("offHand", offHand);
        helmet = map.getValueOrDefault("helmet", helmet);
        chestplate = map.getValueOrDefault("chestplate", chestplate);
        leggings = map.getValueOrDefault("leggings", leggings);
        boots = map.getValueOrDefault("boots", boots);
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

    public void apply(LivingEntity entity, ItemRegistry itemRegistry) {
        EntityEquipment equipment = entity.getEquipment();
        if (equipment == null) return;
        equipment.setItemInMainHand(itemRegistry.getItem(mainHand));
        equipment.setItemInOffHand(itemRegistry.getItem(offHand));
        equipment.setHelmet(itemRegistry.getItem(helmet));
        equipment.setChestplate(itemRegistry.getItem(chestplate));
        equipment.setLeggings(itemRegistry.getItem(leggings));
        equipment.setBoots(itemRegistry.getItem(boots));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Equipment equipment = (Equipment) o;

        if (mainHand != equipment.mainHand) return false;
        if (offHand != equipment.offHand) return false;
        if (helmet != equipment.helmet) return false;
        if (chestplate != equipment.chestplate) return false;
        if (leggings != equipment.leggings) return false;
        return boots == equipment.boots;
    }

    @Override
    public int hashCode() {
        int result = mainHand;
        result = 31 * result + offHand;
        result = 31 * result + helmet;
        result = 31 * result + chestplate;
        result = 31 * result + leggings;
        result = 31 * result + boots;
        return result;
    }
}
