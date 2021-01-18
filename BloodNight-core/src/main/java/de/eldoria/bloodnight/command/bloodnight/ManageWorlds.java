package de.eldoria.bloodnight.command.bloodnight;

import de.eldoria.bloodnight.config.Configuration;
import de.eldoria.bloodnight.config.worldsettings.BossBarSettings;
import de.eldoria.bloodnight.config.worldsettings.WorldSettings;
import de.eldoria.bloodnight.core.BloodNight;
import de.eldoria.bloodnight.util.Permissions;
import de.eldoria.bloodnight.command.util.CommandUtil;
import de.eldoria.eldoutilities.simplecommands.EldoCommand;
import de.eldoria.eldoutilities.simplecommands.TabCompleteUtil;
import de.eldoria.eldoutilities.utils.ArgumentUtils;
import de.eldoria.eldoutilities.utils.EnumUtil;
import de.eldoria.eldoutilities.utils.Parser;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

public class ManageWorlds extends EldoCommand {
	private final Configuration configuration;
	private final BukkitAudiences bukkitAudiences;

	public ManageWorlds(Plugin plugin, Configuration configuration) {
		super(plugin);
		this.configuration = configuration;
		bukkitAudiences = BukkitAudiences.create(BloodNight.getInstance());
	}

	// world field value page
	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (denyConsole(sender)) {
			return true;
		}

		if (denyAccess(sender, Permissions.MANAGE_WORLDS)) {
			return true;
		}

		Player player = getPlayerFromSender(sender);
		World world1 = player.getWorld();

		World world = ArgumentUtils.getOrDefault(args, 0, ArgumentUtils::getWorld, world1);


		if (world == null) {
			messageSender().sendError(sender, localizer().getMessage("error.invalidWorld"));
			return true;
		}

		WorldSettings worldSettings = configuration.getWorldSettings(world);

		if (args.length < 2) {
			sendWorldPage(world, sender, 0);
			return true;
		}

		// world field value
		if (argumentsInvalid(sender, args, 3,
				"[" + localizer().getMessage("syntax.worldName") + "] [<"
						+ localizer().getMessage("syntax.field") + "> <"
						+ localizer().getMessage("syntax.value") + ">]")) {
			return true;
		}

		String field = args[1];
		String value = args[2];
		OptionalInt optPage = CommandUtil.findPage(configuration.getWorldSettings().values(), 2,
				w -> w.getWorldName().equalsIgnoreCase(world.getName()));

		if ("page".equalsIgnoreCase(field)) {
			optPage = Parser.parseInt(value);
			if (optPage.isPresent()) {
				sendWorldPage(world, sender, optPage.getAsInt());
			}
			return true;
		}

		if ("bossBar".equalsIgnoreCase(field)) {
			if (!TabCompleteUtil.isCommand(value, "state", "title", "color", "toggleEffect")) {
				messageSender().sendError(sender, localizer().getMessage("error.invalidField"));
			}
			if (argumentsInvalid(sender, args, 4,
					"[" + localizer().getMessage("syntax.worldName") + "] [" +
							"bossBar <"
							+ localizer().getMessage("syntax.field") + "> <"
							+ localizer().getMessage("syntax.value") + ">]")) {
				return true;
			}
			String bossBarValue = args[3];
			BossBarSettings bbs = worldSettings.getBossBarSettings();
			if ("state".equalsIgnoreCase(value)) {
				Optional<Boolean> aBoolean = Parser.parseBoolean(bossBarValue);
				if (!aBoolean.isPresent()) {
					messageSender().sendError(sender, localizer().getMessage("error.invalidBoolean"));
					return true;
				}
				bbs.setEnabled(aBoolean.get());
			}
			if ("title".equalsIgnoreCase(value)) {
				String title = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
				bbs.setTitle(title);
			}
			if ("color".equalsIgnoreCase(value)) {
				BarColor parse = EnumUtil.parse(bossBarValue, BarColor.class);
				if (parse == null) {
					messageSender().sendError(sender, localizer().getMessage("error.invalidValue"));
					return true;
				}
				bbs.setColor(parse);
			}
			if ("toggleEffect".equalsIgnoreCase(value)) {
				BarFlag parse = EnumUtil.parse(bossBarValue, BarFlag.class);
				if (parse == null) {
					messageSender().sendError(sender, localizer().getMessage("error.invalidValue"));
					return true;
				}
				bbs.toggleEffect(parse);
			}

			sendWorldPage(world, sender, optPage.getAsInt());
			configuration.save();
			return true;
		}

		if (TabCompleteUtil.isCommand(field, "state", "creeperBlockDamage", "manageCreeperAlways")) {
			Optional<Boolean> aBoolean = Parser.parseBoolean(value);
			if (!aBoolean.isPresent()) {
				messageSender().sendError(sender, "invalid boolean");
				return true;
			}
			if ("state".equalsIgnoreCase(field)) {
				worldSettings.setEnabled(aBoolean.get());
			}
			if ("creeperBlockDamage".equalsIgnoreCase(field)) {
				worldSettings.setCreeperBlockDamage(aBoolean.get());
			}
			if ("manageCreeperAlways".equalsIgnoreCase(field)) {
				worldSettings.setAlwaysManageCreepers(aBoolean.get());
			}
			sendWorldPage(world, sender, optPage.getAsInt());
			configuration.save();
			return true;
		}
		messageSender().sendError(sender, localizer().getMessage("error.invalidField"));
		return true;
	}

	private void sendWorldPage(World world, CommandSender sender, int page) {
		TextComponent component = CommandUtil.getPage(
				new ArrayList<>(configuration.getWorldSettings().values()),
				page,
				2, 7,
				entry -> {
					String cmd = "/bloodnight manageWorlds " + ArgumentUtils.escapeWorldName(entry.getWorldName()) + " ";
					BossBarSettings bbs = entry.getBossBarSettings();
					return Component.text()
							// World State
							.append(Component.text(entry.getWorldName(), NamedTextColor.GOLD, TextDecoration.BOLD))
							.append(Component.text("  "))
							.append(CommandUtil.getBooleanField(entry.isEnabled(),
									cmd + "state {bool} ",
									"",
									localizer().getMessage("state.enabled"),
									localizer().getMessage("state.disabled")))
							.append(Component.newline())
							.append(CommandUtil.getBooleanField(entry.isCreeperBlockDamage(),
									cmd + "creeperBlockDamage {bool} ",
									localizer().getMessage("field.creeperBlockDamage"),
									localizer().getMessage("state.enabled"),
									localizer().getMessage("state.disabled")))
							.append(Component.newline())
							.append(CommandUtil.getBooleanField(entry.isAlwaysManageCreepers(),
									cmd + "manageCreeperAlways {bool} ",
									localizer().getMessage("field.alwaysManageCreepers"),
									localizer().getMessage("state.enabled"),
									localizer().getMessage("state.disabled")))
							.append(Component.newline()).append(Component.text("  "))
							// boss bar state
							.append(Component.text(localizer().getMessage("field.bossBarSettings") + ": ", NamedTextColor.AQUA))
							.append(CommandUtil.getBooleanField(bbs.isEnabled(),
									cmd + "bossBar state {bool} ",
									"",
									localizer().getMessage("state.enabled"),
									localizer().getMessage("state.disabled")))
							.append(Component.newline()).append(Component.text("  "))
							// title
							.append(Component.text(localizer().getMessage("field.title") + ": ", NamedTextColor.AQUA))
							.append(Component.text(bbs.getTitle(), NamedTextColor.GOLD))
							.append(Component.text(" [" + localizer().getMessage("action.change") + "] ", NamedTextColor.GREEN)
									.clickEvent(ClickEvent.suggestCommand(cmd + "bossBar title " + bbs.getTitle().replace("§", "&"))))
							.append(Component.newline()).append(Component.text("  "))
							// Color
							.append(Component.text(localizer().getMessage("field.color") + ": ", NamedTextColor.AQUA))
							.append(Component.text(bbs.getColor().toString(), toKyoriColor(bbs.getColor())))
							.append(Component.text(" [" + localizer().getMessage("action.change") + "] ", NamedTextColor.GREEN)
									.clickEvent(ClickEvent.suggestCommand(cmd + "bossBar color ")))
							.append(Component.newline()).append(Component.text("  "))
							// Effects
							.append(Component.text(localizer().getMessage("field.effects") + ": ", NamedTextColor.AQUA))
							.append(CommandUtil.getToggleField(bbs.isEffectEnabled(BarFlag.CREATE_FOG),
									cmd + "bossBar toggleEffect CREATE_FOG",
									localizer().getMessage("state.fog")))
							.append(Component.space())
							.append(CommandUtil.getToggleField(bbs.isEffectEnabled(BarFlag.DARKEN_SKY),
									cmd + "bossBar toggleEffect DARKEN_SKY",
									localizer().getMessage("state.darkenSky")))
							.append(Component.space())
							.append(CommandUtil.getToggleField(bbs.isEffectEnabled(BarFlag.PLAY_BOSS_MUSIC),
									cmd + "bossBar toggleEffect PLAY_BOSS_MUSIC",
									localizer().getMessage("state.music")))
							.build();
				},
				localizer().getMessage("manageWorlds.title"),
				"/bloodNight manageWorlds " + ArgumentUtils.escapeWorldName(world) + " page {page}");

		bukkitAudiences.sender(sender).sendMessage(Identity.nil(), component);
	}


	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
		if (args.length == 1) {
			return TabCompleteUtil.completeWorlds(args[0]);
		}
		if (args.length == 2) {
			return TabCompleteUtil.complete(args[1], "bossBar", "state", "creeperBlockDamage", "manageCreeperAlways");
		}

		String field = args[1];
		if ("bossBar".equalsIgnoreCase(field)) {
			if (args.length == 3) {
				return TabCompleteUtil.complete(args[2], "state", "title", "color", "toggleEffect");
			}
			String bossField = args[2];
			String bossValue = args[3];

			if ("state".equalsIgnoreCase(bossField)) {
				return TabCompleteUtil.completeBoolean(bossValue);
			}
			if ("title".equalsIgnoreCase(bossField)) {
				return TabCompleteUtil.completeFreeInput(ArgumentUtils.getRangeAsString(args, 3), 16, localizer().getMessage("field.title"), localizer());
			}
			if ("color".equalsIgnoreCase(bossField)) {
				return TabCompleteUtil.complete(bossValue, BarColor.class);
			}
			if ("toggleEffect".equalsIgnoreCase(bossField)) {
				return TabCompleteUtil.complete(bossValue, BarFlag.class);
			}
			return Collections.emptyList();
		}

		if (TabCompleteUtil.isCommand(field, "state", "creeperBlockDamage", "manageCreeperAlways")) {
			return TabCompleteUtil.completeBoolean(args[2]);
		}
		return Collections.emptyList();
	}

	private TextColor toKyoriColor(BarColor color) {
		switch (color) {
			case PINK:
				return TextColor.color(248, 24, 148);
			case BLUE:
				return NamedTextColor.BLUE;
			case RED:
				return NamedTextColor.RED;
			case GREEN:
				return NamedTextColor.GREEN;
			case YELLOW:
				return NamedTextColor.YELLOW;
			case PURPLE:
				return NamedTextColor.LIGHT_PURPLE;
			case WHITE:
				return NamedTextColor.WHITE;
			default:
				throw new IllegalStateException("Unexpected value: " + color);
		}
	}
}
