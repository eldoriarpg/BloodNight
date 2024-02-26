package de.eldoria.bloodnight.command.bloodnight;

import de.eldoria.eldoutilities.commands.command.AdvancedCommand;
import de.eldoria.eldoutilities.commands.command.CommandMeta;
import de.eldoria.eldoutilities.commands.command.util.Arguments;
import de.eldoria.eldoutilities.commands.exceptions.CommandException;
import de.eldoria.eldoutilities.commands.executor.ITabExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class Help extends AdvancedCommand implements ITabExecutor {
    public Help(Plugin plugin) {
        super(plugin, CommandMeta.builder("help").build());
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String alias, @NotNull Arguments args) throws CommandException {
        messageSender().sendMessage(sender, """
                $help.help$
                <field>/bn about<default>
                $help.about$
                <field>/bn forceNight<default>
                $help.forceNight$
                <field>/bn cancelNight<default>
                $help.cancelNight$
                <field>/bn manageMob<default>
                $help.manageMob$
                <field>/bn manageMobs<default>
                $help.manageMobs$
                <field>/bn manageNight<default>
                $help.manageNight$
                <field>/bn manageWorlds<default>
                $help.manageWorlds$
                <field>/bn nightSelection<default>
                $help.nightSelection$
                <field>/bn reload<default>
                $help.reload$
                <field>/bn spawnMob<default>
                $help.spawnMob$
                """.stripIndent()
        );

    }
}
