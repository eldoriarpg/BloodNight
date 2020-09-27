package de.eldoria.bloodnight.command;

import de.eldoria.bloodnight.command.bloodnight.About;
import de.eldoria.bloodnight.command.bloodnight.CancelNight;
import de.eldoria.bloodnight.command.bloodnight.ForceNight;
import de.eldoria.bloodnight.command.bloodnight.Help;
import de.eldoria.bloodnight.command.bloodnight.ManageMob;
import de.eldoria.bloodnight.command.bloodnight.ManageMobs;
import de.eldoria.bloodnight.command.bloodnight.ManageNight;
import de.eldoria.bloodnight.command.bloodnight.Reload;
import de.eldoria.bloodnight.command.bloodnight.SetWorldState;
import de.eldoria.bloodnight.command.bloodnight.SpawnMob;
import de.eldoria.bloodnight.config.Configuration;
import de.eldoria.bloodnight.core.manager.MobManager;
import de.eldoria.bloodnight.core.manager.NightManager;
import de.eldoria.eldoutilities.localization.Localizer;
import de.eldoria.eldoutilities.messages.MessageSender;
import de.eldoria.eldoutilities.simplecommands.EldoCommand;
import org.bukkit.plugin.Plugin;

public class BloodNightCommand extends EldoCommand {

    public BloodNightCommand(Configuration configuration, Localizer localizer, Plugin plugin,
                             NightManager nightManager, MobManager mobManager, InventoryListener inventoryListener) {
        super(localizer, MessageSender.get(plugin));
        MessageSender messageSender = MessageSender.get(plugin);
        Help help = new Help(localizer, messageSender);
        setDefaultCommand(help);
        registerCommand("help", help);
        registerCommand("about", new About(localizer, messageSender, plugin));
        registerCommand("spawnMob", new SpawnMob(localizer, messageSender, nightManager, mobManager));
        registerCommand("cancelNight", new CancelNight(localizer, messageSender, nightManager));
        registerCommand("forceNight", new ForceNight(localizer, messageSender, nightManager));
        registerCommand("setWorldState", new SetWorldState(localizer, messageSender, configuration));
        registerCommand("manageMob", new ManageMob(localizer, messageSender, configuration, inventoryListener));
        registerCommand("manageNight", new ManageNight(localizer, messageSender, configuration));
        registerCommand("manageMobs", new ManageMobs(localizer, messageSender, configuration, inventoryListener));
        registerCommand("reload", new Reload(localizer, messageSender, configuration, nightManager));
    }
}
