package de.eldoria.bloodnight.command.bloodnight;

import de.eldoria.bloodnight.config.Configuration;
import de.eldoria.bloodnight.core.manager.NightManager;
import de.eldoria.eldoutilities.localization.Localizer;
import de.eldoria.eldoutilities.messages.MessageSender;
import de.eldoria.eldoutilities.simplecommands.EldoCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class Reload extends EldoCommand {
    private final Configuration configuration;
    private final NightManager nightManager;

    public Reload(Localizer localizer, MessageSender messageSender, Configuration configuration, NightManager nightManager) {
        super(localizer, messageSender);
        this.configuration = configuration;
        this.nightManager = nightManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        configuration.reload();
        nightManager.reload();
        messageSender().sendMessage(sender, localizer().getMessage("reload.success"));
        return true;
    }
}