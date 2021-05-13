package de.eldoria.bloodnight.command.bloodnight.managedeathactions;

import de.eldoria.bloodnight.command.util.CommandUtil;
import de.eldoria.bloodnight.config.Configuration;
import de.eldoria.bloodnight.config.worldsettings.deathactions.PlayerDeathActions;
import de.eldoria.bloodnight.config.worldsettings.deathactions.PotionEffectSettings;
import de.eldoria.bloodnight.core.BloodNight;
import de.eldoria.eldoutilities.conversation.ConversationRequester;
import de.eldoria.eldoutilities.core.EldoUtilities;
import de.eldoria.eldoutilities.inventory.ActionConsumer;
import de.eldoria.eldoutilities.inventory.ActionItem;
import de.eldoria.eldoutilities.inventory.InventoryActions;
import de.eldoria.eldoutilities.items.ItemStackBuilder;
import de.eldoria.eldoutilities.localization.Replacement;
import de.eldoria.eldoutilities.messages.MessageChannel;
import de.eldoria.eldoutilities.messages.MessageType;
import de.eldoria.eldoutilities.simplecommands.EldoCommand;
import de.eldoria.eldoutilities.simplecommands.TabCompleteUtil;
import de.eldoria.eldoutilities.utils.ArgumentUtils;
import de.eldoria.eldoutilities.utils.DataContainerUtil;
import de.eldoria.eldoutilities.utils.Parser;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ManagePlayerDeathActions extends EldoCommand {
    private final Configuration configuration;
    private final ConversationRequester conversationRequester;
    private final BukkitAudiences bukkitAudiences;

    public ManagePlayerDeathActions(Plugin plugin, Configuration configuration, ConversationRequester conversation, BukkitAudiences bukkitAudiences) {
        super(plugin);
        this.configuration = configuration;
        this.conversationRequester = conversation;
        this.bukkitAudiences = bukkitAudiences;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;

        World world = ArgumentUtils.getOrDefault(args, 0, ArgumentUtils::getWorld, player.getWorld());

        if (world == null) {
            messageSender().sendLocalizedError(sender, "error.invalidWorld");
            return true;
        }

        PlayerDeathActions playerDeathActions = configuration.getWorldSettings(world).getDeathActionSettings().getPlayerDeathActions();

        if (args.length < 2) {
            sendPlayerDeathActions(player, world, playerDeathActions);
            return true;
        }

        if (argumentsInvalid(sender, args, 2, "<monster|player> <$syntax.worldName$> [<$syntax.field$> <$syntax.value$>]")) {
            return true;
        }


        String field = args[1];
        String value = ArgumentUtils.getOrDefault(args, 2, "none");

        if ("effects".equalsIgnoreCase(field)) {
            Inventory inventory = Bukkit.createInventory(player, 54,
                    localizer().getMessage("manageDeathActions.inventory.respawnEffects.title"));
            InventoryActions actions = EldoUtilities.getInventoryActions().wrap(player, inventory,
                    e -> {
                        configuration.save();
                        sendPlayerDeathActions(player, world, playerDeathActions);
                    });
            Map<PotionEffectType, PotionEffectSettings> respawnEffects = playerDeathActions.getRespawnEffects();

            // this is always such a mess qwq
            int pos = 0;
            @NotNull PotionEffectType[] values = PotionEffectType.values();
            Arrays.sort(values, Comparator.comparing(PotionEffectType::getName));
            for (@NotNull PotionEffectType potionType : values) {
                @Nullable PotionEffectSettings settings = respawnEffects.get(potionType);
                NamespacedKey valueKey = new NamespacedKey(BloodNight.getInstance(), "value");
                NamespacedKey typeKey = new NamespacedKey(BloodNight.getInstance(), "type");
                actions.addAction(new ActionItem(
                        ItemStackBuilder
                                .of(Material.POTION)
                                .withDisplayName(potionType.getName())
                                .withMetaValue(PotionMeta.class, m -> {
                                    m.setColor(potionType.getColor());
                                })
                                .withLore(String.valueOf(settings == null ? 0 : settings.getDuration()))
                                .withNBTData(c -> {
                                    c.set(typeKey, PersistentDataType.STRING, potionType.getName());
                                    c.set(valueKey, PersistentDataType.INTEGER, settings == null ? 0 : settings.getDuration());
                                })
                                .build(),
                        pos,
                        ActionConsumer.getIntRange(valueKey, 0, 600),
                        stack -> {
                            Optional<Integer> integer = DataContainerUtil.get(stack, valueKey, PersistentDataType.INTEGER);
                            if (!integer.isPresent()) return;
                            Optional<String> optionalName = DataContainerUtil.get(stack, typeKey, PersistentDataType.STRING);
                            optionalName.ifPresent(name -> {
                                PotionEffectType type = PotionEffectType.getByName(name);
                                if (integer.get() == 0) {
                                    respawnEffects.remove(type);
                                    return;
                                }
                                respawnEffects.compute(type, (k, v) -> new PotionEffectSettings(type, integer.get()));
                            });
                        }));
                pos++;
            }

            player.openInventory(inventory);
            return true;
        }

        if ("commands".equalsIgnoreCase(field)) {
            List<String> deathCommands = playerDeathActions.getDeathCommands();
            Inventory inventory = Bukkit.createInventory(player, 54, "Manage Death Commands");
            InventoryActions actions = EldoUtilities.getInventoryActions().wrap(player, inventory, e -> {
                configuration.save();
            });

            int pos = 0;
            for (String deathCommand : deathCommands) {
                actions.addAction(
                        new ActionItem(ItemStackBuilder
                                .of(Material.PAPER)
                                .withDisplayName(deathCommand)
                                .withLore("§2" + localizer().localize("phrase.leftClickChange"),
                                        "§c" + localizer().localize("phrase.rightClickRemove"))
                                .build(),
                                pos,
                                e -> {
                                    switch (e.getClick()) {
                                        case LEFT:
                                        case SHIFT_LEFT:
                                            conversationRequester.requestInput(
                                                    player,
                                                    "phrase.commandPlayer",
                                                    s -> true, 0, i -> {
                                                        deathCommands.set(pos, i);
                                                        player.closeInventory();
                                                        sendPlayerDeathActions(player, world, playerDeathActions);
                                                    });
                                            player.closeInventory();
                                            return;
                                        case RIGHT:
                                        case SHIFT_RIGHT:
                                            deathCommands.remove(pos);
                                            player.closeInventory();
                                            sendPlayerDeathActions(player, world, playerDeathActions);
                                    }
                                },
                                e -> {
                                }));

            }

            player.openInventory(inventory);
            return true;
        }

        if ("addCommand".equalsIgnoreCase(field)) {
            if ("none".equalsIgnoreCase(value)) {
                messageSender().send(MessageChannel.CHAT, MessageType.ERROR, sender, localizer().getMessage("error.noCommand"));
                return true;
            }
            String cmd = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
            playerDeathActions.getDeathCommands().add(String.join(" ", Arrays.copyOfRange(args, 2, args.length)));
            configuration.save();
            sendPlayerDeathActions(player, world, playerDeathActions);
            return true;
        }

        if (TabCompleteUtil.isCommand(field, "loseExp", "loseInv")) {
            OptionalInt optionalInt = Parser.parseInt(value);
            if (!optionalInt.isPresent()) {
                messageSender().sendError(player, localizer().getMessage("error.invalidNumber"));
                return true;
            }
            if (invalidRange(sender, optionalInt.getAsInt(), 0, 100)) {
                return true;
            }
            if ("loseExp".equalsIgnoreCase(field)) {
                playerDeathActions.setLoseExpProbability(optionalInt.getAsInt());
            }
            if ("loseInv".equalsIgnoreCase(field)) {
                playerDeathActions.setLoseInvProbability(optionalInt.getAsInt());
            }
            configuration.save();
            sendPlayerDeathActions(player, world, playerDeathActions);
            return true;
        }

        if ("lightning".equalsIgnoreCase(field)) {
            DeathActionUtil.buildLightningUI(playerDeathActions.getLightningSettings(), player, configuration, localizer(),
                    () -> sendPlayerDeathActions(player, world, playerDeathActions));
            return true;
        }

        if ("shockwave".equalsIgnoreCase(field)) {
            DeathActionUtil.buildShockwaveUI(playerDeathActions.getShockwaveSettings(), player, configuration, localizer(),
                    () -> sendPlayerDeathActions(player, world, playerDeathActions));
            return true;
        }

        sendPlayerDeathActions(player, world, playerDeathActions);
        return true;
    }

    private void sendPlayerDeathActions(Player player, World world, PlayerDeathActions playerDeathActions) {
        String cmd = "/bloodnight deathActions player " + ArgumentUtils.escapeWorldName(world.getName()) + " ";
        TextComponent build = Component.text()
                .append(CommandUtil.getHeader(localizer().getMessage("manageDeathActions.player.title")))
                .append(Component.newline())
                .append(Component.text(localizer().getMessage("field.lightningSettings"), NamedTextColor.AQUA))
                .append(Component.text(" [" + localizer().getMessage("action.change") + "]", NamedTextColor.GREEN)
                        .clickEvent(ClickEvent.runCommand(cmd + "lightning")))
                .append(Component.newline())
                .append(Component.text(localizer().getMessage("field.shockwaveSettings"), NamedTextColor.AQUA))
                .append(Component.text(" [" + localizer().getMessage("action.change") + "]", NamedTextColor.GREEN)
                        .clickEvent(ClickEvent.runCommand(cmd + "shockwave")))
                .append(Component.newline())
                .append(Component.text(localizer().getMessage("field.deathCommands",
                        Replacement.create("COUNT", playerDeathActions.getDeathCommands().size())), NamedTextColor.AQUA))
                .append(Component.text(" [" + localizer().getMessage("action.change") + "]", NamedTextColor.GREEN)
                        .clickEvent(ClickEvent.runCommand(cmd + "commands")))
                .append(Component.text(" [" + localizer().getMessage("action.add") + "]", NamedTextColor.DARK_GREEN)
                        .clickEvent(ClickEvent.suggestCommand(cmd + "addCommand")))
                .append(Component.newline())
                .append(Component.text(localizer().getMessage("field.respawnEffect"), NamedTextColor.AQUA))
                .append(Component.text(" [" + localizer().getMessage("action.change") + "]", NamedTextColor.GREEN)
                        .clickEvent(ClickEvent.runCommand(cmd + "effects")))
                .append(Component.newline())
                .append(Component.text(localizer().getMessage("field.loseInventory") + ": ", NamedTextColor.AQUA))
                .append(Component.text(playerDeathActions.getLoseInvProbability() + "%", NamedTextColor.GOLD))
                .append(Component.text(" [" + localizer().getMessage("action.change") + "]", NamedTextColor.GREEN)
                        .clickEvent(ClickEvent.suggestCommand(cmd + "loseInv ")))
                .append(Component.newline())
                .append(Component.text(localizer().getMessage("field.loseExperience") + ": ", NamedTextColor.AQUA))
                .append(Component.text(playerDeathActions.getLoseExpProbability() + "%", NamedTextColor.GOLD))
                .append(Component.text(" [" + localizer().getMessage("action.change") + "]", NamedTextColor.GREEN)
                        .clickEvent(ClickEvent.suggestCommand(cmd + "loseExp ")))
                .build();

        bukkitAudiences.player(player).sendMessage(build);
    }


    @Override
    public @Nullable
    List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return TabCompleteUtil.completeWorlds(args[0]);
        }

        if (args.length == 2) {
            return TabCompleteUtil.complete(args[1], "effects", "commands", "addCommand", "loseExp", "loseInv", "lightning", "shockwave");
        }

        if (args.length >= 3) {
            if (TabCompleteUtil.isCommand(args[1], "addCommand")) {
                return TabCompleteUtil.completeFreeInput(String.join(" ", Arrays.copyOfRange(args, 2, args.length)), 140, localizer().getMessage("syntax.commandPlayer"), localizer());
            }

            if (TabCompleteUtil.isCommand(args[1], "loseExp", "loseInv")) {
                return TabCompleteUtil.completeInt(args[2], 0, 100, localizer());
            }
            return Collections.emptyList();
        }
        return Collections.emptyList();
    }
}
