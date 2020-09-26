package de.eldoria.bloodnight.command.bloodnight;

import de.eldoria.bloodnight.command.util.CommandUtil;
import de.eldoria.bloodnight.command.util.KyoriColors;
import de.eldoria.bloodnight.config.Configuration;
import de.eldoria.bloodnight.config.MobSetting;
import de.eldoria.bloodnight.config.WorldSettings;
import de.eldoria.bloodnight.core.BloodNight;
import de.eldoria.eldoutilities.localization.Localizer;
import de.eldoria.eldoutilities.messages.MessageSender;
import de.eldoria.eldoutilities.simplecommands.EldoCommand;
import de.eldoria.eldoutilities.utils.ArgumentUtils;
import de.eldoria.eldoutilities.utils.ArrayUtil;
import de.eldoria.eldoutilities.utils.Parser;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Collectors;

public class SetMobState extends EldoCommand {
    public final Configuration configuration;
    private final BukkitAudiences bukkitAudiences;

    public SetMobState(Localizer localizer, MessageSender messageSender, Configuration configuration) {
        super(localizer, messageSender);
        this.configuration = configuration;
        bukkitAudiences = BukkitAudiences.create(BloodNight.getInstance());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) return true;

        Player player = (Player) sender;

        World world = player.getWorld();

        if (args.length > 0) {
            world = Bukkit.getWorld(args[0]);
            if (world == null) {
                messageSender().sendError(sender, "invalid world");
                return true;
            }
        }

        WorldSettings worldSettings = configuration.getWorldSettings(world);

        if (worldSettings.isEnabled()) {
            messageSender().sendError(player, "Blood Night is not active in this world.");
            return true;
        }

        if (args.length < 2) {
            sendMobListPage(world, sender, 0);
            return true;
        }

        if (argumentsInvalid(sender, args, 3, "some syntax")) {
            return true;
        }

        if (!"none".equalsIgnoreCase(args[1])) {

            Optional<MobSetting> mobByName = worldSettings.getMobSettings().getMobByName(args[1]);
            if (!mobByName.isPresent()) {
                messageSender().sendError(sender, "Invalid mob");
                return true;
            }

            Optional<Boolean> aBoolean = Parser.parseBoolean(args[2]);
            if (!aBoolean.isPresent()) {
                messageSender().sendError(sender, "invalid boolean");
                return true;
            }

            mobByName.get().setActive(aBoolean.get());
        }

        OptionalInt optionalParameter = ArgumentUtils.getOptionalParameter(args, 3, OptionalInt.empty(), Parser::parseInt);
        if (!optionalParameter.isPresent()) {
            messageSender().sendMessage(sender, "changed");
        } else {
            sendMobListPage(world, sender, optionalParameter.getAsInt());
        }
        configuration.safeConfig();
        return true;
    }

    private void sendMobListPage(World world, CommandSender sender, int page) {
        TextComponent component = CommandUtil.getPage(
                new ArrayList<>(configuration.getWorldSettings(world).getMobSettings().getMobTypes().entrySet()),
                page,
                entry -> TextComponent.builder().append(entry.getKey() + " ").color(KyoriColors.AQUA)
                        .append(
                                TextComponent.builder("enabled",
                                        entry.getValue().isActive() ? KyoriColors.GREEN : KyoriColors.DARK_GRAY)
                                        .clickEvent(
                                                ClickEvent.runCommand("/bloodNight setMobState " + world.getName() + " " + entry.getKey() + " true " + page))
                        )
                        .append(" ")
                        .append(
                                TextComponent.builder("disabled",
                                        !entry.getValue().isActive() ? KyoriColors.RED : KyoriColors.DARK_GRAY)
                                        .clickEvent(
                                                ClickEvent.runCommand("/bloodNight setMobState " + world.getName() + " " + entry.getKey() + " false " + page))
                        ).build(),
                "Mob States",
                "/bloodNight setMobState " + world.getName() + " none none {page}");

        bukkitAudiences.audience(sender).sendMessage(component);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            Set<String> strings = configuration.getWorldSettings().keySet();
            return ArrayUtil.startingWithInArray(args[0], strings.toArray(new String[0])).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
