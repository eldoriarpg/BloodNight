package de.eldoria.bloodnight.command;

import de.eldoria.bloodnight.command.bloodnight.*;
import de.eldoria.bloodnight.config.Configuration;
import de.eldoria.bloodnight.core.manager.mobmanager.MobManager;
import de.eldoria.bloodnight.core.manager.nightmanager.NightManager;
import de.eldoria.bloodnight.util.Permissions;
import de.eldoria.eldoutilities.simplecommands.EldoCommand;
import de.eldoria.eldoutilities.simplecommands.commands.DefaultDebug;
import org.bukkit.plugin.Plugin;

public class BloodNightCommand extends EldoCommand {

    public BloodNightCommand(Configuration configuration, Plugin plugin,
                             NightManager nightManager, MobManager mobManager, InventoryListener inventoryListener) {
        super(plugin);
        Help help = new Help(plugin);
        setDefaultCommand(help);
        registerCommand("help", help);
        registerCommand("about", new About(plugin));
        registerCommand("spawnMob", new SpawnMob(plugin, nightManager, mobManager));
        registerCommand("cancelNight", new CancelNight(plugin, nightManager, configuration));
        registerCommand("forceNight", new ForceNight(plugin, nightManager, configuration));
        registerCommand("manageWorlds", new ManageWorlds(plugin, configuration));
        registerCommand("manageMob", new ManageMob(plugin, configuration, inventoryListener));
        registerCommand("manageNight", new ManageNight(plugin, configuration));
        registerCommand("manageMobs", new ManageMobs(plugin, configuration, inventoryListener));
        registerCommand("nightSelection", new ManageNightSelection(plugin, configuration, inventoryListener));
        registerCommand("deathActions", new ManageDeathActions(plugin, configuration));
        registerCommand("reload", new Reload(plugin));
        registerCommand("debug", new DefaultDebug(plugin, Permissions.Admin.RELOAD));
    }
}
