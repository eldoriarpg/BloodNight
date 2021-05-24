package de.eldoria.bloodnight.bloodmob.registry.items;

import de.eldoria.eldoutilities.container.Pair;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Simplified item without usage of the bukkit api.
 */
public class SimpleItem {
    private int id;
    private Material type;
    private Map<String, Integer> something;
    private List<String> lore;
    private String displayName;

    public SimpleItem() {
    }

    public SimpleItem(int id, @NotNull Material type, @NotNull Map<String, Integer> something,
                      @NotNull List<String> lore, String displayName) {
        this.id = id;
        this.type = type;
        this.something = something;
        this.lore = lore;
        this.displayName = displayName;
    }

    public static SimpleItem from(int id, ItemStack stack) {
        Material type = stack.getType();
        int amount = stack.getAmount();
        ItemMeta itemMeta = stack.getItemMeta();

        Map<String, Integer> enchantments = stack.getEnchantments()
                .entrySet()
                .stream()
                .map(entry -> Pair.of(entry.getKey().getKey().getKey(), entry.getValue()))
                .collect(Collectors.toMap(entry -> entry.first, entry -> entry.second));
        List<String> lore = new ArrayList<>();
        String displayName = null;
        if (itemMeta != null) {
            if (itemMeta.hasLore()) lore = itemMeta.getLore();
            if (itemMeta.hasDisplayName()) displayName = itemMeta.getDisplayName();
        }
        return new SimpleItem(id, type, enchantments, lore, displayName);
    }

    public int id() {
        return id;
    }

    public Material type() {
        return type;
    }

    public Map<String, Integer> enchantments() {
        return something;
    }

    public List<String> lore() {
        return lore;
    }

    public String displayName() {
        return displayName;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SimpleItem that = (SimpleItem) o;

        if (id != that.id) return false;
        if (type != that.type) return false;
        if (something != null ? !something.equals(that.something) : that.something != null) return false;
        if (lore != null ? !lore.equals(that.lore) : that.lore != null) return false;
        return displayName != null ? displayName.equals(that.displayName) : that.displayName == null;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (something != null ? something.hashCode() : 0);
        result = 31 * result + (lore != null ? lore.hashCode() : 0);
        result = 31 * result + (displayName != null ? displayName.hashCode() : 0);
        return result;
    }
}
