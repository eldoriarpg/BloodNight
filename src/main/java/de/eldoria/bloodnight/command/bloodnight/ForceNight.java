package de.eldoria.bloodnight.command.bloodnight;

import de.eldoria.bloodnight.listener.NightManager;
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

public class ForceNight extends EldoCommand {
    private final NightManager nightManager;

    public ForceNight(Localizer localizer, MessageSender messageSender, NightManager nightManager) {
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
        if (!nightManager.getBloodWorlds().contains(world)) {
            nightManager.forceNight(world);
            messageSender().sendMessage(sender, "The next night in " + world.getName() + " will be a blood night.");
        } else {
            messageSender().sendError(sender, "Blood night in " + world.getName() + " is already active.");
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            String[] strings = nightManager.getObservedWorlds().stream().map(World::getName).toArray(String[]::new);
            return ArrayUtil.startingWithInArray(args[0], strings).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

}
