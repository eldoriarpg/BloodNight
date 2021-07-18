package de.eldoria.bloodnight.command.bloodnight;

import de.eldoria.bloodnight.command.util.CommandUtil;
import de.eldoria.bloodnight.config.Configuration;
import de.eldoria.bloodnight.config.worldsettings.NightSettings;
import de.eldoria.bloodnight.config.worldsettings.WorldSettings;
import de.eldoria.bloodnight.core.BloodNight;
import de.eldoria.bloodnight.util.Permissions;
import de.eldoria.eldoutilities.localization.Replacement;
import de.eldoria.eldoutilities.simplecommands.EldoCommand;
import de.eldoria.eldoutilities.simplecommands.TabCompleteUtil;
import de.eldoria.eldoutilities.utils.ArgumentUtils;
import de.eldoria.eldoutilities.utils.ArrayUtil;
import de.eldoria.eldoutilities.utils.EnumUtil;
import de.eldoria.eldoutilities.utils.Parser;
import lombok.var;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ManageNight extends EldoCommand {
    private final Configuration configuration;
    private final BukkitAudiences bukkitAudiences;

    public ManageNight(Plugin plugin, Configuration configuration) {
        super(plugin);
        this.configuration = configuration;
        bukkitAudiences = BukkitAudiences.create(BloodNight.getInstance());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (denyConsole(sender)) {
            return true;
        }

        if (denyAccess(sender, Permissions.Admin.MANAGE_NIGHT)) {
            return true;
        }

        Player player = getPlayerFromSender(sender);

        World world = ArgumentUtils.getOrDefault(args, 0, ArgumentUtils::getWorld, player.getWorld());

        if (world == null) {
            messageSender().sendError(sender, localizer().getMessage("error.invalidWorld"));
            return true;
        }

        WorldSettings worldSettings = configuration.getWorldSettings(world);
        if (args.length < 2) {
            sendNightSettings(sender, worldSettings);
            return true;
        }

        if (argumentsInvalid(sender, args, 3,
                "[" + localizer().getMessage("syntax.worldName") + "] [<"
                        + localizer().getMessage("syntax.field") + "> <"
                        + localizer().getMessage("syntax.value") + ">]")) {
            return true;
        }

        String cmd = args[1];
        String value = args[2];
        OptionalDouble optionalDouble = Parser.parseDouble(value);
        OptionalInt optionalInt = Parser.parseInt(value);
        Optional<Boolean> optionalBoolean = Parser.parseBoolean(value);

        NightSettings nightSettings = worldSettings.getNightSettings();

        if (ArrayUtil.arrayContains(new String[]{"enable", "skippable"}, cmd)) {
            if (!optionalBoolean.isPresent()) {
                messageSender().sendError(sender, localizer().getMessage("error.invalidBoolean"));
                return true;
            }

            if ("enable".equalsIgnoreCase(cmd)) {
                worldSettings.setEnabled(optionalBoolean.get());
            }
            if ("skippable".equalsIgnoreCase(cmd)) {
                nightSettings.setSkippable(optionalBoolean.get());
            }
            configuration.save();
            sendNightSettings(sender, worldSettings);
            return true;
        }

        if (ArrayUtil.arrayContains(new String[]{"nightBegin", "nightEnd", "nightDuration", "maxNightDuration"}, cmd)) {
            if (!optionalInt.isPresent()) {
                messageSender().sendError(sender, localizer().getMessage("error.invalidNumber"));
                return true;
            }
            if ("nightBegin".equalsIgnoreCase(cmd)) {
                if (invalidRange(sender, optionalInt.getAsInt(), 0, 24000)) {
                    return true;
                }
                nightSettings.setNightBegin(optionalInt.getAsInt());
            }
            if ("nightEnd".equalsIgnoreCase(cmd)) {
                if (invalidRange(sender, optionalInt.getAsInt(), 0, 24000)) {
                    return true;
                }
                nightSettings.setNightEnd(optionalInt.getAsInt());
            }
            if ("nightDuration".equalsIgnoreCase(cmd)) {
                if (invalidRange(sender, optionalInt.getAsInt(), 0, 86400)) {
                    return true;
                }
                nightSettings.setNightDuration(optionalInt.getAsInt());
            }
            if ("maxNightDuration".equalsIgnoreCase(cmd)) {
                if (invalidRange(sender, optionalInt.getAsInt(), nightSettings.getNightDuration(), 86400)) {
                    return true;
                }
                nightSettings.setMaxNightDuration(optionalInt.getAsInt());
            }
            configuration.save();
            sendNightSettings(sender, worldSettings);
            return true;
        }

        if ("durationMode".equalsIgnoreCase(cmd)) {
            NightSettings.NightDuration parse = EnumUtil.parse(value, NightSettings.NightDuration.class);
            if (parse == null) {
                messageSender().sendLocalizedError(sender, "error.invalidValue");
                return true;
            }
            nightSettings.setNightDurationMode(parse);
            configuration.save();
            sendNightSettings(sender, worldSettings);
            return true;
        }

        messageSender().sendError(player, localizer().getMessage("error.invalidField"));
        return true;
    }

    private void sendNightSettings(CommandSender sender, WorldSettings worldSettings) {
        NightSettings nightSettings = worldSettings.getNightSettings();
        String cmd = "/bloodnight manageNight " + ArgumentUtils.escapeWorldName(worldSettings.getWorldName()) + " ";
        NightSettings.NightDuration durationMode = nightSettings.getNightDurationMode();
        var builder = Component.text()
                .append(Component.newline())
                .append(Component.newline())
                .append(Component.newline())
                .append(Component.newline())
                .append(CommandUtil.getHeader(localizer().getMessage("manageNight.title",
                        Replacement.create("WORLD", worldSettings.getWorldName()).addFormatting('6'))))
                .append(Component.newline())
                // World state
                .append(CommandUtil.getBooleanField(
                        worldSettings.isEnabled(),
                        cmd + "enable {bool}",
                        localizer().getMessage("field.active"),
                        localizer().getMessage("state.enabled"),
                        localizer().getMessage("state.disabled")))
                .append(Component.newline())
                // skippable
                .append(CommandUtil.getBooleanField(nightSettings.isSkippable(),
                        cmd + "skippable {bool}",
                        localizer().getMessage("field.sleep"),
                        localizer().getMessage("state.allow"),
                        localizer().getMessage("state.deny")))
                .append(Component.newline())
                // night begin
                .append(Component.text(localizer().getMessage("field.nightBegin") + ": ", NamedTextColor.AQUA))
                .append(Component.text(nightSettings.getNightBegin() + " ", NamedTextColor.GOLD))
                .append(Component.text("[" + localizer().getMessage("action.change") + "]", NamedTextColor.GREEN)
                        .clickEvent(ClickEvent.suggestCommand(cmd + "nightBegin ")))
                .append(Component.newline())
                // night end
                .append(Component.text(localizer().getMessage("field.nightEnd") + ": ", NamedTextColor.AQUA))
                .append(Component.text(nightSettings.getNightEnd() + " ", NamedTextColor.GOLD))
                .append(Component.text("[" + localizer().getMessage("action.change") + "]", NamedTextColor.GREEN)
                        .clickEvent(ClickEvent.suggestCommand(cmd + "nightEnd ")))
                .append(Component.newline())
                // override night duration
                .append(CommandUtil.getToggleField(durationMode == NightSettings.NightDuration.NORMAL,
                        cmd + "durationMode NORMAL",
                        localizer().getMessage("state.normal")))
                .append(Component.space())
                .append(CommandUtil.getToggleField(durationMode == NightSettings.NightDuration.EXTENDED,
                        cmd + "durationMode EXTENDED",
                        localizer().getMessage("state.extended")))
                .append(Component.space())
                .append(CommandUtil.getToggleField(durationMode == NightSettings.NightDuration.RANGE,
                        cmd + "durationMode RANGE",
                        localizer().getMessage("state.range")))
                .append(Component.newline());
        switch (durationMode) {
            case NORMAL:
                builder.append(Component.text(">", NamedTextColor.GOLD))
                        .append(Component.newline())
                        .append(Component.text(">", NamedTextColor.GOLD));
                break;
            case EXTENDED:
                //night duration
                builder.append(Component.text(localizer().getMessage("field.nightDuration") + ": ", NamedTextColor.AQUA))
                        .append(Component.text(nightSettings.getNightDuration() + " " + localizer().getMessage("value.seconds"), NamedTextColor.GOLD))
                        .append(Component.text(" [" + localizer().getMessage("action.change") + "]", NamedTextColor.GREEN)
                                .clickEvent(ClickEvent.suggestCommand(cmd + "nightDuration ")))
                        .append(Component.newline())
                        .append(Component.text(">", NamedTextColor.GOLD));
                break;
            case RANGE:
                builder.append(Component.text(localizer().getMessage("field.minDuration") + ": ", NamedTextColor.AQUA))
                        .append(Component.text(nightSettings.getNightDuration() + " " + localizer().getMessage("value.seconds"), NamedTextColor.GOLD))
                        .append(Component.text(" [" + localizer().getMessage("action.change") + "]", NamedTextColor.GREEN)
                                .clickEvent(ClickEvent.suggestCommand(cmd + "nightDuration ")))
                        .append(Component.newline())
                        .append(Component.text(localizer().getMessage("field.maxDuration") + ": ", NamedTextColor.AQUA))
                        .append(Component.text(nightSettings.getMaxNightDuration() + " " + localizer().getMessage("value.seconds"), NamedTextColor.GOLD))
                        .append(Component.text(" [" + localizer().getMessage("action.change") + "]", NamedTextColor.GREEN)
                                .clickEvent(ClickEvent.suggestCommand(cmd + "maxNightDuration ")));
                break;
        }

        bukkitAudiences.sender(sender).sendMessage(Identity.nil(), builder.build());
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return TabCompleteUtil.completeWorlds(args[0]);
        }
        if (args.length == 2) {
            return TabCompleteUtil.complete(args[1], "nightBegin", "nightEnd", "nightDuration",
                    "enable", "skippable", "overrideDuration");
        }

        String field = args[1];
        String value = args[2];
        if (TabCompleteUtil.isCommand(field, "nightBegin", "nightEnd", "nightDuration", "maxNightDuration")) {
            return TabCompleteUtil.isCommand(field, "nightBegin", "nightEnd")
                    ? TabCompleteUtil.completeInt(value, 1, 24000, localizer())
                    : TabCompleteUtil.completeInt(value, 1, 86400, localizer());
        }

        if (TabCompleteUtil.isCommand(field, "enable", "skippable")) {
            return TabCompleteUtil.completeBoolean(value);
        }

        if (TabCompleteUtil.isCommand(field, "durationMode")) {
            return TabCompleteUtil.complete(value, NightSettings.NightDuration.class);
        }
        return Collections.emptyList();
    }
}
