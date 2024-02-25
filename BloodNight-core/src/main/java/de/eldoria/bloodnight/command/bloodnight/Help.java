package de.eldoria.bloodnight.command.bloodnight;

import de.eldoria.eldoutilities.commands.command.AdvancedCommand;
import de.eldoria.eldoutilities.commands.command.CommandMeta;
import de.eldoria.eldoutilities.commands.command.util.Arguments;
import de.eldoria.eldoutilities.commands.exceptions.CommandException;
import de.eldoria.eldoutilities.commands.executor.ITabExecutor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class Help extends AdvancedCommand implements ITabExecutor {
    public Help(Plugin plugin) {
        super(plugin,CommandMeta.builder("help").build());
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String alias, @NotNull Arguments args) throws CommandException {
        messageSender().sendMessage(sender, """
                $help.help$
                <aqua>/bn about<default>
                $help.about$
                <aqua>/bn forceNight<default>
                $help.forceNight$
                <aqua>/bn cancelNight<default>
                $help.cancelNight$
                <aqua>/bn manageMob<default>
                $help.manageMob$
                <aqua>/bn manageMobs<default>
                $help.manageMobs$
                <aqua>/bn manageNight<default>
                $help.manageNight$
                <aqua>/bn manageWorlds<default>
                $help.manageWorlds$
                <aqua>/bn nightSelection<default>
                $help.nightSelection$
                <aqua>/bn reload<default>
                $help.reload$
                <aqua>/bn spawnMob<default>
                $help.spawnMob$
                """.stripIndent()
        );

    }
}
