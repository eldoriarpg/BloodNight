package de.eldoria.bloodnight.command.bloodnight;

import de.eldoria.eldoutilities.commands.command.AdvancedCommand;
import de.eldoria.eldoutilities.commands.command.CommandMeta;
import de.eldoria.eldoutilities.commands.command.util.Arguments;
import de.eldoria.eldoutilities.commands.exceptions.CommandException;
import de.eldoria.eldoutilities.commands.executor.ITabExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import static de.eldoria.eldoutilities.localization.ILocalizer.escape;

public class Help extends AdvancedCommand implements ITabExecutor {
    public Help(Plugin plugin) {
        super(plugin, CommandMeta.builder("help").build());
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String alias, @NotNull Arguments args) throws CommandException {
        messageSender().sendMessage(sender, """
                %s
                <field>/bn about<default>
                %s
                <field>/bn forceNight<default>
                %s
                <field>/bn cancelNight<default>
                %s
                <field>/bn manageMob<default>
                %s
                <field>/bn manageMobs<default>
                %s
                <field>/bn manageNight<default>
                %s
                <field>/bn manageWorlds<default>
                %s
                <field>/bn nightSelection<default>
                %s
                <field>/bn reload<default>
                %s
                <field>/bn spawnMob<default>
                %s
                """.stripIndent()
                .formatted(
                        escape("help.help"),
                        escape("help.about"),
                        escape("help.forceNight"),
                        escape("help.cancelNight"),
                        escape("help.manageMob"),
                        escape("help.manageMobs"),
                        escape("help.manageNight"),
                        escape("help.manageWorlds"),
                        escape("help.nightSelection"),
                        escape("help.reload"),
                        escape("help.spawnMob")
                )
        );

    }
}
