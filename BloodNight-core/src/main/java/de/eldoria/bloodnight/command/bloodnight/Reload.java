package de.eldoria.bloodnight.command.bloodnight;

import de.eldoria.bloodnight.core.BloodNight;
import de.eldoria.bloodnight.util.Permissions;
import de.eldoria.eldoutilities.simplecommands.EldoCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class Reload extends EldoCommand {

    public Reload(Plugin plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (denyAccess(sender, Permissions.Admin.RELOAD)) {
            return true;
        }
        BloodNight.getInstance().onReload();
        messageSender().sendMessage(sender, localizer().getMessage("reload.success"));
        BloodNight.logger().info("BloodNight reloaded!");
        return true;
    }
}