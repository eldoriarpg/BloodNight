package de.eldoria.bloodnight.command.bloodnight;

import de.eldoria.bloodnight.config.Configuration;
import de.eldoria.bloodnight.core.manager.nightmanager.NightManager;
import de.eldoria.bloodnight.util.Permissions;
import de.eldoria.eldoutilities.commands.Completion;
import de.eldoria.eldoutilities.commands.command.AdvancedCommand;
import de.eldoria.eldoutilities.commands.command.CommandMeta;
import de.eldoria.eldoutilities.commands.command.util.Argument;
import de.eldoria.eldoutilities.commands.command.util.Arguments;
import de.eldoria.eldoutilities.commands.command.util.CommandAssertions;
import de.eldoria.eldoutilities.commands.exceptions.CommandException;
import de.eldoria.eldoutilities.commands.executor.ITabExecutor;
import de.eldoria.eldoutilities.messages.Replacement;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class CancelNight extends AdvancedCommand implements ITabExecutor {
    private final NightManager nightManager;
    private final Configuration configuration;

    public CancelNight(Plugin plugin, NightManager nightManager, Configuration configuration) {
        super(plugin, CommandMeta.builder("cancelNight")
                .withPermission(Permissions.Admin.CANCEL_NIGHT)
                .addArgument("syntax.worldName", false)
                .build());
        this.nightManager = nightManager;
        this.configuration = configuration;
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String alias, @NotNull Arguments args) throws CommandException {
        World world = null;
        if (sender instanceof Player player) {
            world = player.getWorld();
        } else {
            CommandAssertions.invalidArguments(args, Argument.input("syntax.worldName", true));
        }

        world = args.asWorld(0, world);

        boolean enabled = configuration.getWorldSettings(world).isEnabled();
        CommandAssertions.isTrue(!enabled, "error.worldNotEnabled", Replacement.create("WORLD", world));
        CommandAssertions.isTrue(nightManager.isBloodNightActive(world), "cancelNight.notActive", Replacement.create("WORLD", world));
        nightManager.cancelNight(world);
        messageSender().sendMessage(sender, "cancelNight.canceled",
                Replacement.create("WORLD", world.getName()));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull Arguments args) throws CommandException {
        if (args.sizeIs(1)) {
            return Completion.completeWorlds(args.asString(0));
        }
        return Collections.emptyList();
    }
}
