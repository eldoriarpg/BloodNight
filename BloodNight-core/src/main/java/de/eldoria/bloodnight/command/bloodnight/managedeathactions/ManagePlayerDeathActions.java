package de.eldoria.bloodnight.command.bloodnight.managedeathactions;

import de.eldoria.bloodnight.command.util.CommandUtil;
import de.eldoria.bloodnight.config.Configuration;
import de.eldoria.bloodnight.config.worldsettings.deathactions.PlayerDeathActions;
import de.eldoria.bloodnight.config.worldsettings.deathactions.PotionEffectSettings;
import de.eldoria.bloodnight.core.BloodNight;
import de.eldoria.eldoutilities.builder.ItemStackBuilder;
import de.eldoria.eldoutilities.commands.Completion;
import de.eldoria.eldoutilities.commands.command.AdvancedCommand;
import de.eldoria.eldoutilities.commands.command.CommandMeta;
import de.eldoria.eldoutilities.commands.command.util.Arguments;
import de.eldoria.eldoutilities.commands.command.util.CommandAssertions;
import de.eldoria.eldoutilities.commands.command.util.Input;
import de.eldoria.eldoutilities.commands.exceptions.CommandException;
import de.eldoria.eldoutilities.commands.executor.IPlayerTabExecutor;
import de.eldoria.eldoutilities.conversation.ConversationRequester;
import de.eldoria.eldoutilities.inventory.ActionConsumer;
import de.eldoria.eldoutilities.inventory.ActionItem;
import de.eldoria.eldoutilities.inventory.InventoryActionHandler;
import de.eldoria.eldoutilities.inventory.InventoryActions;
import de.eldoria.eldoutilities.messages.Replacement;
import de.eldoria.eldoutilities.pdc.DataContainerUtil;
import de.eldoria.eldoutilities.scheduling.DelayedActions;
import de.eldoria.eldoutilities.utils.ArgumentUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static de.eldoria.eldoutilities.localization.ILocalizer.escape;

public class ManagePlayerDeathActions extends AdvancedCommand implements IPlayerTabExecutor {
    private final Configuration configuration;
    private final ConversationRequester conversationRequester;
    private final InventoryActionHandler inventoryActions;
    private final DelayedActions delayedActions;

    public ManagePlayerDeathActions(Plugin plugin, Configuration configuration) {
        super(plugin, CommandMeta.builder("player")
                .addArgument("syntax.worldName", true)
                .addArgument("syntax.field", false)
                .addArgument("syntax.value", false)
                .build());
        this.configuration = configuration;
        this.conversationRequester = ConversationRequester.start(plugin);
        this.inventoryActions = InventoryActionHandler.create(plugin);
        this.delayedActions = DelayedActions.start(plugin);
    }

    @Override
    public void onCommand(@NotNull Player player, @NotNull String alias, @NotNull Arguments args) throws CommandException {
        World world = args.asWorld(0, player.getWorld());

        PlayerDeathActions playerDeathActions = configuration.getWorldSettings(world).getDeathActionSettings().getPlayerDeathActions();

        if (args.size() < 2) {
            sendPlayerDeathActions(player, world, playerDeathActions);
            return;
        }

        String field = args.asString(1);
        Input value = args.get(2, Input.of(plugin(), "none"));

        if ("effects".equalsIgnoreCase(field)) {
            Inventory inventory = Bukkit.createInventory(player, 54,
                    localizer().getMessage("manageDeathActions.inventory.respawnEffects.title"));
            InventoryActions actions = inventoryActions.wrap(player, inventory,
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
                                .withMetaValue(PotionMeta.class, m -> m.setColor(potionType.getColor()))
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
                            if (integer.isEmpty()) return;
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
            return;
        }

        if ("commands".equalsIgnoreCase(field)) {
            List<String> deathCommands = playerDeathActions.getDeathCommands();
            Inventory inventory = Bukkit.createInventory(player, 54, "Manage Death Commands");
            InventoryActions actions = inventoryActions.wrap(player, inventory, e -> configuration.save());

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
                                        case LEFT, SHIFT_LEFT -> {
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
                                        }
                                        case RIGHT, SHIFT_RIGHT -> {
                                            deathCommands.remove(pos);
                                            player.closeInventory();
                                            sendPlayerDeathActions(player, world, playerDeathActions);
                                        }
                                    }
                                },
                                e -> {
                                }));

            }

            player.openInventory(inventory);
            return;
        }

        if ("addCommand".equalsIgnoreCase(field)) {
            CommandAssertions.isTrue("none".equalsIgnoreCase(value.asString()), "error.noCommand");
            playerDeathActions.getDeathCommands().add(args.join(2));
            configuration.save();
            sendPlayerDeathActions(player, world, playerDeathActions);
            return;
        }

        if (Completion.isCommand(field, "loseExp", "loseInv")) {
            int val = value.asInt();
            CommandAssertions.range(val, 0, 100);
            if ("loseExp".equalsIgnoreCase(field)) {
                playerDeathActions.setLoseExpProbability(val);
            }
            if ("loseInv".equalsIgnoreCase(field)) {
                playerDeathActions.setLoseInvProbability(val);
            }
            configuration.save();
            sendPlayerDeathActions(player, world, playerDeathActions);
            return;
        }

        if ("lightning".equalsIgnoreCase(field)) {
            DeathActionUtil.buildLightningUI(playerDeathActions.getLightningSettings(), player, inventoryActions,
                    configuration, localizer(), () -> sendPlayerDeathActions(player, world, playerDeathActions));
            return;
        }

        if ("shockwave".equalsIgnoreCase(field)) {
            DeathActionUtil.buildShockwaveUI(playerDeathActions.getShockwaveSettings(), player, inventoryActions, delayedActions,
                    configuration, localizer(), () -> sendPlayerDeathActions(player, world, playerDeathActions));
            return;
        }

        sendPlayerDeathActions(player, world, playerDeathActions);
        return;
    }

    private void sendPlayerDeathActions(Player player, World world, PlayerDeathActions playerDeathActions) {
        String cmd = "/bloodnight deathActions player " + ArgumentUtils.escapeWorldName(world.getName()) + " ";
        var actions = """
                %s
                <aqua>%s <click:run_command:'%s'><green>[%s]</click>
                <aqua>%s <click:run_command:'%s'><green>[%s]</click>
                <aqua>%s <click:run_command:'%s'><green>[%s]</click> <click:run_command:'%s'><dark_green>[%s]</click>
                <aqua>%s <click:run_command:'%s'><green>[%s]</click>
                <aqua>%s <click:run_command:'%s'><green>[%s]</click>
                <aqua>%s: <gold>%s <click:suggest_command:'%s'><green>[%s]</click>
                <aqua>%s: <gold>%s <click:suggest_command:'%s'><green>[%s]</click>
                """.stripIndent()
                .formatted(CommandUtil.getHeader("manageDeathActions.player.title"),
                        escape("field.lightningSettings"), cmd + "lightning", escape("action.change"),
                        escape("field.shockwaveSettings"), cmd + "shockwave", escape("action.change"),
                        escape("field.deathCommands"), cmd + "commands", escape("action.change"), cmd + "addCommand", escape("action.add"),
                        escape("field.respawnEffect"), cmd + "effects", escape("action.change"),
                        escape("field.loseInventory"), cmd + "loseInv", escape("action.change"),
                        escape("field.loseInventory"), playerDeathActions.getLoseInvProbability(), cmd + "loseInv ", escape("action.change"),
                        escape("field.loseExperience"), playerDeathActions.getLoseExpProbability(), cmd + "loseExp ", escape("action.change")
                );
        messageSender().sendMessage(player, actions, Replacement.create("COUNT", playerDeathActions.getDeathCommands().size()));
    }


    @Override
    public @Nullable List<String> onTabComplete(@NotNull Player player, @NotNull String alias, @NotNull Arguments args) throws CommandException {
        if (args.sizeIs(1)) {
            return Completion.completeWorlds(args.asString(0));
        }

        if (args.sizeIs(2)) {
            return Completion.complete(args.asString(1), "effects", "commands", "addCommand", "loseExp", "loseInv", "lightning", "shockwave");
        }

        if (args.size() >= 3) {
            if (Completion.isCommand(args.asString(1), "addCommand")) {
                return Completion.completeFreeInput(args.join(2), 140, localizer().getMessage("syntax.commandPlayer"));
            }

            if (Completion.isCommand(args.asString(1), "loseExp", "loseInv")) {
                return Completion.completeInt(args.asString(2), 0, 100);
            }
            return Collections.emptyList();
        }
        return Collections.emptyList();
    }

}
