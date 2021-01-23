package de.eldoria.bloodnight.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;
import org.jetbrains.annotations.NotNull;

public class SomeCommand implements CommandExecutor, Listener {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player p = (Player) commandSender;


        Location baseLoc = p.getLocation();
        int horSize = 2, vertSize = 5;
        Location fire = null;
        for (int x = horSize * (-1); x <= horSize; x++) {
            for (int y = 0; y <= vertSize; y++) {
                Location loc = baseLoc.clone().add(x, y, 0);
                if (Math.abs(x) == vertSize || y == 0 || y == vertSize) loc.getBlock().setType(Material.OBSIDIAN);
                else if (y == 1 && x == 0) fire = loc;
                else loc.getBlock().setType(Material.AIR);
            }
        }
        fire.getBlock().setType(Material.FIRE);
        return true;
    }

    @EventHandler
    public void onServerPing(ServerListPingEvent event) {

    }
}
