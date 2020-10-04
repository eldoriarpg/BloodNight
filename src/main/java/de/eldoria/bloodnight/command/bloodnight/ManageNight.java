package de.eldoria.bloodnight.command.bloodnight;

import de.eldoria.bloodnight.command.util.CommandUtil;
import de.eldoria.bloodnight.command.util.KyoriColors;
import de.eldoria.bloodnight.config.Configuration;
import de.eldoria.bloodnight.config.worldsettings.NightSettings;
import de.eldoria.bloodnight.config.worldsettings.WorldSettings;
import de.eldoria.bloodnight.core.BloodNight;
import de.eldoria.bloodnight.util.Permissions;
import de.eldoria.eldoutilities.localization.Localizer;
import de.eldoria.eldoutilities.localization.Replacement;
import de.eldoria.eldoutilities.messages.MessageSender;
import de.eldoria.eldoutilities.simplecommands.EldoCommand;
import de.eldoria.eldoutilities.simplecommands.TabCompleteUtil;
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;

public class ManageNight extends EldoCommand {
    private final Configuration configuration;
    private final BukkitAudiences bukkitAudiences;

    public ManageNight(Localizer localizer, MessageSender messageSender, Configuration configuration) {
        super(localizer, messageSender);
        this.configuration = configuration;
        bukkitAudiences = BukkitAudiences.create(BloodNight.getInstance());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (isConsole(sender)) return true;

        if (denyAccess(sender, Permissions.MANAGE_NIGHT)) {
            messageSender().sendError(sender, localizer().getMessage("error.console"));
            return true;
        }

        Player player = (Player) sender;

        World world = args.length > 0 ? Bukkit.getWorld(args[0]) : player.getWorld();

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

        if (ArrayUtil.arrayContains(new String[] {"enable", "skippable", "overrideDuration"}, cmd)) {
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
            if ("overrideDuration".equalsIgnoreCase(cmd)) {
                nightSettings.setOverrideNightDuration(optionalBoolean.get());
            }
            configuration.safeConfig();
            sendNightSettings(sender, worldSettings);
            return true;
        }

        if (ArrayUtil.arrayContains(new String[] {"nightBegin", "nightEnd", "nightDuration"}, cmd)) {
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
            configuration.safeConfig();
            sendNightSettings(sender, worldSettings);
            return true;
        }
        messageSender().sendError(player, localizer().getMessage("error.invalidField"));
        return true;
    }

    private void sendNightSettings(CommandSender sender, WorldSettings worldSettings) {
        NightSettings nightSettings = worldSettings.getNightSettings();
        String cmd = "/bloodnight manageNight " + worldSettings.getWorldName() + " ";
        TextComponent.Builder builder = TextComponent.builder()
                .append(TextComponent.newline())
                .append(TextComponent.newline())
                .append(TextComponent.newline())
                .append(TextComponent.newline())
                .append(CommandUtil.getHeader(localizer().getMessage("manageNight.title",
                        Replacement.create("WORLD", worldSettings.getWorldName()).addFormatting('6'))))
                .append(TextComponent.newline())
                // World state
                .append(CommandUtil.getBooleanField(
                        worldSettings.isEnabled(),
                        cmd + "enable {bool}",
                        localizer().getMessage("field.active"),
                        localizer().getMessage("state.enabled"),
                        localizer().getMessage("state.disabled")))
                .append(TextComponent.newline())
                // skippable
                .append(CommandUtil.getBooleanField(nightSettings.isSkippable(),
                        cmd + "skippable {bool}",
                        localizer().getMessage("field.sleep"),
                        localizer().getMessage("state.allow"),
                        localizer().getMessage("state.deny")))
                .append(TextComponent.newline())
                // night begin
                .append(TextComponent.builder(localizer().getMessage("field.nightBegin") + ": ", KyoriColors.AQUA))
                .append(TextComponent.builder(nightSettings.getNightBegin() + " ", KyoriColors.GOLD))
                .append(TextComponent.builder("[" + localizer().getMessage("action.change") + "]", KyoriColors.GREEN)
                        .clickEvent(ClickEvent.suggestCommand(cmd + "nightBegin ")))
                .append(TextComponent.newline())
                // night end
                .append(TextComponent.builder(localizer().getMessage("field.nightEnd") + ": ", KyoriColors.AQUA))
                .append(TextComponent.builder(nightSettings.getNightEnd() + " ", KyoriColors.GOLD))
                .append(TextComponent.builder("[" + localizer().getMessage("action.change") + "]", KyoriColors.GREEN)
                        .clickEvent(ClickEvent.suggestCommand(cmd + "nightEnd ")))
                .append(TextComponent.newline())
                // override night duration
                .append(CommandUtil.getBooleanField(nightSettings.isOverrideNightDuration(), cmd + " overrideDuration {bool}",
                        localizer().getMessage("field.overrideDuration") + ": ",
                        localizer().getMessage("state.enabled"),
                        localizer().getMessage("state.disabled")));
        if (nightSettings.isOverrideNightDuration()) {
            //night duration
            builder.append(TextComponent.newline())
                    .append(TextComponent.builder(localizer().getMessage("field.nightDuration") + ": ", KyoriColors.AQUA))
                    .append(TextComponent.builder(nightSettings.getNightDuration() + " " + localizer().getMessage("value.seconds"), KyoriColors.GOLD))
                    .append(TextComponent.builder(" [" + localizer().getMessage("action.change") + "]", KyoriColors.GREEN)
                            .clickEvent(ClickEvent.suggestCommand(cmd + "nightDuration ")));
        }


        bukkitAudiences.audience(sender).sendMessage(builder.build());
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
        if (TabCompleteUtil.isCommand(field, "nightBegin", "nightEnd", "nightDuration")) {
            return TabCompleteUtil.isCommand(field, "nightBegin", "nightEnd")
                    ? TabCompleteUtil.completeInt(value, 1, 24000, localizer())
                    : TabCompleteUtil.completeInt(value, 1, 86400, localizer());
        }

        if (TabCompleteUtil.isCommand(field, "enable", "skippable", "overrideDuration")) {
            return TabCompleteUtil.completeBoolean(value);
        }
        return Collections.emptyList();
    }
}
