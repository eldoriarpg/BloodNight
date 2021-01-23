package de.eldoria.bloodnight.command.bloodnight;

import de.eldoria.eldoutilities.localization.Replacement;
import de.eldoria.eldoutilities.simplecommands.EldoCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;

public class About extends EldoCommand {

    public About(Plugin plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        PluginDescriptionFile descr = getPlugin().getDescription();
        String info = localizer().getMessage("about",
                Replacement.create("PLUGIN_NAME", "Blood Night").addFormatting('b'),
                Replacement.create("AUTHORS", String.join(", ", descr.getAuthors())).addFormatting('b'),
                Replacement.create("VERSION", descr.getVersion()).addFormatting('b'),
                Replacement.create("WEBSITE", descr.getWebsite()).addFormatting('b'),
                Replacement.create("DISCORD", "https://discord.gg/3bYny67").addFormatting('b'));
        messageSender().sendMessage(sender, info);
        return true;
    }
}
