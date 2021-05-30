package de.eldoria.bloodnight.bloodmob.registry.items;

import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ItemRegistry {
    private int currentId = 0;
    private Map<Integer, ItemStack> items = new HashMap<>();

    public ItemRegistry() {
    }

    public List<SimpleItem> getAsSimpleItems() {
        return items.entrySet().stream()
                .map(entry -> SimpleItem.from(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    public void register(ItemStack stack) {
        items.put(currentId++, stack);
    }

    public ItemStack getItem(int id) {
        return items.get(id);
    }
}
