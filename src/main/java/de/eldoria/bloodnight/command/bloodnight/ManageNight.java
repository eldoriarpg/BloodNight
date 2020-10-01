package de.eldoria.bloodnight.command.bloodnight;

import de.eldoria.bloodnight.command.util.CommandUtil;
import de.eldoria.bloodnight.command.util.KyoriColors;
import de.eldoria.bloodnight.config.Configuration;
import de.eldoria.bloodnight.config.NightSettings;
import de.eldoria.bloodnight.config.WorldSettings;
import de.eldoria.bloodnight.core.BloodNight;
import de.eldoria.eldoutilities.localization.Localizer;
import de.eldoria.eldoutilities.messages.MessageSender;
import de.eldoria.eldoutilities.simplecommands.EldoCommand;
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
        NightSettings nightSettings = worldSettings.getNightSettings();
        if (args.length < 2) {
            sendNightSettings(sender, worldSettings);
            return true;
        }

        if (argumentsInvalid(sender, args, 3, "world field value")) {
            return true;
        }

        String cmd = args[1];
        String value = args[2].replace(",", ".");
        OptionalDouble optionalDouble = Parser.parseDouble(value);
        OptionalInt optionalInt = Parser.parseInt(value);
        Optional<Boolean> optionalBoolean = Parser.parseBoolean(value);

        if (ArrayUtil.arrayContains(new String[] {"monsterDamage", "playerDamage", "experience", "drops"}, cmd)) {
            if (!optionalDouble.isPresent()) {
                messageSender().sendError(sender, "invalid number");
                return true;
            }

            if ("monsterDamage".equalsIgnoreCase(cmd)) {
                nightSettings.setMonsterDamageMultiplier(optionalDouble.getAsDouble());
            }
            if ("playerDamage".equalsIgnoreCase(cmd)) {
                nightSettings.setPlayerDamageMultiplier(optionalDouble.getAsDouble());
            }
            if ("experience".equalsIgnoreCase(cmd)) {
                nightSettings.setExperienceMultiplier(optionalDouble.getAsDouble());
            }
            if ("drops".equalsIgnoreCase(cmd)) {
                nightSettings.setDropMultiplier(optionalDouble.getAsDouble());
            }
            configuration.safeConfig();
            sendNightSettings(sender, worldSettings);
            return true;
        }

        if (ArrayUtil.arrayContains(new String[] {"enable", "forcePhantoms", "skippable", "overrideDuration"}, cmd)) {
            if (!optionalBoolean.isPresent()) {
                messageSender().sendError(sender, "invalid boolean");
                return true;
            }

            if ("enable".equalsIgnoreCase(cmd)) {
                worldSettings.setEnabled(optionalBoolean.get());
            }
            if ("forcePhantoms".equalsIgnoreCase(cmd)) {
                nightSettings.setForcePhantoms(optionalBoolean.get());
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
                messageSender().sendError(sender, "invalid number");
                return true;
            }
            if ("nightBegin".equalsIgnoreCase(cmd)) {
                nightSettings.setNightBegin(optionalInt.getAsInt());
            }
            if ("nightEnd".equalsIgnoreCase(cmd)) {
                nightSettings.setNightEnd(optionalInt.getAsInt());
            }
            if ("nightDuration".equalsIgnoreCase(cmd)) {
                nightSettings.setNightDuration(optionalInt.getAsInt());
            }
            configuration.safeConfig();
            sendNightSettings(sender, worldSettings);
            return true;
        }
        messageSender().sendError(player, "invalid field");
        return true;
    }

    private void sendNightSettings(CommandSender sender, WorldSettings worldSettings) {
        NightSettings nightSettings = worldSettings.getNightSettings();
        String cmd = "/bloodnight manageNight " + worldSettings.getWorldName() + " ";
        TextComponent.Builder builder = TextComponent.builder()
                .append(TextComponent.newline())
                .append(CommandUtil.getHeader("Night Setting of " + worldSettings.getWorldName()))
                .append(TextComponent.newline())
                // World state
                .append(CommandUtil.getBooleanField(
                        worldSettings.isEnabled(),
                        cmd + "enable {bool}",
                        "Active",
                        "enabled",
                        "disabled"))
                .append(TextComponent.newline())
                // Monster damage
                .append(TextComponent.builder("Monster Damage: ", KyoriColors.AQUA))
                .append(TextComponent.builder(nightSettings.getMonsterDamageMultiplier() + "x ", KyoriColors.GOLD))
                .append(TextComponent.builder("[change]", KyoriColors.GREEN)
                        .clickEvent(ClickEvent.suggestCommand(cmd + "monsterDamage ")))
                .append(TextComponent.newline())
                // Player damage
                .append(TextComponent.builder("Player Damage: ", KyoriColors.AQUA))
                .append(TextComponent.builder(nightSettings.getPlayerDamageMultiplier() + "x ", KyoriColors.GOLD))
                .append(TextComponent.builder("[change]", KyoriColors.GREEN)
                        .clickEvent(ClickEvent.suggestCommand(cmd + "playerDamage ")))
                .append(TextComponent.newline())
                // experience multiply
                .append(TextComponent.builder("Experience Amount: ", KyoriColors.AQUA))
                .append(TextComponent.builder(nightSettings.getExperienceMultiplier() + "x ", KyoriColors.GOLD))
                .append(TextComponent.builder("[change]", KyoriColors.GREEN)
                        .clickEvent(ClickEvent.suggestCommand(cmd + "experience ")))
                .append(TextComponent.newline())
                // drop multiply
                .append(TextComponent.builder("Drop Amount: ", KyoriColors.AQUA))
                .append(TextComponent.builder(nightSettings.getDropMultiplier() + "x ", KyoriColors.GOLD))
                .append(TextComponent.builder("[change]", KyoriColors.GREEN)
                        .clickEvent(ClickEvent.suggestCommand(cmd + "drops ")))
                .append(TextComponent.newline())
                // force phantoms
                .append(CommandUtil.getBooleanField(nightSettings.isForcePhantoms(),
                        cmd + "forcePhantoms {bool}",
                        "Force Phantoms", "enabled", "disabled"))
                .append(TextComponent.newline())
                // skippable
                .append(CommandUtil.getBooleanField(nightSettings.isSkippable(),
                        cmd + "skippable {bool}",
                        "Skippable", "allow", "deny"))
                .append(TextComponent.newline())
                // night begin
                .append(TextComponent.builder("Night Begin: ", KyoriColors.AQUA))
                .append(TextComponent.builder(nightSettings.getNightBegin() + " ", KyoriColors.GOLD))
                .append(TextComponent.builder("[change]", KyoriColors.GREEN)
                        .clickEvent(ClickEvent.suggestCommand(cmd + "nightBegin ")))
                .append(TextComponent.newline())
                // night end
                .append(TextComponent.builder("Night End: ", KyoriColors.AQUA))
                .append(TextComponent.builder(nightSettings.getNightEnd() + " ", KyoriColors.GOLD))
                .append(TextComponent.builder("[change]", KyoriColors.GREEN)
                        .clickEvent(ClickEvent.suggestCommand(cmd + "nightEnd ")))
                .append(TextComponent.newline())
                // override night duration
                .append(CommandUtil.getBooleanField(nightSettings.isOverrideNightDuration(), cmd + " overrideDuration {bool}",
                        "Override Duration", "enabled", "disabled"));
        if (nightSettings.isOverrideNightDuration()) {
            //night duration
            builder.append(TextComponent.newline())
                    .append(TextComponent.builder("Night duration: ", KyoriColors.AQUA))
                    .append(TextComponent.builder(nightSettings.getNightDuration() + "x ", KyoriColors.GOLD))
                    .append(TextComponent.builder("[change]", KyoriColors.GREEN)
                            .clickEvent(ClickEvent.suggestCommand(cmd + "nightDuration ")));
        }
        builder.append(TextComponent.newline());

        bukkitAudiences.audience(sender).sendMessage(builder.build());
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return super.onTabComplete(sender, command, alias, args);
    }
}
