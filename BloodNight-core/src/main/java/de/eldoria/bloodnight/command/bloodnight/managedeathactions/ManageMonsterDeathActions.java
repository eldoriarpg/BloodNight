package de.eldoria.bloodnight.command.bloodnight.managedeathactions;

import de.eldoria.bloodnight.command.util.CommandUtil;
import de.eldoria.bloodnight.config.Configuration;
import de.eldoria.bloodnight.config.worldsettings.deathactions.MobDeathActions;
import de.eldoria.bloodnight.config.worldsettings.deathactions.PotionEffectSettings;
import de.eldoria.bloodnight.config.worldsettings.deathactions.subsettings.LightningSettings;
import de.eldoria.bloodnight.config.worldsettings.deathactions.subsettings.ShockwaveSettings;
import de.eldoria.eldoutilities.core.EldoUtilities;
import de.eldoria.eldoutilities.inventory.ActionConsumer;
import de.eldoria.eldoutilities.inventory.ActionItem;
import de.eldoria.eldoutilities.inventory.InventoryActions;
import de.eldoria.eldoutilities.items.ItemStackBuilder;
import de.eldoria.eldoutilities.simplecommands.EldoCommand;
import de.eldoria.eldoutilities.simplecommands.TabCompleteUtil;
import de.eldoria.eldoutilities.utils.ArgumentUtils;
import de.eldoria.eldoutilities.utils.DataContainerUtil;
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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ManageMonsterDeathActions extends EldoCommand {
    private final Configuration configuration;
    private final BukkitAudiences bukkitAudiences;

    public ManageMonsterDeathActions(Plugin plugin, Configuration configuration, BukkitAudiences bukkitAudiences) {
        super(plugin);
        this.configuration = configuration;
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

        MobDeathActions mobDeathActions = configuration.getWorldSettings(world).getDeathActionSettings().getMobDeathActions();

        if (args.length < 2) {
            sendMobDeathActions(player, world, mobDeathActions);
            return true;
        }

        if (argumentsInvalid(sender, args, 1,
                "<monster|player> <$syntax.worldName$> [<$syntax.field$> <$syntax.value$>]")) return true;

        String field = args[1];
        String value = ArgumentUtils.getOrDefault(args, 2, "none");


        if ("lightning".equalsIgnoreCase(field)) {
            LightningSettings lightningSettings = mobDeathActions.getLightningSettings();

            Inventory inventory = Bukkit.createInventory(player, 18,
                    localizer().getMessage("manageDeathActions.inventory.lightning.title"));

            InventoryActions actions = EldoUtilities.getInventoryActions().wrap(player, inventory, e -> {
                configuration.save();
                sendMobDeathActions(player, world, mobDeathActions);
            });

            NamespacedKey valueKey = new NamespacedKey(getPlugin(), "valueKey");
            actions.addAction(
                    new ActionItem(
                            ItemStackBuilder
                                    .of(Material.LEVER)
                                    .withDisplayName(localizer().getMessage("manageDeathActions.inventory.lightning.lightningActive"))
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
                                    .withDisplayName(localizer().getMessage("manageDeathActions.inventory.lightning.thunderActive"))
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
                                    .withDisplayName(localizer().getMessage("manageDeathActions.inventory.lightning.lightningProb"))
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
                                    .withDisplayName(localizer().getMessage("manageDeathActions.inventory.lightning.thunderProb"))
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
            ShockwaveSettings shockwave = mobDeathActions.getShockwaveSettings();

            Inventory inventory = Bukkit.createInventory(player, 9,
                    localizer().getMessage("manageDeathActions.inventory.shockwave.title"));

            InventoryActions actions = EldoUtilities.getInventoryActions().wrap(player, inventory, e -> {
                configuration.save();
                sendMobDeathActions(player, world, mobDeathActions);
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
                                        sendMobDeathActions(player, world, mobDeathActions);
                                    });
                            Map<PotionType, PotionEffectSettings> respawnEffects = shockwave.getShockwaveEffects();

                            player.openInventory(effectInventory);

                            // this is always such a mess qwq
                            int pos = 0;
                            for (PotionType potionType : PotionType.values()) {
                                if (potionType.getEffectType() == null) continue;

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
                                            Optional<Integer> integer = DataContainerUtil.get(stack, valueKey, PersistentDataType.INTEGER);
                                            if (!integer.isPresent() || integer.get() == 0) return;
                                            PotionMeta itemMeta = (PotionMeta) stack.getItemMeta();
                                            PotionType type = itemMeta.getBasePotionData().getType();
                                            shockwave.getShockwaveEffects().compute(type, (k, v) -> new PotionEffectSettings(type, integer.get()));
                                        }));
                                pos++;
                            }

                        }, 2);
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
        messageSender().sendError(sender, "error.invalidField");
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return TabCompleteUtil.completeWorlds(args[0]);
        }

        if (args.length == 2) {
            return TabCompleteUtil.complete(args[1], "lightning", "shockwave");
        }
        return Collections.emptyList();
    }

    private void sendMobDeathActions(Player player, World world, MobDeathActions mobDeathActions) {
        String cmd = "/bloodnight deathActions monster " + ArgumentUtils.escapeWorldName(world.getName()) + " ";
        TextComponent build = Component.text()
                .append(CommandUtil.getHeader(localizer().getMessage("manageDeathActions.monster.title")))
                .append(Component.newline())
                .append(Component.text(localizer().getMessage("field.lightningSettings"), NamedTextColor.AQUA))
                .append(Component.text(" [" + localizer().getMessage("action.change") + "]", NamedTextColor.GREEN)
                        .clickEvent(ClickEvent.runCommand(cmd + "lightning")))
                .append(Component.newline())
                .append(Component.text(localizer().getMessage("field.shockwaveSettings"), NamedTextColor.AQUA))
                .append(Component.text(" [" + localizer().getMessage("action.change") + "]", NamedTextColor.GREEN)
                        .clickEvent(ClickEvent.runCommand(cmd + "shockwave")))
                .build();

        bukkitAudiences.player(player).sendMessage(build);
    }
}
