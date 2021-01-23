package de.eldoria.bloodnight.command.bloodnight.managedeathactions;

import de.eldoria.bloodnight.command.util.CommandUtil;
import de.eldoria.bloodnight.config.Configuration;
import de.eldoria.bloodnight.config.worldsettings.deathactions.PlayerDeathActions;
import de.eldoria.bloodnight.config.worldsettings.deathactions.PotionEffectSettings;
import de.eldoria.bloodnight.config.worldsettings.deathactions.subsettings.LightningSettings;
import de.eldoria.bloodnight.config.worldsettings.deathactions.subsettings.ShockwaveSettings;
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
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
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
            // TODO
            Inventory inventory = Bukkit.createInventory(player, 56,
                    localizer().getMessage("manageDeathActions.inventory.respawnEffects.title"));
            InventoryActions actions = EldoUtilities.getInventoryActions().wrap(player, inventory,
                    e -> {
                        configuration.save();
                        sendPlayerDeathActions(player, world, playerDeathActions);
                    });
            Map<PotionType, PotionEffectSettings> respawnEffects = playerDeathActions.getRespawnEffects();

            // this is always such a mess qwq
            int pos = 0;
            for (PotionType potionType : PotionType.values()) {
                if (potionType.getEffectType() == null) continue;
                pos++;
                @Nullable PotionEffectSettings settings = respawnEffects.get(potionType);
                NamespacedKey valueKey = new NamespacedKey(BloodNight.getInstance(), "value");
                actions.addAction(new ActionItem(
                        ItemStackBuilder
                                .of(Material.POTION)
                                .withDisplayName(potionType.getEffectType().getName())
                                .withMetaValue(PotionMeta.class, m -> {
                                    m.setBasePotionData(new PotionData(PotionType.WATER_BREATHING));
                                    m.setColor(potionType.getEffectType().getColor());
                                })
                                .withNBTData(c ->
                                        c.set(valueKey, PersistentDataType.INTEGER, settings == null ? 0 : settings.getDuration()))
                                .build(),
                        pos,
                        ActionConsumer.getIntRange(valueKey, 0, 600),
                        stack -> {
                            PersistentDataContainer container = stack.getItemMeta().getPersistentDataContainer();
                            int duration = container.get(valueKey, PersistentDataType.INTEGER);
                            if (duration == 0) return;
                            PotionMeta itemMeta = (PotionMeta) stack.getItemMeta();
                            PotionType type = itemMeta.getBasePotionData().getType();
                            respawnEffects.compute(type, (k, v) -> new PotionEffectSettings(type, duration));
                        }));
            }
            return true;
        }

        if ("commands".equalsIgnoreCase(field)) {
            List<String> deathCommands = playerDeathActions.getDeathCommands();
            Inventory inventory = Bukkit.createInventory(player, 56, "Manage Death Commands");
            InventoryActions actions = EldoUtilities.getInventoryActions().wrap(player, inventory, e -> {
                configuration.save();
                sendPlayerDeathActions(player, world, playerDeathActions);
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
                                                    localizer().getMessage("phrase.commandPlayer"),
                                                    s -> true, 0, i -> {
                                                        deathCommands.set(pos, i);
                                                        player.closeInventory();
                                                    });
                                            return;
                                        case RIGHT:
                                        case SHIFT_RIGHT:
                                            deathCommands.remove(pos);
                                            player.closeInventory();
                                    }
                                },
                                e -> {
                                }));

            }
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
            if ("loseInventory".equalsIgnoreCase(field)) {
                playerDeathActions.setLoseInvProbability(optionalInt.getAsInt());
            }
            configuration.save();
            sendPlayerDeathActions(player, world, playerDeathActions);
            return true;
        }

        if ("lightning".equalsIgnoreCase(field)) {
            LightningSettings lightningSettings = playerDeathActions.getLightningSettings();

            Inventory inventory = Bukkit.createInventory(player, 18,
                    localizer().getMessage("manageDeathActions.inventory.lightning.title"));

            InventoryActions actions = EldoUtilities.getInventoryActions().wrap(player, inventory, e -> {
                configuration.save();
                sendPlayerDeathActions(player, world, playerDeathActions);
            });

            NamespacedKey valueKey = new NamespacedKey(getPlugin(), "valueKey");
            actions.addAction(
                    new ActionItem(
                            ItemStackBuilder
                                    .of(Material.LEVER)
                                    .withDisplayName(
                                            localizer().getMessage("manageDeathActions.inventory.lightning.lightningActive")
                                    )
                                    .withLore(String.valueOf(lightningSettings.isDoLightning()))
                                    .withNBTData(
                                            c -> c.set(
                                                    valueKey,
                                                    PersistentDataType.BYTE,
                                                    DataContainerUtil.booleanToByte(lightningSettings.isDoLightning())))
                                    .build(),
                            3,
                            ActionConsumer.booleanToggle(valueKey),
                            stack -> {
                                boolean fieldValue = DataContainerUtil.byteToBoolean(DataContainerUtil.compute(stack, valueKey, PersistentDataType.BYTE, s -> s));
                                lightningSettings.setDoLightning(fieldValue);
                            })
            );

            actions.addAction(
                    new ActionItem(
                            ItemStackBuilder
                                    .of(Material.LEVER)
                                    .withDisplayName(
                                            localizer().getMessage("manageDeathActions.inventory.lightning.thunderActive")
                                    )
                                    .withLore(String.valueOf(lightningSettings.isDoThunder()))
                                    .withNBTData(
                                            c -> c.set(
                                                    valueKey,
                                                    PersistentDataType.BYTE,
                                                    DataContainerUtil.booleanToByte(lightningSettings.isDoThunder())))
                                    .build(),
                            5,
                            ActionConsumer.booleanToggle(valueKey),
                            stack -> {
                                boolean fieldValue = DataContainerUtil.byteToBoolean(DataContainerUtil.compute(stack, valueKey, PersistentDataType.BYTE, s -> s));
                                lightningSettings.setDoThunder(fieldValue);
                            })
            );

            actions.addAction(
                    new ActionItem(
                            ItemStackBuilder
                                    .of(Material.BLAZE_ROD)
                                    .withDisplayName(
                                            localizer().getMessage("manageDeathActions.inventory.lightning.lightningProb")
                                    )
                                    .withLore(String.valueOf(lightningSettings.getLightning()))
                                    .withNBTData(
                                            c -> c.set(
                                                    valueKey,
                                                    PersistentDataType.INTEGER,
                                                    lightningSettings.getLightning()))
                                    .build(),
                            12,
                            ActionConsumer.getIntRange(valueKey, 0, 100),
                            stack -> {
                                int fieldValue = DataContainerUtil.compute(stack, valueKey, PersistentDataType.INTEGER, s -> s);
                                lightningSettings.setLightning(fieldValue);
                            })
            );

            actions.addAction(
                    new ActionItem(
                            ItemStackBuilder
                                    .of(Material.BLAZE_POWDER)
                                    .withDisplayName(
                                            localizer().getMessage("manageDeathActions.inventory.lightning.thunderProb"))
                                    .withLore(String.valueOf(lightningSettings.getThunder()))
                                    .withNBTData(
                                            c -> c.set(
                                                    valueKey,
                                                    PersistentDataType.INTEGER,
                                                    lightningSettings.getThunder()))
                                    .build(),
                            14,
                            ActionConsumer.getIntRange(valueKey, 0, 100),
                            stack -> {
                                int fieldValue = DataContainerUtil.compute(stack, valueKey, PersistentDataType.INTEGER, s -> s);
                                lightningSettings.setThunder(fieldValue);
                            })
            );
            player.openInventory(inventory);
            return true;
        }

        if ("shockwave".equalsIgnoreCase(field)) {
            ShockwaveSettings shockwave = playerDeathActions.getShockwaveSettings();

            Inventory inventory = Bukkit.createInventory(player, 9,
                    localizer().getMessage("manageDeathActions.inventory.shockwave.title"));

            InventoryActions actions = EldoUtilities.getInventoryActions().wrap(player, inventory, e -> {
                configuration.save();
                sendPlayerDeathActions(player, world, playerDeathActions);
            });

            player.openInventory(inventory);

            // dont look at this O///O
            NamespacedKey valueKey = new NamespacedKey(getPlugin(), "valueKey");
            actions.addAction(
                    ItemStackBuilder
                            .of(Material.POTION)
                            .withMetaValue(PotionMeta.class, m -> m.setColor(Color.RED))
                            .withDisplayName(localizer().getMessage("manageDeathActions.inventory.shockwave.effects"))
                            .build(),
                    2,
                    event -> {
                        player.closeInventory();
                        EldoUtilities.getDelayedActions().schedule(() -> {
                            Inventory effectInventory = Bukkit.createInventory(player, 54,
                                    localizer().getMessage("manageDeathActions.inventory.shockwave.effects"));
                            InventoryActions effectActions = EldoUtilities.getInventoryActions().wrap(player, effectInventory,
                                    e -> {
                                        configuration.save();
                                        sendPlayerDeathActions(player, world, playerDeathActions);
                                    });
                            Map<PotionType, PotionEffectSettings> respawnEffects = shockwave.getShockwaveEffects();

                            player.openInventory(effectInventory);

                            // this is always such a mess qwq
                            int pos = 0;
                            for (PotionType potionType : PotionType.values()) {
                                if (potionType.getEffectType() == null) continue;
                                pos++;
                                @Nullable PotionEffectSettings settings = respawnEffects.get(potionType);
                                effectActions.addAction(new ActionItem(
                                        ItemStackBuilder
                                                .of(Material.POTION)
                                                .withDisplayName(potionType.getEffectType().getName())
                                                .withMetaValue(PotionMeta.class, m -> {
                                                    m.setBasePotionData(new PotionData(potionType));
                                                    m.setColor(potionType.getEffectType().getColor());
                                                })
                                                .withNBTData(c ->
                                                        c.set(valueKey, PersistentDataType.INTEGER, settings == null ? 0 : settings.getDuration()))
                                                .withLore(String.valueOf(settings == null ? 0 : settings.getDuration()))
                                                .build(),
                                        pos,
                                        ActionConsumer.getIntRange(valueKey, 0, 600),
                                        stack -> {
                                            PersistentDataContainer container = stack.getItemMeta().getPersistentDataContainer();
                                            int duration = container.get(valueKey, PersistentDataType.INTEGER);
                                            if (duration == 0) return;
                                            PotionMeta itemMeta = (PotionMeta) stack.getItemMeta();
                                            PotionType type = itemMeta.getBasePotionData().getType();
                                            shockwave.getShockwaveEffects().compute(type, (k, v) -> new PotionEffectSettings(type, duration));
                                        }));
                            }

                        }, 1);
                    },
                    event -> {
                    }
            );

            actions.addAction(
                    ItemStackBuilder
                            .of(Material.CLOCK)
                            .withDisplayName(
                                    localizer().getMessage("manageDeathActions.inventory.shockwave.minEffectDuration"))
                            .withLore(String.format("%.2f", shockwave.getMinDuration()))
                            .withNBTData(c -> c.set(valueKey, PersistentDataType.DOUBLE, shockwave.getMinDuration()))
                            .build(),
                    3,
                    ActionConsumer.getDoubleRange(valueKey, 0, 60),
                    item -> {
                        Double aDouble = item.getItemMeta().getPersistentDataContainer().get(valueKey,
                                PersistentDataType.DOUBLE);
                        shockwave.setMinDuration(aDouble);
                    }
            );

            actions.addAction(
                    ItemStackBuilder
                            .of(Material.BOW)
                            .withDisplayName(localizer().getMessage("field.range"))
                            .withLore(String.valueOf(shockwave.getShockwaveRange()))
                            .withNBTData(c -> c.set(valueKey, PersistentDataType.INTEGER, shockwave.getShockwaveRange()))
                            .build(),
                    4,
                    ActionConsumer.getIntRange(valueKey, 0, 60),
                    item -> {
                        shockwave.setShockwaveRange(DataContainerUtil.getOrDefault(item, valueKey, PersistentDataType.INTEGER, 0));
                    }
            );

            actions.addAction(
                    ItemStackBuilder
                            .of(Material.BLAZE_POWDER)
                            .withDisplayName(localizer().getMessage("field.power"))
                            .withLore(String.valueOf(shockwave.getShockwavePower()))
                            .withNBTData(c -> c.set(valueKey, PersistentDataType.INTEGER, shockwave.getShockwavePower()))
                            .build(),
                    5,
                    ActionConsumer.getIntRange(valueKey, 0, 60),
                    item -> {
                        shockwave.setShockwavePower(DataContainerUtil.getOrDefault(item, valueKey, PersistentDataType.INTEGER, 0));
                    }
            );

            actions.addAction(
                    ItemStackBuilder
                            .of(Material.LEVER)
                            .withDisplayName(localizer().getMessage("field.probability"))
                            .withLore(String.valueOf(shockwave.getShockwaveProbability()))
                            .withNBTData(c -> c.set(valueKey, PersistentDataType.INTEGER, shockwave.getShockwaveProbability()))
                            .build(),
                    6,
                    ActionConsumer.getIntRange(valueKey, 0, 100),
                    item -> {
                        shockwave.setShockwaveProbability(DataContainerUtil.getOrDefault(item, valueKey, PersistentDataType.INTEGER, 0));
                    }
            );
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
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return TabCompleteUtil.completeWorlds(args[0]);
        }

        if (args.length == 2) {
            return TabCompleteUtil.complete(args[1], "effects", "commands", "addCommand", "loseExp", "loseInv", "lightning", "shockwave");
        }

        if (args.length == 3) {
            if (TabCompleteUtil.isCommand(args[2], "addCommand")) {
                return TabCompleteUtil.completeFreeInput(args[2], 140, localizer().getMessage("syntax.commandPlayer"), localizer());
            }

            if (TabCompleteUtil.isCommand("loseExp", "loseInv")) {
                return TabCompleteUtil.completeInt(args[2], 0, 100, localizer());
            }
            return Collections.emptyList();
        }
        return Collections.emptyList();
    }
}