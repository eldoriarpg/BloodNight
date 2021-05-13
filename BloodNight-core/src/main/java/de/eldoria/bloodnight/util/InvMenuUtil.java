package de.eldoria.bloodnight.util;

import lombok.experimental.UtilityClass;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

@UtilityClass
public class InvMenuUtil {
    private final String INV_MENU_UTIL = "inv_menu_util";
    private final NamespacedKey BOOLEAN_KEY = new NamespacedKey(INV_MENU_UTIL, "boolean");

    /**
     * Get a item stack with the matching material based on the state.
     * <p>
     * This method will apply a nbt tag which marks this item as a boolean item.
     *
     * @param state current boolean state
     * @return item stack with correct material and boolean nbt tag.
     */
    public ItemStack getBooleanMaterial(boolean state) {
        Material mat = state ? Material.GREEN_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;
        ItemStack itemStack = new ItemStack(mat);

        if (itemStack.hasItemMeta()) {
            ItemMeta itemMeta = itemStack.getItemMeta();
            PersistentDataContainer container = itemMeta.getPersistentDataContainer();
            container.set(BOOLEAN_KEY, PersistentDataType.BYTE, (byte) (state ? 1 : 0));
            itemStack.setItemMeta(itemMeta);
        }

        return itemStack;
    }

    public void toogleBoolean(ItemStack itemStack) {
        if (itemStack.hasItemMeta()) {
            ItemMeta itemMeta = itemStack.getItemMeta();
            PersistentDataContainer container = itemMeta.getPersistentDataContainer();

            if (container.has(BOOLEAN_KEY, PersistentDataType.BYTE)) {
                Byte aByte = container.get(BOOLEAN_KEY, PersistentDataType.BYTE);
                container.set(BOOLEAN_KEY, PersistentDataType.BYTE, (byte) (aByte == (byte) 1 ? 0 : 1));
            }

            itemStack.setItemMeta(itemMeta);
        }
    }

    public boolean getBoolean(ItemStack itemStack) {
        if (itemStack.hasItemMeta()) {
            ItemMeta itemMeta = itemStack.getItemMeta();
            PersistentDataContainer container = itemMeta.getPersistentDataContainer();

            if (container.has(BOOLEAN_KEY, PersistentDataType.BYTE)) {
                return container.get(BOOLEAN_KEY, PersistentDataType.BYTE) == (byte) 1;
            }
        }
        return false;
    }

    public Material getBooleanMat(boolean state) {
        return state ? Material.GREEN_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;
    }
}
