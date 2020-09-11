package de.eldoria.bloodnight.command;

import de.eldoria.bloodnight.config.Configuration;
import de.eldoria.bloodnight.listener.NightListener;
import de.eldoria.bloodnight.util.Permissions;
import de.eldoria.eldoutilities.localization.Localizer;
import de.eldoria.eldoutilities.localization.Replacement;
import de.eldoria.eldoutilities.messages.MessageSender;
import de.eldoria.eldoutilities.utils.ArrayUtil;
import de.eldoria.eldoutilities.utils.Parser;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;

public class BloodNightCommand implements TabExecutor {
    private final Configuration configuration;
    private final Localizer localizer;
    private final Plugin plugin;
    private final NightListener nightListener;
    private final MessageSender messageSender;

    public BloodNightCommand(Configuration configuration, Localizer localizer, Plugin plugin,
                             NightListener nightListener, MessageSender messageSender) {
        this.configuration = configuration;
        this.localizer = localizer;
        this.plugin = plugin;
        this.nightListener = nightListener;
        this.messageSender = messageSender;
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
                        Replacement.create("%WORLD%", arguments[0]).addFormatting('6')));
                return true;
            }

            if (configuration.getNightSettings().getWorlds().contains(world.getName())) {
                messageSender.sendMessage(player, localizer.getMessage("addWorld.alreadyRegistered",
                        Replacement.create("%WORLD%", world.getName()).addFormatting('6')));
                return true;
            }

            nightListener.registerWorld(world);
            configuration.getNightSettings().getWorlds().add(world.getName());
            configuration.safeConfig();

            messageSender.sendMessage(player, localizer.getMessage("addWorld.added",
                    Replacement.create("%WORLD%", world.getName()).addFormatting('6')));
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

            World world = Bukkit.getWorld(arguments[0]);

            if (world == null) {
                messageSender.sendError(player, localizer.getMessage("error.invalidWorld",
                        Replacement.create("%WORLD%", arguments[0]).addFormatting('6')));
                return true;
            }

            boolean removed = nightListener.unregisterWorld(world);

            if (removed) {
                messageSender.sendError(player, localizer.getMessage("removeWorld.notRegistered",
                        Replacement.create("%WORLD%", world.getName()).addFormatting('6')));
            } else {
                messageSender.sendMessage(player, localizer.getMessage("removeWorld.removed",
                        Replacement.create("%WORLD%", world.getName()).addFormatting('6')));
            }
            configuration.safeConfig();
            return true;
        }

        if ("setLanguage".equalsIgnoreCase(cmd)) {
            if (denyAccess(player, Permissions.ADMIN)) {
                return true;
            }

            if (argumentsInvalid(player, arguments, 1,
                    "<" + localizer.getMessage("syntax.languageCode") + ">")) {
                return true;
            }

            boolean contains = ArrayUtils.contains(localizer.getIncludedLocales(), arguments[0]);
            if (contains) {
                messageSender.sendError(player, localizer.getMessage("setLanguage.notValid"));
            } else {
                localizer.setLocale(arguments[0]);
                messageSender.sendError(player, localizer.getMessage("setLanguage.setLanguage",
                        Replacement.create("%LANG%", arguments[0]).addFormatting('6')));
            }
            configuration.safeConfig();
            return true;
        }

        if ("forcePhantoms".equalsIgnoreCase(cmd)) {
            if (denyAccess(player, Permissions.ADMIN)) {
                return true;
            }

            if (argumentsInvalid(player, arguments, 1,
                    "<" + localizer.getMessage("syntax.boolean") + ">")) {
                return true;
            }

            Optional<Boolean> aBoolean = Parser.parseBoolean(arguments[0]);

            if (!aBoolean.isPresent()) {
                messageSender.sendError(player, localizer.getMessage("error.invalidBoolean"));
                return true;
            } else {
                configuration.getNightSettings().setForcePhantoms(aBoolean.get());
            }

            if (aBoolean.get()) {
                messageSender.sendMessage(player, localizer.getMessage("forcePhantoms.true"));
            } else {
                messageSender.sendMessage(player, localizer.getMessage("forcePhantoms.false"));
            }
            configuration.safeConfig();
            return true;
        }
        if ("setSkippable".equalsIgnoreCase(cmd)) {
            if (denyAccess(player, Permissions.ADMIN)) {
                return true;
            }

            if (argumentsInvalid(player, arguments, 1,
                    "<" + localizer.getMessage(("syntax.boolean")) + ">")) {
                return true;
            }

            Optional<Boolean> aBoolean = Parser.parseBoolean(arguments[0]);

            if (!aBoolean.isPresent()) {
                messageSender.sendError(player, localizer.getMessage("error.invalidBoolean"));
                return true;
            }

            configuration.getNightSettings().setSkippable(aBoolean.get());

            if (aBoolean.get()) {
                messageSender.sendMessage(player, localizer.getMessage("setSkippable.true"));
            } else {
                messageSender.sendMessage(player, localizer.getMessage("setSkippable.false"));
            }
            configuration.safeConfig();
            return true;
        }

        if ("setNightBegin".equalsIgnoreCase(cmd)) {
            if (denyAccess(player, Permissions.ADMIN)) {
                return true;
            }

            if (argumentsInvalid(player, arguments, 1,
                    "<0-23999>")) {
                return true;
            }

            OptionalInt optionalInt = Parser.parseInt(arguments[0]);

            if (!optionalInt.isPresent()) {
                messageSender.sendError(player, localizer.getMessage("error.invalidNumber"));
                return true;
            }

            int asInt = optionalInt.getAsInt();

            if (asInt > 23999 || asInt < 0) {
                messageSender.sendError(player, localizer.getMessage("error.outOfRange",
                        Replacement.create("%MIN%", 0),
                        Replacement.create("%MAX%", 23999)));
                return true;
            }

            messageSender.sendMessage(player, localizer.getMessage("setNightBegin.set", Replacement.create("%VALUE%", asInt)));

            configuration.getNightSettings().setNightBegin(asInt);
            configuration.safeConfig();
            return true;
        }
        if ("setNightEnd".equalsIgnoreCase(cmd)) {
            if (denyAccess(player, Permissions.ADMIN)) {
                return true;
            }

            if (argumentsInvalid(player, arguments, 1,
                    "<0-23999>")) {
                return true;
            }

            OptionalInt optionalInt = Parser.parseInt(arguments[0]);

            if (!optionalInt.isPresent()) {
                messageSender.sendError(player, "error.invalidNumber");
                return true;
            }

            int asInt = optionalInt.getAsInt();

            if (asInt > 23999 || asInt < 0) {
                messageSender.sendError(player, localizer.getMessage("error.outOfRange",
                        Replacement.create("%MIN%", 0),
                        Replacement.create("%MAX%", 23999)));
                return true;
            }

            messageSender.sendMessage(player, localizer.getMessage("setNightEnd.set", Replacement.create("%VALUE%", asInt)));

            configuration.getNightSettings().setNightEnd(asInt);
            configuration.safeConfig();
            return true;
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
                        startingWithInArray(args[1], configuration.getNightSettings().getWorlds().toArray(new String[0]))
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
