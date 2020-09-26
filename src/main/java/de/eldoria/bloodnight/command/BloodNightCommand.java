package de.eldoria.bloodnight.command;

import de.eldoria.bloodnight.command.bloodnight.About;
import de.eldoria.bloodnight.command.bloodnight.CancelNight;
import de.eldoria.bloodnight.command.bloodnight.ForceNight;
import de.eldoria.bloodnight.command.bloodnight.Help;
import de.eldoria.bloodnight.command.bloodnight.ManageNight;
import de.eldoria.bloodnight.command.bloodnight.Reload;
import de.eldoria.bloodnight.command.bloodnight.SetMobState;
import de.eldoria.bloodnight.command.bloodnight.SetWorldState;
import de.eldoria.bloodnight.command.bloodnight.SpawnMob;
import de.eldoria.bloodnight.config.Configuration;
import de.eldoria.bloodnight.listener.MobModifier;
import de.eldoria.bloodnight.listener.NightManager;
import de.eldoria.eldoutilities.localization.Localizer;
import de.eldoria.eldoutilities.localization.Replacement;
import de.eldoria.eldoutilities.messages.MessageSender;
import de.eldoria.eldoutilities.simplecommands.EldoCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BloodNightCommand extends EldoCommand {

    public BloodNightCommand(Configuration configuration, Localizer localizer, Plugin plugin,
                             NightManager nightManager, MobModifier mobModifier) {
        super(localizer, MessageSender.get(plugin));
        MessageSender messageSender = MessageSender.get(plugin);
        Help help = new Help(localizer, messageSender);
        setDefaultCommand(help);
        registerCommand("help", help);
        registerCommand("about", new About(localizer, messageSender, plugin));
        registerCommand("spawnMob", new SpawnMob(localizer, messageSender, nightManager, mobModifier));
        registerCommand("cancelNight", new CancelNight(localizer, messageSender, nightManager));
        registerCommand("forceNight", new ForceNight(localizer, messageSender, nightManager));
        registerCommand("setMobState", new SetMobState(localizer, messageSender, configuration));
        registerCommand("setWorldState", new SetWorldState(localizer, messageSender, configuration));
        registerCommand("manageNight", new ManageNight(localizer, messageSender, configuration));
        registerCommand("reload", new Reload(localizer, messageSender, configuration, nightManager));
    }
}
