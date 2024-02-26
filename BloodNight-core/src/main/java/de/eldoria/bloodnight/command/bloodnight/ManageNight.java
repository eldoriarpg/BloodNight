package de.eldoria.bloodnight.command.bloodnight;

import de.eldoria.bloodnight.config.Configuration;
import de.eldoria.bloodnight.config.worldsettings.NightSettings;
import de.eldoria.bloodnight.config.worldsettings.WorldSettings;
import de.eldoria.bloodnight.core.BloodNight;
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
import de.eldoria.eldoutilities.utils.ArrayUtil;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

import static de.eldoria.bloodnight.command.util.CommandUtil.changeableValue;
import static de.eldoria.bloodnight.command.util.CommandUtil.getBooleanField;
import static de.eldoria.bloodnight.command.util.CommandUtil.getHeader;
import static de.eldoria.bloodnight.command.util.CommandUtil.getToggleField;
import static de.eldoria.eldoutilities.localization.ILocalizer.escape;

public class ManageNight extends AdvancedCommand implements IPlayerTabExecutor {
    private final Configuration configuration;
    private final BukkitAudiences bukkitAudiences;

    public ManageNight(Plugin plugin, Configuration configuration) {
        super(plugin, CommandMeta.builder("manageNight")
                .withPermission(Permissions.Admin.MANAGE_NIGHT)
                .addArgument("syntax.worldName", false)
                .addArgument("syntax.field", false)
                .addArgument("syntax.value", false)
                .build());
        this.configuration = configuration;
        bukkitAudiences = BukkitAudiences.create(BloodNight.getInstance());
    }

    @Override
    public void onCommand(@NotNull Player player, @NotNull String alias, @NotNull Arguments args) throws CommandException {
        World world = args.asWorld(0, player.getWorld());

        WorldSettings worldSettings = configuration.getWorldSettings(world);
        if (args.size() < 2) {
            sendNightSettings(player, worldSettings);
            return;
        }


        String cmd = args.asString(1);
        Input value = args.get(2);

        NightSettings nightSettings = worldSettings.getNightSettings();

        if (ArrayUtil.arrayContains(new String[]{"enable", "skippable"}, cmd)) {
            if ("enable".equalsIgnoreCase(cmd)) {
                worldSettings.setEnabled(value.asBoolean());
            }
            if ("skippable".equalsIgnoreCase(cmd)) {
                nightSettings.setSkippable(value.asBoolean());
            }
            configuration.save();
            sendNightSettings(player, worldSettings);
            return;
        }

        if (ArrayUtil.arrayContains(new String[]{"nightBegin", "nightEnd", "nightDuration", "maxNightDuration"}, cmd)) {
            if ("nightBegin".equalsIgnoreCase(cmd)) {
                CommandAssertions.range(value.asInt(), 0, 24000);
                nightSettings.setNightBegin(value.asInt());
            }
            if ("nightEnd".equalsIgnoreCase(cmd)) {
                CommandAssertions.range(value.asInt(), 0, 24000);
                nightSettings.setNightEnd(value.asInt());
            }
            if ("nightDuration".equalsIgnoreCase(cmd)) {
                CommandAssertions.range(value.asInt(), 0, 86400);
                nightSettings.setNightDuration(value.asInt());
            }
            if ("maxNightDuration".equalsIgnoreCase(cmd)) {
                CommandAssertions.range(value.asInt(), 0, 86400);
                nightSettings.setMaxNightDuration(value.asInt());
            }
            configuration.save();
            sendNightSettings(player, worldSettings);
            return;
        }

        if ("durationMode".equalsIgnoreCase(cmd)) {
            nightSettings.setNightDurationMode(value.asEnum(NightSettings.NightDuration.class));
            configuration.save();
            sendNightSettings(player, worldSettings);
            return;
        }

        messageSender().sendError(player, localizer().getMessage("error.invalidField"));
    }

    private void sendNightSettings(CommandSender sender, WorldSettings worldSettings) {
        NightSettings nightSettings = worldSettings.getNightSettings();
        String cmd = "/bloodnight manageNight " + ArgumentUtils.escapeWorldName(worldSettings.getWorldName()) + " ";
        NightSettings.NightDuration durationMode = nightSettings.getNightDurationMode();

        var duration = switch (durationMode) {
            case NORMAL -> """
                    <value>>
                    <value>>""".stripIndent();
            //night duration
            case EXTENDED -> """
                    <value>> %s
                    <value>>""".stripIndent()
                    .formatted(
                            changeableValue("field.nightDuration", nightSettings.getNightDuration() + " " + escape("value.seconds"), cmd + "nightDuration ")
                    );
            case RANGE -> """
                    <value>> %s
                    <value>> %s""".stripIndent()
                    .formatted(
                            changeableValue("field.minDuration", nightSettings.getNightDuration() + " " + escape("value.seconds"), cmd + "nightDuration "),
                            changeableValue("field.maxDuration", nightSettings.getMaxNightDuration() + " " + escape("value.seconds"), cmd + "maxNightDuration ")
                    );
        };

        var a = """
                %s
                %s
                %s
                %s
                %s
                %s %s %s
                %s
                """.stripIndent()
                .formatted(
                        getHeader("manageNight.title"),
                        // World state
                        getBooleanField(worldSettings.isEnabled(), cmd + "enable {bool}", "field.active", "state.enabled", "state.disabled"),
                        // skippable
                        getBooleanField(nightSettings.isSkippable(), cmd + "skippable {bool}", "field.sleep", "state.allow", "state.deny"),
                        // night begin
                        changeableValue("field.nightBegin", nightSettings.getNightBegin(), cmd + "nightBegin "),
                        // night end
                        changeableValue("field.nightEnd", nightSettings.getNightEnd(), cmd + "nightEnd "),
                        // Night duration type
                        getToggleField(durationMode == NightSettings.NightDuration.NORMAL, cmd + "durationMode NORMAL", "state.normal"),
                        getToggleField(durationMode == NightSettings.NightDuration.EXTENDED, cmd + "durationMode EXTENDED", "state.extended"),
                        getToggleField(durationMode == NightSettings.NightDuration.RANGE, cmd + "durationMode RANGE", "state.range"),
                        duration
                );

        messageSender().sendMessage(sender, a, Replacement.create("WORLD", worldSettings.getWorldName()));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull Player player, @NotNull String alias, @NotNull Arguments args) throws CommandException {
        if (args.size() == 1) {
            return Completion.completeWorlds(args.asString(0));
        }
        if (args.size() == 2) {
            return Completion.complete(args.asString(1), "nightBegin", "nightEnd", "nightDuration",
                    "enable", "skippable", "overrideDuration");
        }

        String field = args.asString(1);
        String value = args.asString(2);
        if (Completion.isCommand(field, "nightBegin", "nightEnd", "nightDuration", "maxNightDuration")) {
            return Completion.isCommand(field, "nightBegin", "nightEnd")
                    ? Completion.completeInt(value, 1, 24000)
                    : Completion.completeInt(value, 1, 86400);
        }

        if (Completion.isCommand(field, "enable", "skippable")) {
            return Completion.completeBoolean(value);
        }

        if (Completion.isCommand(field, "durationMode")) {
            return Completion.complete(value, NightSettings.NightDuration.class);
        }
        return Collections.emptyList();
    }
}
