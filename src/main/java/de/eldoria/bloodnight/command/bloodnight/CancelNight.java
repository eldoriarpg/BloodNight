package de.eldoria.bloodnight.command.bloodnight;

import de.eldoria.bloodnight.core.manager.NightManager;
import de.eldoria.eldoutilities.localization.Localizer;
import de.eldoria.eldoutilities.messages.MessageSender;
import de.eldoria.eldoutilities.simplecommands.EldoCommand;
import de.eldoria.eldoutilities.utils.ArrayUtil;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CancelNight extends EldoCommand {
    private final NightManager nightManager;

    public CancelNight(Localizer localizer, MessageSender messageSender, NightManager nightManager) {
        super(localizer, messageSender);
        this.nightManager = nightManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        World world = null;
        if (sender instanceof Player) {
            Player player = (Player) sender;
            world = player.getWorld();
        }

        if (args.length == 1) {
            world = Bukkit.getWorld(args[0]);
            if (world == null) {
                messageSender().sendError(sender, "invalid world");
                return true;
            }
        } else if (world == null) {
            messageSender().sendError(sender, "Invalid world.");
            return true;
        }
        boolean registered = nightManager.isWorldRegistered(world);
        if (!registered) {
            messageSender().sendError(sender, "This world ist not registered.");
            return true;
        }
        if (nightManager.getBloodWorlds().contains(world)) {
            nightManager.cancelNight(world);
            messageSender().sendMessage(sender, "Blood night in " + world.getName() + " was canceled.");
        } else {
            messageSender().sendError(sender, "No blood night active in " + world.getName() + ".");
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            String[] strings = nightManager.getBloodWorlds().stream().map(World::getName).toArray(String[]::new);
            return ArrayUtil.startingWithInArray(args[0], strings).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
