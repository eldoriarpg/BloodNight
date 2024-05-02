package de.eldoria.bloodnight.command.bloodnight;

import de.eldoria.bloodnight.command.util.CommandUtil;
import de.eldoria.bloodnight.config.Configuration;
import de.eldoria.bloodnight.config.worldsettings.BossBarSettings;
import de.eldoria.bloodnight.config.worldsettings.WorldSettings;
import de.eldoria.bloodnight.util.Permissions;
import de.eldoria.eldoutilities.commands.Completion;
import de.eldoria.eldoutilities.commands.command.AdvancedCommand;
import de.eldoria.eldoutilities.commands.command.CommandMeta;
import de.eldoria.eldoutilities.commands.command.util.Arguments;
import de.eldoria.eldoutilities.commands.command.util.CommandAssertions;
import de.eldoria.eldoutilities.commands.command.util.Input;
import de.eldoria.eldoutilities.commands.exceptions.CommandException;
import de.eldoria.eldoutilities.commands.executor.IPlayerTabExecutor;
import de.eldoria.eldoutilities.messages.Replacement;
import de.eldoria.eldoutilities.utils.ArgumentUtils;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static de.eldoria.eldoutilities.localization.ILocalizer.escape;
import static net.kyori.adventure.bossbar.BossBar.Flag.CREATE_WORLD_FOG;
import static net.kyori.adventure.bossbar.BossBar.Flag.DARKEN_SCREEN;
import static net.kyori.adventure.bossbar.BossBar.Flag.PLAY_BOSS_MUSIC;

public class ManageWorlds extends AdvancedCommand implements IPlayerTabExecutor {
    private final Configuration configuration;

    public ManageWorlds(Plugin plugin, Configuration configuration) {
        super(plugin, CommandMeta.builder("manageWorlds").withPermission(Permissions.Admin.MANAGE_WORLDS)
                .addArgument("syntax.worldName", false)
                .addArgument("syntax.field", false)
                .addArgument("syntax.value", false)
                .build());
        this.configuration = configuration;
    }

    // world field value page <- Stupid decision btw
    @Override
    public void onCommand(@NotNull Player player, @NotNull String alias, @NotNull Arguments args) throws CommandException {
        World world = args.asWorld(0, player.getWorld());
        WorldSettings worldSettings = configuration.getWorldSettings(world);

        if (args.size() < 2) {
            sendWorldPage(world, player, 0);
            return;
        }


        String field = args.asString(1);
        Input value = args.get(2);
        Optional<Integer> optPage = CommandUtil.findPage(configuration.getWorldSettings().values(), 2,
                w -> w.getWorldName().equalsIgnoreCase(world.getName()));

        if ("page".equalsIgnoreCase(field)) {
            sendWorldPage(world, player, value.asInt());
            return;
        }

        if ("bossBar".equalsIgnoreCase(field)) {
            CommandAssertions.isTrue(Completion.isCommand(value.asString(), "state", "title", "color", "toggleEffect"), "error.invalidField");
            Input bossBarValue = args.get(3);
            BossBarSettings bbs = worldSettings.getBossBarSettings();
            if ("state".equalsIgnoreCase(value.asString())) {
                bbs.setEnabled(bossBarValue.asBoolean());
            }
            if ("title".equalsIgnoreCase(value.asString())) {
                bbs.setTitle(args.join(3));
            }
            if ("color".equalsIgnoreCase(value.asString())) {
                bbs.setColor(bossBarValue.asEnum(BossBar.Color.class));
            }
            if ("toggleEffect".equalsIgnoreCase(value.asString())) {
                bbs.toggleEffect(bossBarValue.asEnum(BossBar.Flag.class));
            }

            sendWorldPage(world, player, optPage.get());
            configuration.save();
            return;
        }

        CommandAssertions.isTrue(Completion.isCommand(field, "state", "creeperBlockDamage", "manageCreeperAlways"), "error.invalidField");
        if ("state".equalsIgnoreCase(field)) {
            worldSettings.setEnabled(value.asBoolean());
        }
        if ("creeperBlockDamage".equalsIgnoreCase(field)) {
            worldSettings.setCreeperBlockDamage(value.asBoolean());
        }
        if ("manageCreeperAlways".equalsIgnoreCase(field)) {
            worldSettings.setAlwaysManageCreepers(value.asBoolean());
        }
        sendWorldPage(world, player, optPage.get());
        configuration.save();
    }

    private void sendWorldPage(World world, CommandSender sender, int page) {
        String component = CommandUtil.getPage(
                new ArrayList<>(configuration.getWorldSettings().values()),
                page,
                2, 7,
                entry -> {
                    String cmd = "/bloodnight manageWorlds " + ArgumentUtils.escapeWorldName(entry.getWorldName()) + " ";
                    BossBarSettings bbs = entry.getBossBarSettings();
                    return """
                            <header><%s></header> %s
                            %s
                            %s
                            <field>%s:
                              %s
                              <field>%s: <value>%s <reset><click:suggest_command:'%s'><change>[%s]</click>
                              <field>%s: %s <click:suggest_command:'%s'><change>[%s]</click>
                              <field>%s: %s %s %s
                            """.stripIndent()
                            .formatted(
                                    // header
                                    entry.getWorldName(), CommandUtil.getBooleanField(entry.isEnabled(), cmd + "state {bool} ", "", "state.enabled", "state.disabled"),
                                    CommandUtil.getBooleanField(entry.isCreeperBlockDamage(), cmd + "creeperBlockDamage {bool} ", "field.creeperBlockDamage", "state.enabled", "state.disabled"),
                                    CommandUtil.getBooleanField(entry.isAlwaysManageCreepers(), cmd + "manageCreeperAlways {bool} ", "field.alwaysManageCreepers", "state.enabled", "state.disabled"),
                                    escape("field.bossBarSettings"),
                                    CommandUtil.getBooleanField(bbs.isEnabled(), cmd + "bossBar state {bool} ", "field.state", "state.enabled", "state.disabled"),
                                    escape("field.title"), bbs.getTitle(), cmd + "bossBar title " + bbs.getTitle(), escape("action.change"),
                                    escape("field.color"), bbs.getColor(), cmd + "bossBar color ", escape("action.change"),
                                    escape("field.effects"),
                                    CommandUtil.getToggleField(bbs.isEffectEnabled(CREATE_WORLD_FOG), cmd + "bossBar toggleEffect " + CREATE_WORLD_FOG, "state.fog"),
                                    CommandUtil.getToggleField(bbs.isEffectEnabled(DARKEN_SCREEN), cmd + "bossBar toggleEffect " + DARKEN_SCREEN, "state.darkenSky"),
                                    CommandUtil.getToggleField(bbs.isEffectEnabled(PLAY_BOSS_MUSIC), cmd + "bossBar toggleEffect " + PLAY_BOSS_MUSIC, "state.music")
                            );
                },
                "manageWorlds.title",
                "/bloodnight manageWorlds " + ArgumentUtils.escapeWorldName(world) + " page {page}");
        messageSender().sendMessage(sender, component, Replacement.create("world", world));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull Player player, @NotNull String alias, @NotNull Arguments args) throws CommandException {
        if (args.sizeIs(1)) {
            return Completion.completeWorlds(args.asString(0));
        }
        if (args.sizeIs(2)) {
            return Completion.complete(args.asString(1), "bossBar", "state", "creeperBlockDamage", "manageCreeperAlways");
        }

        String field = args.asString(1);
        if ("bossBar".equalsIgnoreCase(field)) {
            if (args.sizeIs(3)) {
                return Completion.complete(args.asString(2), "state", "title", "color", "toggleEffect");
            }
            String bossField = args.asString(2);
            String bossValue = args.asString(3);

            if ("state".equalsIgnoreCase(bossField)) {
                return Completion.completeBoolean(bossValue);
            }
            if ("title".equalsIgnoreCase(bossField)) {
                return Completion.completeFreeInput(args.join(3), 16, localizer().getMessage("field.title"));
            }
            if ("color".equalsIgnoreCase(bossField)) {
                return Completion.complete(bossValue, BossBar.Color.class);
            }
            if ("toggleEffect".equalsIgnoreCase(bossField)) {
                return Completion.complete(bossValue, BossBar.Flag.class);
            }
            return Collections.emptyList();
        }

        if (Completion.isCommand(field, "state", "creeperBlockDamage", "manageCreeperAlways")) {
            return Completion.completeBoolean(args.asString(2));
        }
        return Collections.emptyList();
    }

    private TextColor toKyoriColor(BarColor color) {
        return switch (color) {
            case PINK -> TextColor.color(248, 24, 148);
            case BLUE -> NamedTextColor.BLUE;
            case RED -> NamedTextColor.RED;
            case GREEN -> NamedTextColor.GREEN;
            case YELLOW -> NamedTextColor.YELLOW;
            case PURPLE -> NamedTextColor.LIGHT_PURPLE;
            case WHITE -> NamedTextColor.WHITE;
            default -> throw new IllegalStateException("Unexpected value: " + color);
        };
    }
}
