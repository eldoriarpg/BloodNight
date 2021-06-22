package de.eldoria.bloodnight.bloodmob.registry.items;

import de.eldoria.bloodnight.bloodmob.serialization.annotation.MapProperty;
import de.eldoria.bloodnight.bloodmob.serialization.annotation.NumberProperty;
import de.eldoria.bloodnight.bloodmob.serialization.annotation.Property;
import de.eldoria.bloodnight.bloodmob.serialization.annotation.StringProperty;
import de.eldoria.bloodnight.bloodmob.serialization.value.ValueType;
import de.eldoria.eldoutilities.container.Pair;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Simplified item without usage of the bukkit api.
 */
public class SimpleItem {
    @NumberProperty(name = "", descr = "", max = Integer.MAX_VALUE)
    private int id;
    @StringProperty(name = "", descr = "")
    private String type;
    @MapProperty(name = "", descr = "", key = ValueType.STRING, value = ValueType.NUMBER)
    private Map<String, Integer> enchantment;
    @Property(name = "", descr = "")
    private List<String> lore;
    @StringProperty(name = "", descr = "")
    private String displayName;

    public SimpleItem() {
    }

    public SimpleItem(int id, @NotNull Material type, @NotNull Map<String, Integer> enchantments,
                      @NotNull List<String> lore, String displayName) {
        this.id = id;
        this.type = type.name();
        this.enchantment = enchantments;
        this.lore = lore;
        this.displayName = displayName;
    }

    public static SimpleItem from(int id, ItemStack stack) {
        Material type = stack.getType();
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
        return Material.valueOf(type);
    }

    public Map<String, Integer> enchantments() {
        return enchantment;
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
        if (!Objects.equals(enchantment, that.enchantment)) return false;
        if (!Objects.equals(lore, that.lore)) return false;
        return Objects.equals(displayName, that.displayName);
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (enchantment != null ? enchantment.hashCode() : 0);
        result = 31 * result + (lore != null ? lore.hashCode() : 0);
        result = 31 * result + (displayName != null ? displayName.hashCode() : 0);
        return result;
    }
}
