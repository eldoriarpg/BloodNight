package de.eldoria.bloodnight.command.bloodnight;

import de.eldoria.bloodnight.core.BloodNight;
import de.eldoria.bloodnight.util.Permissions;
import de.eldoria.eldoutilities.commands.command.AdvancedCommand;
import de.eldoria.eldoutilities.commands.command.CommandMeta;
import de.eldoria.eldoutilities.commands.command.util.Arguments;
import de.eldoria.eldoutilities.commands.exceptions.CommandException;
import de.eldoria.eldoutilities.commands.executor.IPlayerTabExecutor;
import de.eldoria.eldoutilities.commands.executor.ITabExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class Reload extends AdvancedCommand implements ITabExecutor {

    public Reload(Plugin plugin) {
        super(plugin, CommandMeta.builder("reload").withPermission(Permissions.Admin.RELOAD)
                .build());
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String alias, @NotNull Arguments args) throws CommandException {
        BloodNight.getInstance().onReload();
        messageSender().sendMessage(sender, localizer().getMessage("reload.success"));
        BloodNight.logger().info("BloodNight reloaded!");
    }
}
