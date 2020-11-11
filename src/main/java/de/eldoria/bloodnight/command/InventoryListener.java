package de.eldoria.bloodnight.command;

import de.eldoria.bloodnight.config.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InventoryListener implements Listener {

    private final Map<UUID, InventoryActionHandler> inventories = new HashMap<>();
    private final Configuration configuration;

    public InventoryListener(Configuration configuration) {
        this.configuration = configuration;
    }

    public void registerModification(Player player, InventoryActionHandler handler) {
        inventories.put(player.getUniqueId(), handler);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (inventories.containsKey(event.getPlayer().getUniqueId())) {
            inventories.remove(event.getPlayer().getUniqueId()).onInventoryClose(event);
            configuration.save();
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (inventories.containsKey(event.getWhoClicked().getUniqueId())) {
            inventories.get(event.getWhoClicked().getUniqueId()).onInventoryClick(event);
        }
    }

    public interface InventoryActionHandler {
        void onInventoryClose(InventoryCloseEvent event);

        void onInventoryClick(InventoryClickEvent event);
    }
}
