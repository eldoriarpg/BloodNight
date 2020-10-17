package de.eldoria.bloodnight.command.bloodnight;

import de.eldoria.bloodnight.core.BloodNight;
import de.eldoria.bloodnight.util.Permissions;
import de.eldoria.eldoutilities.localization.Localizer;
import de.eldoria.eldoutilities.messages.MessageSender;
import de.eldoria.eldoutilities.simplecommands.EldoCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class Reload extends EldoCommand {

    public Reload(Localizer localizer, MessageSender messageSender) {
        super(localizer, messageSender);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (denyAccess(sender, Permissions.RELOAD)) {
            return true;
        }
        BloodNight.getInstance().onReload();
        messageSender().sendMessage(sender, localizer().getMessage("reload.success"));
        return true;
    }
}