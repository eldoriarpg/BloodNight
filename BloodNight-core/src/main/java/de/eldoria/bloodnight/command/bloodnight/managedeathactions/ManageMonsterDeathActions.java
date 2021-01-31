package de.eldoria.bloodnight.command.bloodnight.managedeathactions;

import de.eldoria.bloodnight.command.util.CommandUtil;
import de.eldoria.bloodnight.config.Configuration;
import de.eldoria.bloodnight.config.worldsettings.deathactions.MobDeathActions;
import de.eldoria.eldoutilities.simplecommands.EldoCommand;
import de.eldoria.eldoutilities.simplecommands.TabCompleteUtil;
import de.eldoria.eldoutilities.utils.ArgumentUtils;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class ManageMonsterDeathActions extends EldoCommand {
    private final Configuration configuration;
    private final BukkitAudiences bukkitAudiences;

    public ManageMonsterDeathActions(Plugin plugin, Configuration configuration, BukkitAudiences bukkitAudiences) {
        super(plugin);
        this.configuration = configuration;
        this.bukkitAudiences = bukkitAudiences;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;

        World world = ArgumentUtils.getOrDefault(args, 0, ArgumentUtils::getWorld, player.getWorld());

        if (world == null) {
            messageSender().sendLocalizedError(sender, "error.invalidWorld");
            return true;
        }

        MobDeathActions mobDeathActions = configuration.getWorldSettings(world).getDeathActionSettings().getMobDeathActions();

        if (args.length < 2) {
            sendMobDeathActions(player, world, mobDeathActions);
            return true;
        }

        if (argumentsInvalid(sender, args, 1,
                "<monster|player> <$syntax.worldName$> [<$syntax.field$> <$syntax.value$>]")) return true;

        String field = args[1];
        String value = ArgumentUtils.getOrDefault(args, 2, "none");


        if ("lightning".equalsIgnoreCase(field)) {
            DeathActionUtil.buildLightningUI(mobDeathActions.getLightningSettings(), player, configuration, localizer(), () -> sendMobDeathActions(player, world, mobDeathActions));
            return true;
        }

        if ("shockwave".equalsIgnoreCase(field)) {
            DeathActionUtil.buildShockwaveUI(mobDeathActions.getShockwaveSettings(), player, configuration, localizer(),
                    () -> sendMobDeathActions(player, world, mobDeathActions));

            return true;
        }
        messageSender().sendError(sender, "error.invalidField");
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return TabCompleteUtil.completeWorlds(args[0]);
        }

        if (args.length == 2) {
            return TabCompleteUtil.complete(args[1], "lightning", "shockwave");
        }
        return Collections.emptyList();
    }

    private void sendMobDeathActions(Player player, World world, MobDeathActions mobDeathActions) {
        String cmd = "/bloodnight deathActions monster " + ArgumentUtils.escapeWorldName(world.getName()) + " ";
        TextComponent build = Component.text()
                .append(CommandUtil.getHeader(localizer().getMessage("manageDeathActions.monster.title")))
                .append(Component.newline())
                .append(Component.text(localizer().getMessage("field.lightningSettings"), NamedTextColor.AQUA))
                .append(Component.text(" [" + localizer().getMessage("action.change") + "]", NamedTextColor.GREEN)
                        .clickEvent(ClickEvent.runCommand(cmd + "lightning")))
                .append(Component.newline())
                .append(Component.text(localizer().getMessage("field.shockwaveSettings"), NamedTextColor.AQUA))
                .append(Component.text(" [" + localizer().getMessage("action.change") + "]", NamedTextColor.GREEN)
                        .clickEvent(ClickEvent.runCommand(cmd + "shockwave")))
                .build();

        bukkitAudiences.player(player).sendMessage(build);
    }
}
