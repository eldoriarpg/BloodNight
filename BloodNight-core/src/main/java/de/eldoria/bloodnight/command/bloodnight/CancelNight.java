package de.eldoria.bloodnight.command.bloodnight;

import de.eldoria.bloodnight.config.Configuration;
import de.eldoria.bloodnight.core.manager.nightmanager.NightManager;
import de.eldoria.bloodnight.util.Permissions;
import de.eldoria.eldoutilities.localization.Replacement;
import de.eldoria.eldoutilities.simplecommands.EldoCommand;
import de.eldoria.eldoutilities.simplecommands.TabCompleteUtil;
import de.eldoria.eldoutilities.utils.ArgumentUtils;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class CancelNight extends EldoCommand {
    private final NightManager nightManager;
    private final Configuration configuration;

    public CancelNight(Plugin plugin, NightManager nightManager, Configuration configuration) {
        super(plugin);
        this.nightManager = nightManager;
        this.configuration = configuration;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (denyAccess(sender, Permissions.CANCEL_NIGHT)) {
            return true;
        }

        World world = null;
        if (sender instanceof Player) {
            Player player = (Player) sender;
            world = player.getWorld();
        } else {
            if (argumentsInvalid(sender, args, 1, "[" + localizer().getMessage("syntax.worldName") + "]")) {
                return true;
            }
        }

        world = ArgumentUtils.getOrDefault(args, 0, ArgumentUtils::getWorld, world);

        if (world == null) {
            messageSender().sendError(sender, localizer().getMessage("error.invalidWorld"));
            return true;
        }

        boolean enabled = configuration.getWorldSettings(world).isEnabled();
        if (!enabled) {
            messageSender().sendLocalizedError(sender, "error.worldNotEnabled",
                    Replacement.create("WORLD", world.getName(), '6'));
            return true;
        }
        if (nightManager.isBloodNightActive(world)) {
            nightManager.cancelNight(world);
            messageSender().sendMessage(sender, localizer().getMessage("cancelNight.canceled",
                    Replacement.create("WORLD", world.getName()).addFormatting('6')));
        } else {
            messageSender().sendError(sender, localizer().getMessage("cancelNight.notActive",
                    Replacement.create("WORLD", world.getName()).addFormatting('6')));
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command
            command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return TabCompleteUtil.completeWorlds(args[0]);
        }
        return Collections.emptyList();
    }
}
