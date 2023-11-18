package de.eldoria.bloodnight.config.worldsettings.mobsettings;

import de.eldoria.bloodnight.core.BloodNight;
import de.eldoria.eldoutilities.localization.ILocalizer;
import de.eldoria.eldoutilities.serialization.SerializationUtil;
import de.eldoria.eldoutilities.serialization.TypeResolvingMap;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@SerializableAs("bloodNightDrop")
public class Drop implements ConfigurationSerializable {
    private static final NamespacedKey WEIGHT_KEY = BloodNight.getNamespacedKey("dropweight");
    private final ItemStack item;
    private final int weight;

    public Drop(Map<String, Object> objectMap) {
        TypeResolvingMap map = SerializationUtil.mapOf(objectMap);
        item = map.getValue("item");
        removeWeight(item);
        weight = map.getValue("weight");
    }

    public Drop(ItemStack item, int weight) {
        this.item = item;
        removeWeight(item);
        this.weight = weight;
    }

    public static Drop fromItemStack(ItemStack itemStack) {
        if (itemStack == null) return null;
        int weightFromItemStack = getWeightFromItemStack(itemStack);
        return new Drop(removeWeight(itemStack), weightFromItemStack);
    }

    public static void changeWeight(ItemStack item, int change) {
        int currWeight = getWeightFromItemStack(item);
        int newWeight = Math.min(Math.max(currWeight + change, 1), 100);
        setWeight(item, newWeight);
        Pattern weight = getRegexWeight();
        ItemMeta itemMeta = item.getItemMeta();
        List<String> lore = itemMeta.hasLore() ? itemMeta.getLore() : new ArrayList<>();
        if (lore.isEmpty()) {
            lore.add(getWeightString(newWeight));
        } else {
            Matcher matcher = weight.matcher(lore.get(lore.size() - 1));
            if (matcher.find()) {
                lore.set(lore.size() - 1, getWeightString(newWeight));
            } else {
                lore.add(getWeightString(newWeight));
            }
        }
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
    }

    public static ItemStack removeWeight(ItemStack item) {
        Pattern weight = getRegexWeight();
        ItemMeta itemMeta = item.hasItemMeta() ? item.getItemMeta() : Bukkit.getItemFactory().getItemMeta(item.getType());
        List<String> lore = itemMeta.hasLore() ? itemMeta.getLore() : new ArrayList<>();
        if (!lore.isEmpty()) {
            Matcher matcher = weight.matcher(lore.get(lore.size() - 1));
            if (matcher.find()) {
                lore.remove(lore.size() - 1);
            }
        }
        var container = itemMeta.getPersistentDataContainer();
        if (container.has(WEIGHT_KEY, PersistentDataType.INTEGER)) {
            container.remove(WEIGHT_KEY);
        }
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }

    public static int getWeightFromItemStack(ItemStack item) {
        setWeightIfNotSet(item, 1);
        ItemMeta itemMeta = item.hasItemMeta() ? item.getItemMeta() : Bukkit.getItemFactory().getItemMeta(item.getType());
        PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();
        return dataContainer.get(WEIGHT_KEY, PersistentDataType.INTEGER);
    }

    private static Pattern getRegexWeight() {
        return Pattern.compile("§6" + ILocalizer.getPluginLocalizer(BloodNight.class).getMessage("drops.weight") + ":\\s([0-9]+?)");
    }

    private static void setWeight(ItemStack item, int weight) {
        ItemMeta itemMeta = item.hasItemMeta() ? item.getItemMeta() : Bukkit.getItemFactory().getItemMeta(item.getType());
        PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();
        dataContainer.set(WEIGHT_KEY, PersistentDataType.INTEGER, weight);
        item.setItemMeta(itemMeta);
    }

    private static void setWeightIfNotSet(ItemStack item, int weight) {
        ItemMeta itemMeta = item.hasItemMeta() ? item.getItemMeta() : Bukkit.getItemFactory().getItemMeta(item.getType());
        PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();
        if (!dataContainer.has(WEIGHT_KEY, PersistentDataType.INTEGER)) {
            setWeight(item, weight);
        }
    }

    private static String getWeightString(int weight) {
        return "§6" + ILocalizer.getPluginLocalizer(BloodNight.class).getMessage("drops.weight") + ": " + weight;
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        removeWeight(item);
        return SerializationUtil.newBuilder()
                .add("item", item)
                .add("weight", weight)
                .build();
    }

    public ItemStack getWeightedItem() {
        ItemStack newItem = item.clone();
        setWeight(newItem, weight);
        return newItem;
    }

    public ItemStack getItem() {
        ItemMeta itemMeta = item.hasItemMeta() ? item.getItemMeta() : Bukkit.getItemFactory().getItemMeta(item.getType());
        var container = itemMeta.getPersistentDataContainer();
        if (container.has(WEIGHT_KEY, PersistentDataType.INTEGER)) {
            container.remove(WEIGHT_KEY);
        }
        item.setItemMeta(itemMeta);
        return item.clone();
    }

    public ItemStack getItemWithLoreWeight() {
        ItemMeta itemMeta = item.hasItemMeta() ? item.getItemMeta() : Bukkit.getItemFactory().getItemMeta(item.getType());
        List<String> lore = itemMeta.hasLore() ? itemMeta.getLore() : new ArrayList<>();
        lore.add("§6Weight: " + getWeight());
        itemMeta.setLore(lore);
        ItemStack newItem = item.clone();
        newItem.setItemMeta(itemMeta);
        return newItem;
    }
}
