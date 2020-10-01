package de.eldoria.bloodnight.command;

import de.eldoria.bloodnight.config.Configuration;
import de.eldoria.bloodnight.core.BloodNight;
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
            configuration.safeConfig();
            BloodNight.logger().info("inventory closed");
        } else {
            BloodNight.logger().info("unregistered inventory closed");
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (inventories.containsKey(event.getWhoClicked().getUniqueId())) {
            inventories.get(event.getWhoClicked().getUniqueId()).onInventoryClick(event);
        }
    }

    public static interface InventoryActionHandler {
        public void onInventoryClose(InventoryCloseEvent event);

        public void onInventoryClick(InventoryClickEvent event);
    }
}
