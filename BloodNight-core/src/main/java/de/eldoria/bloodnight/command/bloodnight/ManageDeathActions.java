package de.eldoria.bloodnight.command.bloodnight;

import de.eldoria.bloodnight.command.bloodnight.managedeathactions.ManageMonsterDeathActions;
import de.eldoria.bloodnight.command.bloodnight.managedeathactions.ManagePlayerDeathActions;
import de.eldoria.bloodnight.config.Configuration;
import de.eldoria.bloodnight.util.Permissions;
import de.eldoria.eldoutilities.commands.command.AdvancedCommand;
import de.eldoria.eldoutilities.commands.command.CommandMeta;
import org.bukkit.plugin.Plugin;

public class ManageDeathActions extends AdvancedCommand {

    public ManageDeathActions(Plugin plugin, Configuration configuration) {
        super(plugin);
        meta(CommandMeta.builder("manageDeathActions")
                .allowPlayer()
                .withPermission(Permissions.Admin.MANAGE_DEATH_ACTION)
                .withSubCommand(new ManageMonsterDeathActions(plugin, configuration))
                .withSubCommand(new ManagePlayerDeathActions(plugin, configuration))
                .build());
    }
}
