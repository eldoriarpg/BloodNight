package de.eldoria.bloodnight.command.bloodnight;

import de.eldoria.eldoutilities.simplecommands.EldoCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class Help extends EldoCommand {
    public Help(Plugin plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        messageSender().sendMessage(sender, localizer().getMessage("help.help") + "\n"
                + "§6/bn about§r\n" + localizer().getMessage("help.about") + "\n"
                + "§6/bn forceNight§r\n" + localizer().getMessage("help.forceNight") + "\n"
                + "§6/bn cancelNight§r\n" + localizer().getMessage("help.cancelNight") + "\n"
                + "§6/bn manageMob§r\n" + localizer().getMessage("help.manageMob") + "\n"
                + "§6/bn manageMobs§r\n" + localizer().getMessage("help.manageMobs") + "\n"
                + "§6/bn manageNight§r\n" + localizer().getMessage("help.manageNight") + "\n"
                + "§6/bn manageWorlds§r\n" + localizer().getMessage("help.manageWorlds") + "\n"
                + "§6/bn nightSelection§r\n" + localizer().getMessage("help.nightSelection") + "\n"
                + "§6/bn reload§r\n" + localizer().getMessage("help.reload") + "\n"
                + "§6/bn spawnMob§r\n" + localizer().getMessage("help.spawnMob")
        );
        return true;
    }

    @Override
    public @Nullable
    List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
