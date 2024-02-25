package de.eldoria.bloodnight.command;

import de.eldoria.bloodnight.command.bloodnight.About;
import de.eldoria.bloodnight.command.bloodnight.CancelNight;
import de.eldoria.bloodnight.command.bloodnight.ForceNight;
import de.eldoria.bloodnight.command.bloodnight.Help;
import de.eldoria.bloodnight.command.bloodnight.ManageDeathActions;
import de.eldoria.bloodnight.command.bloodnight.ManageMob;
import de.eldoria.bloodnight.command.bloodnight.ManageMobs;
import de.eldoria.bloodnight.command.bloodnight.ManageNight;
import de.eldoria.bloodnight.command.bloodnight.ManageNightSelection;
import de.eldoria.bloodnight.command.bloodnight.ManageWorlds;
import de.eldoria.bloodnight.command.bloodnight.Reload;
import de.eldoria.bloodnight.command.bloodnight.SpawnMob;
import de.eldoria.bloodnight.config.Configuration;
import de.eldoria.bloodnight.core.manager.mobmanager.MobManager;
import de.eldoria.bloodnight.core.manager.nightmanager.NightManager;
import de.eldoria.bloodnight.util.Permissions;
import de.eldoria.eldoutilities.commands.command.AdvancedCommand;
import de.eldoria.eldoutilities.commands.command.CommandMeta;
import de.eldoria.eldoutilities.commands.defaultcommands.DefaultAbout;
import de.eldoria.eldoutilities.commands.defaultcommands.DefaultDebug;
import de.eldoria.eldoutilities.simplecommands.commands.DefaultDebug;
import org.bukkit.plugin.Plugin;

public class BloodNightCommand extends AdvancedCommand {

    public BloodNightCommand(Configuration configuration, Plugin plugin,
                             NightManager nightManager, MobManager mobManager, InventoryListener inventoryListener) {
        super(plugin);
        Help help = new Help(plugin);
        meta(CommandMeta.builder("bloodnight")
                .withDefaultCommand(help)
                .withSubCommand(help)
                .withSubCommand(new DefaultAbout(plugin, "https://bn.discord.eldoria.de"))
                .withSubCommand(new SpawnMob(plugin, nightManager, mobManager))
                .withSubCommand(new CancelNight(plugin, nightManager, configuration))
                .withSubCommand("forceNight", new ForceNight(plugin, nightManager, configuration))
                .withSubCommand("manageWorlds", new ManageWorlds(plugin, configuration))
                .withSubCommand("manageMob", new ManageMob(plugin, configuration, inventoryListener))
                .withSubCommand("manageNight", new ManageNight(plugin, configuration))
                .withSubCommand("manageMobs", new ManageMobs(plugin, configuration, inventoryListener))
                .withSubCommand("nightSelection", new ManageNightSelection(plugin, configuration, inventoryListener))
                .withSubCommand(new ManageDeathActions(plugin, configuration))
                .withSubCommand(new Reload(plugin))
                .withSubCommand(new DefaultDebug(plugin, Permissions.Admin.RELOAD))
        );
    }
}
