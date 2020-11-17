package de.eldoria.bloodnight.command.bloodnight;

import de.eldoria.bloodnight.config.Configuration;
import de.eldoria.bloodnight.core.manager.NightManager;
import de.eldoria.bloodnight.util.Permissions;
import de.eldoria.eldoutilities.localization.Replacement;
import de.eldoria.eldoutilities.simplecommands.EldoCommand;
import de.eldoria.eldoutilities.utils.ArgumentUtils;
import de.eldoria.eldoutilities.utils.ArrayUtil;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ForceNight extends EldoCommand {
	private final NightManager nightManager;
	private final Configuration configuration;

	public ForceNight(Plugin plugin, NightManager nightManager, Configuration configuration) {
		super(plugin);
		this.nightManager = nightManager;
		this.configuration = configuration;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (denyAccess(sender, Permissions.FORCE_NIGHT)) {
			return true;
		}

		World world = null;
		if (sender instanceof Player) {
			Player player = (Player) sender;
			world = player.getWorld();
		} else {
			if (argumentsInvalid(sender, args, 1, "[" + localizer().getMessage("syntax.worldName") + "]")) {
				return true;
			}
		}

		world = ArgumentUtils.getOrDefault(args, 0, ArgumentUtils::getWorld, world);

		if (world == null) {
			messageSender().sendError(sender, localizer().getMessage("error.invalidWorld"));
			return true;
		}

		boolean enabled = configuration.getWorldSettings(world).isEnabled();
		if (!enabled) {
			messageSender().sendError(sender, localizer().getMessage("error.worldNotEnabled",
					Replacement.create("WORLD", world.getName()).addFormatting('6')));
			return true;
		}
		if (!nightManager.getBloodWorlds().contains(world)) {
			nightManager.forceNight(world);
			messageSender().sendMessage(sender, localizer().getMessage("forceNight.enabeld",
					Replacement.create("WORLD", world.getName()).addFormatting('6')));
		} else {
			messageSender().sendError(sender, localizer().getMessage("forceNight.alreadyActive",
					Replacement.create("WORLD", world.getName()).addFormatting('6')));
		}
		return true;
	}

	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
		if (args.length == 1) {
			String[] strings = nightManager.getObservedWorlds().stream().map(World::getName).toArray(String[]::new);
			return ArrayUtil.startingWithInArray(args[0], strings).collect(Collectors.toList());
		}
		return Collections.emptyList();
	}

}
