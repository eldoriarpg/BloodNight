package de.eldoria.bloodnight.command.bloodnight;

import de.eldoria.bloodnight.command.bloodnight.managedeathactions.ManageMonsterDeathActions;
import de.eldoria.bloodnight.command.bloodnight.managedeathactions.ManagePlayerDeathActions;
import de.eldoria.bloodnight.config.Configuration;
import de.eldoria.bloodnight.util.Permissions;
import de.eldoria.eldoutilities.conversation.ConversationRequester;
import de.eldoria.eldoutilities.simplecommands.EldoCommand;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class ManageDeathActions extends EldoCommand {
    private final Configuration configuration;
    private final ConversationRequester conversationRequester;

    public ManageDeathActions(Plugin plugin, Configuration configuration) {
        super(plugin);
        this.configuration = configuration;
        this.conversationRequester = ConversationRequester.start(plugin);

        BukkitAudiences bukkitAudiences = BukkitAudiences.create(getPlugin());

        registerCommand("monster", new ManageMonsterDeathActions(plugin, configuration, bukkitAudiences));
        registerCommand("player", new ManagePlayerDeathActions(plugin, configuration, conversationRequester, bukkitAudiences));
    }

    // <monster|player> <world> <field> <value>
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (denyConsole(sender)) {
            return true;
        }

        if (denyAccess(sender, Permissions.MANAGE_DEATH_ACTION)) {
            return true;
        }

        if (argumentsInvalid(sender, args, 1, "<monster|player> <$syntax.worldName$> [<$syntax.field$> <$syntax.value$>]")) {
            return true;
        }

        return super.onCommand(sender, command, label, args);
    }
}