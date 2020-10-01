package de.eldoria.bloodnight.command.bloodnight;

import de.eldoria.eldoutilities.localization.Localizer;
import de.eldoria.eldoutilities.messages.MessageSender;
import de.eldoria.eldoutilities.simplecommands.EldoCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class Help extends EldoCommand {
    public Help(Localizer localizer, MessageSender messageSender) {
        super(localizer, messageSender);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        messageSender().sendMessage(sender, "help" + localizer().getMessage("help.help") + "\n"
                + "§6about§r\n" + localizer().getMessage("help.about") + "\n"
                + "§6addWorld§r\n" + localizer().getMessage("help.addWorld") + "\n"
                + "§6removeWorld§r\n" + localizer().getMessage("help.removeWorld") + "\n"
                + "§6setLanguage§r\n" + localizer().getMessage("help.setLanguage") + "\n"
                + "§6forcePhantoms§r\n" + localizer().getMessage("help.forcePhantoms") + "\n"
                + "§6setSkippable§r\n" + localizer().getMessage("help.setSkippable") + "\n"
                + "§6setNightBegin§r\n" + localizer().getMessage("help.setNightBegin") + "\n"
                + "§6setNightEnd§r\n" + localizer().getMessage("help.setNightEnd")
        );
        return true;
    }

}
