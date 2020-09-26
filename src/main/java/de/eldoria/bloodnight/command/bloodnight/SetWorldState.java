package de.eldoria.bloodnight.command.bloodnight;

import de.eldoria.bloodnight.command.util.CommandUtil;
import de.eldoria.bloodnight.command.util.KyoriColors;
import de.eldoria.bloodnight.config.Configuration;
import de.eldoria.bloodnight.config.WorldSettings;
import de.eldoria.bloodnight.core.BloodNight;
import de.eldoria.eldoutilities.localization.Localizer;
import de.eldoria.eldoutilities.messages.MessageSender;
import de.eldoria.eldoutilities.simplecommands.EldoCommand;
import de.eldoria.eldoutilities.utils.ArgumentUtils;
import de.eldoria.eldoutilities.utils.Parser;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;

public class SetWorldState extends EldoCommand {
    private final Configuration configuration;
    private final BukkitAudiences bukkitAudiences;

    public SetWorldState(Localizer localizer, MessageSender messageSender, Configuration configuration) {
        super(localizer, messageSender);
        this.configuration = configuration;
        bukkitAudiences = BukkitAudiences.create(BloodNight.getInstance());
    }

    // <world> <state> [page]
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (args.length == 0) {
            sendPage(sender, 0);
            return true;
        }

        if (argumentsInvalid(sender, args, 2, "")) {
            return true;
        }
        if (!"none".equalsIgnoreCase(args[0])) {


            World world = Bukkit.getWorld(args[0]);

            if (world == null) {
                messageSender().sendError(sender, "invalid world");
                return true;
            }

            Optional<Boolean> aBoolean = Parser.parseBoolean(args[1]);

            if (!aBoolean.isPresent()) {
                messageSender().sendError(sender, "invalid boolean");
                return true;
            }

            configuration.getWorldSettings(world.getName()).setEnabled(aBoolean.get());
        }

        OptionalInt page = ArgumentUtils.getOptionalParameter(args, 2, OptionalInt.empty(), Parser::parseInt);
        if (!page.isPresent()) {
            messageSender().sendMessage(sender, "changed");
        } else {
            sendPage(sender, page.getAsInt());
        }
        configuration.safeConfig();
        return true;
    }

    private void sendPage(CommandSender sender, int page) {
        List<WorldSettings> collect = Bukkit.getWorlds().stream()
                .map(w -> configuration.getWorldSettings(w.getName()))
                .collect(Collectors.toList());

        TextComponent message = CommandUtil.getPage(collect,
                page,
                entry -> TextComponent.builder().append(TextComponent.builder(entry.getWorldName() + " ").color(KyoriColors.AQUA))
                        .append(
                                TextComponent.builder("enabled",
                                        entry.isEnabled() ? KyoriColors.GREEN : KyoriColors.DARK_GRAY)
                                        .clickEvent(
                                                ClickEvent.runCommand("/bloodNight setWorldState " + entry.getWorldName() + " true " + page))
                        )
                        .append(" ")
                        .append(
                                TextComponent.builder("disabled",
                                        !entry.isEnabled() ? KyoriColors.RED : KyoriColors.DARK_GRAY)
                                        .clickEvent(
                                                ClickEvent.runCommand("/bloodNight setWorldState " + entry.getWorldName() + " false " + page))
                        ).build(),
                "World States",
                "/bloodNight setWorldState none {page}");

        bukkitAudiences.audience(sender).sendMessage(message);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return super.onTabComplete(sender, command, alias, args);
    }
}
