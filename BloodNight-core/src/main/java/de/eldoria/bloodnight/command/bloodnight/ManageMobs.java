package de.eldoria.bloodnight.command.bloodnight;

import de.eldoria.bloodnight.command.InventoryListener;
import de.eldoria.bloodnight.command.util.CommandUtil;
import de.eldoria.bloodnight.config.Configuration;
import de.eldoria.bloodnight.config.worldsettings.WorldSettings;
import de.eldoria.bloodnight.config.worldsettings.mobsettings.Drop;
import de.eldoria.bloodnight.config.worldsettings.mobsettings.MobSettings;
import de.eldoria.bloodnight.config.worldsettings.mobsettings.VanillaDropMode;
import de.eldoria.bloodnight.config.worldsettings.mobsettings.VanillaMobSettings;
import de.eldoria.bloodnight.core.BloodNight;
import de.eldoria.bloodnight.util.Permissions;
import de.eldoria.eldoutilities.commands.Completion;
import de.eldoria.eldoutilities.commands.command.AdvancedCommand;
import de.eldoria.eldoutilities.commands.command.CommandMeta;
import de.eldoria.eldoutilities.commands.command.util.Arguments;
import de.eldoria.eldoutilities.commands.command.util.CommandAssertions;
import de.eldoria.eldoutilities.commands.command.util.Input;
import de.eldoria.eldoutilities.commands.exceptions.CommandException;
import de.eldoria.eldoutilities.commands.executor.IPlayerTabExecutor;
import de.eldoria.eldoutilities.messages.Replacement;
import de.eldoria.eldoutilities.utils.ArgumentUtils;
import de.eldoria.eldoutilities.utils.ArrayUtil;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static de.eldoria.bloodnight.command.util.CommandUtil.changeButton;
import static de.eldoria.bloodnight.command.util.CommandUtil.changeableValue;
import static de.eldoria.bloodnight.command.util.CommandUtil.getBooleanField;
import static de.eldoria.bloodnight.command.util.CommandUtil.getHeader;
import static de.eldoria.bloodnight.command.util.CommandUtil.getToggleField;
import static de.eldoria.bloodnight.command.util.CommandUtil.value;
import static de.eldoria.eldoutilities.localization.ILocalizer.escape;

public class ManageMobs extends AdvancedCommand implements IPlayerTabExecutor {
    private final BukkitAudiences bukkitAudiences = BukkitAudiences.create(BloodNight.getInstance());
    private final Configuration configuration;
    private final InventoryListener inventoryListener;

    public ManageMobs(Plugin plugin, Configuration configuration, InventoryListener inventoryListener) {
        super(plugin, CommandMeta.builder("manageMobs")
                .withPermission(Permissions.Admin.MANAGE_MOBS)
                .addArgument("syntax.worldName", false)
                .addArgument("syntax.field", false)
                .addArgument("syntax.value", false)
                .build());
        this.configuration = configuration;
        this.inventoryListener = inventoryListener;
    }

    // world field value

    @Override
    public void onCommand(@NotNull Player player, @NotNull String alias, @NotNull Arguments args) throws CommandException {
        World world = args.asWorld(0, player.getWorld());

        WorldSettings worldSettings = configuration.getWorldSettings(world);
        MobSettings mobSettings = worldSettings.getMobSettings();
        if (args.size() < 2) {
            sendInfo(player, worldSettings);
            return;
        }

        String field = args.asString(1);
        Input value = args.get(2);

        if (Completion.isCommand(field, "spawnPercentage", "dropAmount", "vanillaDropAmount")) {
            CommandAssertions.range(value.asInt(), 1, 100);
            if ("spawnPercentage".equalsIgnoreCase(field)) {
                mobSettings.setSpawnPercentage(value.asInt());
            }
            if ("dropAmount".equalsIgnoreCase(field)) {
                mobSettings.setDropAmount(value.asInt());
            }
            if ("vanillaDropAmount".equalsIgnoreCase(field)) {
                mobSettings.getVanillaMobSettings().setExtraDrops(value.asInt());
            }
            configuration.save();
            sendInfo(player, worldSettings);
            return;
        }

        if (Completion.isCommand(field, "monsterDamage", "vanillaMonsterDamage", "vanillaMonsterHealth",
                "monsterHealth", "experience", "vanillaDropsMulti")) {
            CommandAssertions.range(value.asDouble(), 1, 200);

            if ("monsterDamage".equalsIgnoreCase(field)) {
                mobSettings.setDamageMultiplier(value.asDouble());
            }
            if ("monsterHealth".equalsIgnoreCase(field)) {
                mobSettings.setHealthModifier(value.asDouble());
            }
            if ("vanillaMonsterDamage".equalsIgnoreCase(field)) {
                mobSettings.getVanillaMobSettings().setDamageMultiplier(value.asDouble());
            }
            if ("vanillaMonsterHealth".equalsIgnoreCase(field)) {
                mobSettings.getVanillaMobSettings().setHealthMultiplier(value.asDouble());
            }
            if ("experience".equalsIgnoreCase(field)) {
                mobSettings.setExperienceMultiplier(value.asDouble());
            }
            if ("vanillaDropsMulti".equalsIgnoreCase(field)) {
                mobSettings.getVanillaMobSettings().setDropMultiplier(value.asDouble());
            }
            configuration.save();
            sendInfo(player, worldSettings);
            return;
        }
        if (ArrayUtil.arrayContains(new String[]{"forcePhantoms", "displayName", "naturalDrops"}, field)) {

            if ("forcePhantoms".equalsIgnoreCase(field)) {
                mobSettings.setForcePhantoms(value.asBoolean());
            }
            if ("displayName".equalsIgnoreCase(field)) {
                mobSettings.setDisplayMobNames(value.asBoolean());
            }
            if ("naturalDrops".equalsIgnoreCase(field)) {
                mobSettings.setNaturalDrops(value.asBoolean());
            }
            sendInfo(player, worldSettings);
            configuration.save();
            return;
        }

        if ("defaultDrops".equalsIgnoreCase(field)) {
            if ("changeContent".equalsIgnoreCase(value.asString())) {
                Inventory inv = Bukkit.createInventory(player, 54, "Drops");
                inv.setContents(mobSettings.getDefaultDrops().stream().map(Drop::getWeightedItem)
                        .toArray(ItemStack[]::new));
                player.openInventory(inv);
                inventoryListener.registerModification(player, new InventoryListener.InventoryActionHandler() {
                    @Override
                    public void onInventoryClose(InventoryCloseEvent event) {
                        List<Drop> collect = Arrays.stream(event.getInventory().getContents())
                                .filter(Objects::nonNull)
                                .map(Drop::fromItemStack)
                                .collect(Collectors.toList());
                        mobSettings.setDefaultDrops(collect);
                        sendInfo(player, worldSettings);
                    }

                    @Override
                    public void onInventoryClick(InventoryClickEvent event) {
                    }
                });
                return;
            }
            if ("changeWeight".equalsIgnoreCase(value.asString())) {
                Inventory inv = Bukkit.createInventory(player, 54, "Weight");
                inv.setContents(mobSettings.getDefaultDrops().stream().map(Drop::getItemWithLoreWeight)
                        .toArray(ItemStack[]::new));
                player.openInventory(inv);
                inventoryListener.registerModification(player, new InventoryListener.InventoryActionHandler() {
                    @Override
                    public void onInventoryClose(InventoryCloseEvent event) {
                        List<Drop> collect = Arrays.stream(event.getInventory().getContents())
                                .filter(Objects::nonNull)
                                .map(Drop::fromItemStack)
                                .collect(Collectors.toList());
                        mobSettings.setDefaultDrops(collect);
                        sendInfo(player, worldSettings);
                        configuration.save();
                    }

                    @Override
                    public void onInventoryClick(InventoryClickEvent event) {
                        if (event.getInventory().getType() != InventoryType.CHEST) return;

                        if (!event.getView().getTopInventory().equals(event.getClickedInventory())) {
                            return;
                        }

                        switch (event.getClick()) {
                            case LEFT -> Drop.changeWeight(event.getCurrentItem(), 1);
                            case SHIFT_LEFT -> Drop.changeWeight(event.getCurrentItem(), 10);
                            case RIGHT -> Drop.changeWeight(event.getCurrentItem(), -1);
                            case SHIFT_RIGHT -> Drop.changeWeight(event.getCurrentItem(), -10);
                        }
                        event.setCancelled(true);
                    }
                });
                return;
            }
            if ("clear".equalsIgnoreCase(value.asString())) {
                mobSettings.setDefaultDrops(new ArrayList<>());
                configuration.save();
                sendInfo(player, worldSettings);
                return;
            }
            messageSender().sendError(player, localizer().getMessage("error.invalidValue"));
            return;
        }

        if ("vanillaDropMode".equalsIgnoreCase(field)) {
            mobSettings.getVanillaMobSettings().setVanillaDropMode(value.asEnum(VanillaDropMode.class));
            sendInfo(player, worldSettings);
            configuration.save();
            return;
        }
        messageSender().sendError(player, localizer().getMessage("error.invalidField"));
        return;
    }


    private void sendInfo(CommandSender sender, WorldSettings worldSettings) {
        MobSettings mSet = worldSettings.getMobSettings();
        VanillaMobSettings vms = worldSettings.getMobSettings().getVanillaMobSettings();
        String cmd = "/bloodnight manageMobs " + ArgumentUtils.escapeWorldName(worldSettings.getWorldName()) + " ";
        var notVanilla = changeableValue("field.customDropAmount", vms.getExtraDrops() + "x", cmd + "vanillaDropAmount ");

        var a = """
                %s
                  %s
                  %s
                  %s
                  %s
                  %s
                  %s
                  %s %s %s %s
                  %s
                %s:
                  %s
                  %s
                  %s
                  %s: %s %s %s
                  %s
                """.stripIndent()
                .formatted(
                        getHeader("manageMobs.title"),
                        // spawn percentage
                        changeableValue("field.spawnPercentage", mSet.getSpawnPercentage() + "%", cmd + "spawnPercentage "),
                        // Display mobNamens
                        getBooleanField(mSet.isDisplayMobNames(), cmd + "displayName {bool}", "field.showMobNames", "state.enabled", "state.disabled"),
                        // Monster damage
                        changeableValue("field.monsterDamage", mSet.getDamageMultiplier() + "x", cmd + "monsterDamage "),
                        // Player damage
                        changeableValue("field.monsterHealth", mSet.getHealthModifier() + "x", cmd + "monsterHealth "),
                        // experience multiply
                        changeableValue("field.experienceMultiplier", mSet.getExperienceMultiplier() + "x", cmd + "experience "),
                        // force phantoms
                        getBooleanField(mSet.isForcePhantoms(), cmd + "forcePhantoms {bool}", "field.forcePhantoms", "state.enabled", "state.disabled"),
                        // natural drops
                        getBooleanField(mSet.isNaturalDrops(), cmd + "naturalDrops {bool}", "field.naturalDrops", "state.allow", "state.deny"),
                        // default drops
                        value("field.defaultDrops", mSet.getDefaultDrops().size() + " " + escape("field.drops")),
                        changeButton(cmd + "defaultDrops changeContent", "action.content", "change"),
                        changeButton(cmd + "defaultDrops changeWeight", "action.weight", "weight"),
                        changeButton(cmd + "defaultDrops clear", "action.clear", "delete"),
                        // default drop amount
                        changeableValue("field.dropAmount", mSet.getDropAmount() + "x", cmd + "dropAmount "),
                        // Vanilla Mobs submenu
                        escape("field.vanillaMobs"),
                        // Monster damage
                        changeableValue("field.monsterDamage", vms.getDamageMultiplier() + "x", cmd + "vanillaMonsterDamage "),
                        // Player damage
                        changeableValue("field.monsterHealth", vms.getHealthMultiplier() + "x", cmd + "vanillaMonsterHealth "),
                        // drops
                        changeableValue("field.dropsMultiplier", vms.getDropMultiplier() + "x", cmd + "vanillaDropsMulti "),
                        // Drop Mode
                        escape("field.dropMode"),
                        getToggleField(vms.getVanillaDropMode() == VanillaDropMode.VANILLA, cmd + "vanillaDropMode VANILLA", "state.vanilla"),
                        getToggleField(vms.getVanillaDropMode() == VanillaDropMode.COMBINE, cmd + "vanillaDropMode COMBINE", "state.combine"),
                        getToggleField(vms.getVanillaDropMode() == VanillaDropMode.CUSTOM, cmd + "vanillaDropMode CUSTOM", "state.custom"),
                        // not vanilla
                        vms.getVanillaDropMode() != VanillaDropMode.VANILLA ? notVanilla : ""
                );
        messageSender().sendMessage(sender, a, Replacement.create("WORLD", worldSettings.getWorldName()));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull Player player, @NotNull String alias, @NotNull Arguments args) throws CommandException {
        if (args.size() == 1) {
            return Completion.completeWorlds(args.asString(0));
        }
        if (args.size() == 2) {
            return Completion.complete(args.asString(1), "spawnPercentage", "dropAmount", "monsterDamage",
                    "vanillaMonsterDamage", "vanillaMonsterHealth", "experience", "drops",
                    "forcePhantoms", "displayName");
        }

        String field = args.asString(1);
        String value = args.asString(2);
        if (Completion.isCommand(field, "spawnPercentage", "dropAmount", "vanillaDropAmount", "vanillaDropAmount")) {
            return Completion.completeInt(value, 1, 100);
        }

        if (Completion.isCommand(field, "monsterDamage", "vanillaMonsterDamage", "vanillaMonsterHealth", "monsterHealth",
                "experience", "drops")) {
            return Completion.completeDouble(value, 1, 200);
        }
        if (Completion.isCommand(field, "forcePhantoms", "displayName", "naturalDrops")) {
            return Completion.completeBoolean(value);
        }
        if (Completion.isCommand(field, "defaultDrops")) {
            return Completion.complete(value, "changeContent", "changeWeight", "clear");
        }
        if (Completion.isCommand(field, "vanillaDropMode")) {
            return Completion.complete(value, VanillaDropMode.class);
        }

        return Collections.emptyList();
    }
}
