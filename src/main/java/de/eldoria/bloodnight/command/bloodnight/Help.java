package de.eldoria.bloodnight.command.bloodnight;

import de.eldoria.eldoutilities.localization.ILocalizer;
import de.eldoria.eldoutilities.messages.MessageSender;
import de.eldoria.eldoutilities.simplecommands.EldoCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class Help extends EldoCommand {
    public Help(ILocalizer localizer, MessageSender messageSender) {
        super(localizer, messageSender);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        messageSender().sendMessage(sender, localizer().getMessage("help.help") + "\n"
                + "§6about§r\n" + localizer().getMessage("help.about") + "\n"
                + "§6forceNight§r\n" + localizer().getMessage("help.forceNight") + "\n"
                + "§6cancelNight§r\n" + localizer().getMessage("help.cancelNight") + "\n"
                + "§6manageMob§r\n" + localizer().getMessage("help.manageMob") + "\n"
                + "§6manageMobs§r\n" + localizer().getMessage("help.manageMobs") + "\n"
                + "§6manageNight§r\n" + localizer().getMessage("help.manageNight") + "\n"
                + "§6manageWorlds§r\n" + localizer().getMessage("help.manageWorlds") + "\n"
                + "§6reload§r\n" + localizer().getMessage("help.reload") + "\n"
                + "§6spawnMob§r\n" + localizer().getMessage("help.spawnMob")
        );
        return true;
    }

    @Override
    public @Nullable
    List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
