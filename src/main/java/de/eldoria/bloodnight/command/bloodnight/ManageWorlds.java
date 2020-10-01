package de.eldoria.bloodnight.command.bloodnight;

import de.eldoria.bloodnight.command.util.CommandUtil;
import de.eldoria.bloodnight.command.util.KyoriColors;
import de.eldoria.bloodnight.config.BossBarSettings;
import de.eldoria.bloodnight.config.Configuration;
import de.eldoria.bloodnight.config.WorldSettings;
import de.eldoria.bloodnight.core.BloodNight;
import de.eldoria.eldoutilities.localization.Localizer;
import de.eldoria.eldoutilities.messages.MessageSender;
import de.eldoria.eldoutilities.simplecommands.EldoCommand;
import de.eldoria.eldoutilities.utils.EnumUtil;
import de.eldoria.eldoutilities.utils.Parser;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

public class ManageWorlds extends EldoCommand {
    private final Configuration configuration;
    private final BukkitAudiences bukkitAudiences;

    public ManageWorlds(Localizer localizer, MessageSender messageSender, Configuration configuration) {
        super(localizer, messageSender);
        this.configuration = configuration;
        bukkitAudiences = BukkitAudiences.create(BloodNight.getInstance());
    }

    // world field value page
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

        if (args.length < 2) {
            sendWorldPage(world, sender, 0);
            return true;
        }

        // world field value [page]
        if (argumentsInvalid(sender, args, 3, "some syntax")) {
            return true;
        }

        String field = args[1];
        String value = args[2];
        World finalWorld = world;
        OptionalInt optPage = CommandUtil.findPage(configuration.getWorldSettings().values(), 3,
                w -> w.getWorldName().equalsIgnoreCase(finalWorld.getName()));

        if ("page".equalsIgnoreCase(field)) {
            optPage = Parser.parseInt(value);
            if (optPage.isPresent()) {
                sendWorldPage(world, sender, optPage.getAsInt());
            }
            return true;
        }

        if ("bossBar".equalsIgnoreCase(field)) {
            String bossBarValue = args[3];
            BossBarSettings bbs = worldSettings.getBossBarSettings();
            if ("state".equalsIgnoreCase(value)) {
                Optional<Boolean> aBoolean = Parser.parseBoolean(bossBarValue);
                if (!aBoolean.isPresent()) {
                    messageSender().sendError(sender, "invalid boolean");
                    return true;
                }
                bbs.setEnabled(aBoolean.get());
            } else if ("title".equalsIgnoreCase(value)) {
                String title = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
                bbs.setTitle(title);
            } else if ("color".equalsIgnoreCase(value)) {
                BarColor parse = EnumUtil.parse(bossBarValue, BarColor.class);
                if (parse == null) {
                    messageSender().sendError(sender, "Invalid boss bar color");
                    return true;
                }
                bbs.setColor(parse);
            } else if ("toggleEffect".equalsIgnoreCase(value)) {
                BarFlag parse = EnumUtil.parse(bossBarValue, BarFlag.class);
                if (parse == null) {
                    messageSender().sendError(sender, "Invalid boss bar flag");
                    return true;
                }
                bbs.toggleEffect(parse);
            } else {
                messageSender().sendError(sender, "invalid field");
                return true;
            }
        } else if ("state".equalsIgnoreCase(field)) {
            Optional<Boolean> aBoolean = Parser.parseBoolean(value);
            if (!aBoolean.isPresent()) {
                messageSender().sendError(sender, "invalid boolean");
                return true;
            }
            worldSettings.setEnabled(aBoolean.get());
        } else {
            messageSender().sendError(sender, "invalid field");
            return true;
        }
        if (optPage.isPresent()) {
            sendWorldPage(world, sender, optPage.getAsInt());
        } else {
            messageSender().sendMessage(sender, "changed");
        }
        return true;
    }

    private void sendWorldPage(World world, CommandSender sender, int page) {
        TextComponent component = CommandUtil.getPage(
                new ArrayList<>(configuration.getWorldSettings().values()),
                page,
                3, 5,
                entry -> {
                    String cmd = "/bloodnight manageWorlds " + entry.getWorldName() + " ";
                    BossBarSettings bbs = entry.getBossBarSettings();
                    return TextComponent.builder()
                            // World State
                            .append(TextComponent.builder(entry.getWorldName(), KyoriColors.GOLD)
                                    .decoration(TextDecoration.BOLD, true).build()).append(" ")
                            .append(CommandUtil.getBooleanField(entry.isEnabled(),
                                    cmd + "state {bool} ",
                                    "", "enabled", "disabled"))
                            .append(TextComponent.newline()).append("  ")
                            // boss bar state
                            .append(TextComponent.builder("BossBarSettings: ", KyoriColors.AQUA))
                            .append(CommandUtil.getBooleanField(bbs.isEnabled(),
                                    cmd + "bossBar state {bool} ",
                                    "", "enabled", "disabled"))
                            .append(TextComponent.newline()).append("  ")
                            // title
                            .append(TextComponent.builder("Title: ", KyoriColors.AQUA))
                            .append(TextComponent.builder(bbs.getTitle(), KyoriColors.GOLD))
                            .append(TextComponent.builder(" [change] ", KyoriColors.GREEN)
                                    .clickEvent(ClickEvent.suggestCommand(cmd + "bossBar title ")))
                            .append(TextComponent.newline()).append("  ")
                            // Color
                            .append(TextComponent.builder("Color: ", KyoriColors.AQUA))
                            .append(TextComponent.builder(bbs.getColor().toString(), toKyoriColor(bbs.getColor())))
                            .append(TextComponent.builder(" [change] ", KyoriColors.GREEN)
                                    .clickEvent(ClickEvent.suggestCommand(cmd + "bossBar color ")))
                            .append(TextComponent.newline()).append("  ")
                            // Effects
                            .append(TextComponent.builder("Effects: ", KyoriColors.AQUA))
                            .append(CommandUtil.getToggleField(bbs.isEffectEnabled(BarFlag.CREATE_FOG),
                                    cmd + "bossBar toggleEffect CREATE_FOG",
                                    "Fog"))
                            .append(" ")
                            .append(CommandUtil.getToggleField(bbs.isEffectEnabled(BarFlag.DARKEN_SKY),
                                    cmd + "bossBar toggleEffect DARKEN_SKY",
                                    "Darken Sky"))
                            .append(" ")
                            .append(CommandUtil.getToggleField(bbs.isEffectEnabled(BarFlag.PLAY_BOSS_MUSIC),
                                    cmd + "bossBar toggleEffect PLAY_BOSS_MUSIC",
                                    "Music"))
                            .build();
                },
                "Mob States",
                "/bloodNight manageWorlds " + world.getName() + " page {page}");

        bukkitAudiences.audience(sender).sendMessage(component);
    }


    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return super.onTabComplete(sender, command, alias, args);
    }

    private TextColor toKyoriColor(BarColor color) {
        switch (color) {
            case PINK:
                return KyoriColors.PINK;
            case BLUE:
                return KyoriColors.BLUE;
            case RED:
                return KyoriColors.RED;
            case GREEN:
                return KyoriColors.GREEN;
            case YELLOW:
                return KyoriColors.YELLOW;
            case PURPLE:
                return KyoriColors.LIGHT_PURPLE;
            case WHITE:
                return KyoriColors.WHITE;
            default:
                throw new IllegalStateException("Unexpected value: " + color);
        }
    }
}
