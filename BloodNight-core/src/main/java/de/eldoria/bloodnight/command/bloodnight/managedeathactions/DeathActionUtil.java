package de.eldoria.bloodnight.command.bloodnight.managedeathactions;

import de.eldoria.bloodnight.config.Configuration;
import de.eldoria.bloodnight.config.worldsettings.deathactions.PotionEffectSettings;
import de.eldoria.bloodnight.config.worldsettings.deathactions.subsettings.LightningSettings;
import de.eldoria.bloodnight.config.worldsettings.deathactions.subsettings.ShockwaveSettings;
import de.eldoria.bloodnight.core.BloodNight;
import de.eldoria.eldoutilities.builder.ItemStackBuilder;
import de.eldoria.eldoutilities.core.EldoUtilities;
import de.eldoria.eldoutilities.inventory.ActionConsumer;
import de.eldoria.eldoutilities.inventory.ActionItem;
import de.eldoria.eldoutilities.inventory.InventoryActions;
import de.eldoria.eldoutilities.localization.ILocalizer;
import de.eldoria.eldoutilities.utils.DataContainerUtil;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;

public final class DeathActionUtil {
    private DeathActionUtil() {
    }

    public static void buildShockwaveUI(ShockwaveSettings shockwave, Player player, Configuration configuration,
                                        ILocalizer localizer, Runnable callback) {

        Inventory inventory = Bukkit.createInventory(player, 9,
                localizer.getMessage("manageDeathActions.inventory.shockwave.title"));

        InventoryActions actions = EldoUtilities.getInventoryActions().wrap(player, inventory, e -> {
            configuration.save();
            callback.run();
        });

        player.openInventory(inventory);

        // dont look at this O///O
        NamespacedKey valueKey = new NamespacedKey(BloodNight.getInstance(), "valueKey");
        NamespacedKey typeKey = new NamespacedKey(BloodNight.getInstance(), "typeKey");
        actions.addAction(
                ItemStackBuilder
                        .of(Material.POTION)
                        .withMetaValue(PotionMeta.class, m -> m.setColor(Color.RED))
                        .withDisplayName(localizer.getMessage("manageDeathActions.inventory.shockwave.effects"))
                        .build(),
                2,
                event -> {
                    player.closeInventory();
                    EldoUtilities.getDelayedActions().schedule(() -> {
                        Inventory effectInventory = Bukkit.createInventory(player, 54,
                                localizer.getMessage("manageDeathActions.inventory.shockwave.effects"));
                        InventoryActions effectActions = EldoUtilities.getInventoryActions().wrap(player, effectInventory,
                                e -> {
                                    configuration.save();
                                    callback.run();
                                });
                        Map<PotionEffectType, PotionEffectSettings> respawnEffects = shockwave.getShockwaveEffects();

                        player.openInventory(effectInventory);

                        // this is always such a mess qwq
                        int pos = 0;
                        @NotNull PotionEffectType[] values = PotionEffectType.values();
                        Arrays.sort(values, Comparator.comparing(PotionEffectType::getName));
                        for (@NotNull PotionEffectType potionType : values) {
                            @Nullable PotionEffectSettings settings = respawnEffects.get(potionType);
                            effectActions.addAction(new ActionItem(
                                    ItemStackBuilder
                                            .of(Material.POTION)
                                            .withDisplayName(potionType.getName())
                                            .withMetaValue(PotionMeta.class, m -> m.setColor(potionType.getColor()))
                                            .withNBTData(c -> {
                                                c.set(typeKey, PersistentDataType.STRING, potionType.getName());
                                                c.set(valueKey, PersistentDataType.INTEGER, settings == null ? 0 : settings.getDuration());
                                            })
                                            .withLore(String.valueOf(settings == null ? 0 : settings.getDuration()))
                                            .build(),
                                    pos,
                                    ActionConsumer.getIntRange(valueKey, 0, 600),
                                    stack -> {
                                        Optional<Integer> integer = DataContainerUtil.get(stack, valueKey, PersistentDataType.INTEGER);
                                        if (!integer.isPresent()) return;
                                        PotionMeta itemMeta = (PotionMeta) stack.getItemMeta();
                                        Optional<String> optionalType = DataContainerUtil.get(stack, typeKey, PersistentDataType.STRING);
                                        optionalType.ifPresent(name -> {
                                                    PotionEffectType type = PotionEffectType.getByName(name);
                                                    if (integer.get() == 0) {
                                                        shockwave.getShockwaveEffects().remove(type);
                                                        return;
                                                    }
                                                    shockwave.getShockwaveEffects().compute(type, (k, v) -> new PotionEffectSettings(type, integer.get()));
                                                }
                                        );
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
                                localizer.getMessage("manageDeathActions.inventory.shockwave.minEffectDuration"))
                        .withLore(String.valueOf((int) shockwave.getMinDuration() * 100))
                        .withNBTData(c -> c.set(valueKey, PersistentDataType.INTEGER, (int) shockwave.getMinDuration() * 100))
                        .build(),
                3,
                ActionConsumer.getIntRange(valueKey, 0, 100),
                item -> {
                    @Nullable Integer integer = item.getItemMeta().getPersistentDataContainer().get(valueKey,
                            PersistentDataType.INTEGER);
                    shockwave.setMinDuration(integer / 100d);
                }
        );

        actions.addAction(
                ItemStackBuilder
                        .of(Material.BOW)
                        .withDisplayName(localizer.getMessage("field.range"))
                        .withLore(String.valueOf(shockwave.getShockwaveRange()))
                        .withNBTData(c -> c.set(valueKey, PersistentDataType.INTEGER, shockwave.getShockwaveRange()))
                        .build(),
                4,
                ActionConsumer.getIntRange(valueKey, 0, 60),
                item -> shockwave.setShockwaveRange(DataContainerUtil.getOrDefault(item, valueKey, PersistentDataType.INTEGER, 0))
        );

        actions.addAction(
                ItemStackBuilder
                        .of(Material.BLAZE_POWDER)
                        .withDisplayName(localizer.getMessage("field.power"))
                        .withLore(String.valueOf(shockwave.getShockwavePower()))
                        .withNBTData(c -> c.set(valueKey, PersistentDataType.INTEGER, shockwave.getShockwavePower()))
                        .build(),
                5,
                ActionConsumer.getIntRange(valueKey, 0, 60),
                item -> shockwave.setShockwavePower(DataContainerUtil.getOrDefault(item, valueKey, PersistentDataType.INTEGER, 0))
        );

        actions.addAction(
                ItemStackBuilder
                        .of(Material.LEVER)
                        .withDisplayName(localizer.getMessage("field.probability"))
                        .withLore(String.valueOf(shockwave.getShockwaveProbability()))
                        .withNBTData(c -> c.set(valueKey, PersistentDataType.INTEGER, shockwave.getShockwaveProbability()))
                        .build(),
                6,
                ActionConsumer.getIntRange(valueKey, 0, 100),
                item -> shockwave.setShockwaveProbability(DataContainerUtil.getOrDefault(item, valueKey, PersistentDataType.INTEGER, 0))
        );
    }

    public static void buildLightningUI(LightningSettings lightningSettings, Player player, Configuration configuration,
                                        ILocalizer localizer, Runnable callback) {
        Inventory inventory = Bukkit.createInventory(player, 18,
                localizer.getMessage("manageDeathActions.inventory.lightning.title"));

        InventoryActions actions = EldoUtilities.getInventoryActions().wrap(player, inventory, e -> {
            configuration.save();
            callback.run();
        });

        NamespacedKey valueKey = new NamespacedKey(BloodNight.getInstance(), "valueKey");
        actions.addAction(
                new ActionItem(
                        ItemStackBuilder
                                .of(Material.LEVER)
                                .withDisplayName(
                                        localizer.getMessage("manageDeathActions.inventory.lightning.lightningActive")
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
                                        localizer.getMessage("manageDeathActions.inventory.lightning.thunderActive")
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
                                        localizer.getMessage("manageDeathActions.inventory.lightning.lightningProb")
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
                                        localizer.getMessage("manageDeathActions.inventory.lightning.thunderProb"))
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
    }
}
