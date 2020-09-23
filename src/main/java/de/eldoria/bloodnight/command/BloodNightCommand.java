package de.eldoria.bloodnight.command;

import de.eldoria.bloodnight.config.Configuration;
import de.eldoria.bloodnight.config.WorldSettings;
import de.eldoria.bloodnight.core.mobfactory.MobFactory;
import de.eldoria.bloodnight.core.mobfactory.SpecialMobRegistry;
import de.eldoria.bloodnight.listener.MobModifier;
import de.eldoria.bloodnight.listener.NightListener;
import de.eldoria.bloodnight.specialmobs.SpecialMobUtil;
import de.eldoria.bloodnight.util.Permissions;
import de.eldoria.eldoutilities.localization.Localizer;
import de.eldoria.eldoutilities.localization.Replacement;
import de.eldoria.eldoutilities.messages.MessageSender;
import de.eldoria.eldoutilities.utils.ArrayUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class BloodNightCommand implements TabExecutor {
    private final Configuration configuration;
    private final Localizer localizer;
    private final Plugin plugin;
    private final NightListener nightListener;
    private final MessageSender messageSender;
    private final MobModifier mobModifier;

    public BloodNightCommand(Configuration configuration, Localizer localizer, Plugin plugin,
                             NightListener nightListener, MobModifier mobModifier) {
        this.configuration = configuration;
        this.localizer = localizer;
        this.plugin = plugin;
        this.nightListener = nightListener;
        this.messageSender = MessageSender.get(plugin);
        this.mobModifier = mobModifier;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = null;
        if (sender instanceof Player) {
            player = (Player) sender;
        }


        if (args.length == 0 || "about".equalsIgnoreCase(args[0])) {
            PluginDescriptionFile descr = plugin.getDescription();
            String info = localizer.getMessage("about",
                    Replacement.create("PLUGIN_NAME", "Big Doors Opener").addFormatting('b'),
                    Replacement.create("AUTHORS", String.join(", ", descr.getAuthors())).addFormatting('b'),
                    Replacement.create("VERSION", descr.getVersion()).addFormatting('b'),
                    Replacement.create("WEBSITE", descr.getWebsite()).addFormatting('b'),
                    Replacement.create("DISCORD", "https://discord.gg/rfRuUge").addFormatting('b'));
            messageSender.sendMessage(player, info);
            return true;
        }

        String cmd = args[0];

        String[] arguments = new String[0];
        if (args.length > 1) {
            arguments = Arrays.copyOfRange(args, 1, args.length - 1);
        }

        if ("help".equalsIgnoreCase(cmd)) {
            messageSender.sendMessage(player, "help" + localizer.getMessage("help.help") + "\n"
                    + "§6about§r" + localizer.getMessage("help.about") + "\n"
                    + "§6addWorld§r" + localizer.getMessage("help.addWorld") + "\n"
                    + "§6removeWorld§r" + localizer.getMessage("help.removeWorld") + "\n"
                    + "§6setLanguage§r" + localizer.getMessage("help.setLanguage") + "\n"
                    + "§6forcePhantoms§r" + localizer.getMessage("help.forcePhantoms") + "\n"
                    + "§6setSkippable§r" + localizer.getMessage("help.setSkippable") + "\n"
                    + "§6setNightBegin§r" + localizer.getMessage("help.setNightBegin") + "\n"
                    + "§6setNightEnd§r" + localizer.getMessage("help.setNightEnd")
            );
            return true;
        }

        // Enable, disable Worlds. Register, unregister Worlds
        // Calls addWorld, removeWorld, enableBloodNight <world>, disableBloodNight <world>
        if ("manageWorlds".equalsIgnoreCase(cmd)) {
            if (denyAccess(player, Permissions.ADMIN)) {
                return true;
            }
        }

        // Show Current world settings. Shows NightSettings and NightSelection and active special mobs
        if ("worldInfo".equalsIgnoreCase(cmd)) {

        }

        if ("spawnMob".equalsIgnoreCase(cmd)) {
            if (player == null) return true;
            if (nightListener.isBloodNightActive(player.getWorld())) {
                Block targetBlock = player.getTargetBlock(null, 100);
                if (targetBlock.getType() == Material.AIR) {
                    messageSender.sendError(player, "No Block in sight.");
                    return true;
                }

                Optional<MobFactory> mobFactoryByName = SpecialMobRegistry.getMobFactoryByName(args[1]);

                if (!mobFactoryByName.isPresent()) {
                    messageSender.sendError(player, "Invalid mob type");
                    return true;
                }

                MobFactory mobFactory = mobFactoryByName.get();

                Entity entity = SpecialMobUtil.spawnAndTagEntity(targetBlock.getLocation().add(0, 1, 0), mobFactory.getEntityType());
                mobModifier.wrapMob(entity, mobFactory);
            } else {
                messageSender.sendError(player, "no blood night active");
            }

            return true;
        }

        if ("addWorld".equalsIgnoreCase(cmd)) {
            if (denyAccess(player, Permissions.ADMIN)) {
                return true;
            }

            if (argumentsInvalid(player, arguments, 1,
                    "<" + localizer.getMessage("syntax.worldName") + ">")) {
                return true;
            }

            World world = Bukkit.getWorld(arguments[0]);

            if (world == null) {
                messageSender.sendError(player, localizer.getMessage("error.invalidWorld",
                        Replacement.create("WORLD", arguments[0]).addFormatting('6')));
                return true;
            }

            if (configuration.getWorldSettings(world.getName()) != null) {
                messageSender.sendMessage(player, localizer.getMessage("addWorld.alreadyRegistered",
                        Replacement.create("WORLD", world.getName()).addFormatting('6')));
                return true;
            }

            nightListener.registerWorld(world);
            configuration.addWorldSettings(world);
            configuration.safeConfig();

            messageSender.sendMessage(player, localizer.getMessage("addWorld.added",
                    Replacement.create("WORLD", world.getName()).addFormatting('6')));
            return true;
        }

        if ("removeWorld".equalsIgnoreCase(cmd)) {
            if (denyAccess(player, Permissions.ADMIN)) {
                return true;
            }

            if (argumentsInvalid(player, arguments, 1,
                    "<" + localizer.getMessage("syntax.worldName") + ">")) {
                return true;
            }

            WorldSettings worldSettings = configuration.getWorldSettings(arguments[0]);

            if (worldSettings == null) {
                messageSender.sendError(player, localizer.getMessage("error.invalidWorld",
                        Replacement.create("WORLD", arguments[0]).addFormatting('6')));
                return true;
            }

            World world = Bukkit.getWorld(worldSettings.getWorldName());

            boolean removed = nightListener.unregisterWorld(world);

            messageSender.sendMessage(player, localizer.getMessage("removeWorld.removed",
                    Replacement.create("WORLD", worldSettings.getWorldName()).addFormatting('6')));

            configuration.safeConfig();
            return true;
        }

        // cancels a active blood night. cancel <world>
        if ("cancel".equalsIgnoreCase(cmd)) {

        }

        // force a world to activate a blood night
        // only when its night in this world.
        if ("force".equalsIgnoreCase(cmd)) {

        }

        if ("reload".equalsIgnoreCase(cmd)) {
            configuration.reload();
            nightListener.reload();
            messageSender.sendMessage(player, localizer.getMessage("reload.success"));
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length > 5) {
            return Collections.singletonList("(╯°□°）╯︵ ┻━┻");
        }

        String cmd = args[0];

        if (args.length == 1) {
            return ArrayUtil.startingWithInArray(cmd,
                    new String[] {"help", "about",
                            "addWorld", "removeWorld",
                            "setLanguage", "forcePhantoms", "setSkippable",
                            "setNightBegin", "setNightEnd", "reload"})
                    .collect(Collectors.toList());
        }

        if ("help".equalsIgnoreCase(cmd)) {
            return Collections.emptyList();
        }
        if ("reload".equalsIgnoreCase(cmd)) {
            return Collections.emptyList();
        }

        if ("about".equalsIgnoreCase(cmd)) {
            return Collections.emptyList();
        }

        if ("addWorld".equalsIgnoreCase(cmd)) {
            if (args.length == 2) {

                return ArrayUtil.startingWithInArray(args[1], Bukkit.getServer().getWorlds()
                        .stream()
                        .map(World::getName)
                        .toArray(String[]::new))
                        .collect(Collectors.toList());
            }
        }

        if ("removeWorld".equalsIgnoreCase(cmd)) {
            if (args.length == 2) {
                return ArrayUtil.
                        startingWithInArray(args[1], configuration.getWorldSettings().keySet().toArray(new String[0]))
                        .collect(Collectors.toList());
            }
        }

        if ("setLanguage".equalsIgnoreCase(cmd)) {
            if (args.length == 2) {
                return ArrayUtil.startingWithInArray(args[1], localizer.getIncludedLocales()).collect(Collectors.toList());
            }
        }

        if ("forcePhantoms".equalsIgnoreCase(cmd)) {
            if (args.length == 2) {
                return ArrayUtil.startingWithInArray(args[1], new String[] {"true", "false"}).collect(Collectors.toList());
            }
        }
        if ("setSkippable".equalsIgnoreCase(cmd)) {
            if (args.length == 2) {
                return ArrayUtil.startingWithInArray(args[1], new String[] {"true", "false"}).collect(Collectors.toList());
            }
        }
        if ("setNightBegin".equalsIgnoreCase(cmd)) {
            if (args.length == 2) {
                return Collections.singletonList("0-23999");
            }
        }
        if ("setNightEnd".equalsIgnoreCase(cmd)) {
            if (args.length == 2) {
                return Collections.singletonList("0-23999");
            }
        }
        if ("spawnMob".equalsIgnoreCase(cmd)) {
            if (args.length == 2) {
                return ArrayUtil.startingWithInArray(args[1], SpecialMobRegistry.getRegisteredMobs().stream()
                        .map(MobFactory::getMobName).toArray(String[]::new))
                        .collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
    }

    /**
     * Checks if the provided arguments are invalid.
     *
     * @param player player which executed the command.
     * @param args   arguments to check
     * @param length min amount of arguments.
     * @param syntax correct syntax
     * @return true if the arguments are invalid
     */
    private boolean argumentsInvalid(Player player, String[] args, int length, String syntax) {
        if (args.length < length) {
            messageSender.sendError(player, localizer.getMessage("error.invalidArguments",
                    Replacement.create("SYNTAX", syntax).addFormatting('6')));
            return true;
        }
        return false;
    }

    private boolean denyAccess(CommandSender sender, String... permissions) {
        return denyAccess(sender, false, permissions);
    }

    private boolean denyAccess(CommandSender sender, boolean silent, String... permissions) {
        if (sender == null) {
            return false;
        }

        Player player = null;

        if (sender instanceof Player) {
            player = (Player) sender;
        }

        if (player == null) {
            return false;
        }
        for (String permission : permissions) {
            if (player.hasPermission(permission)) {
                return false;
            }
        }
        if (!silent) {
            messageSender.sendMessage(player, localizer.getMessage("error.permission",
                    Replacement.create("PERMISSION", String.join(", ", permissions)).addFormatting('6')));
        }
        return true;
    }

}
