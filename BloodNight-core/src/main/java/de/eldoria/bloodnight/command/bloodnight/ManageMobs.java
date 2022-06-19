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
import de.eldoria.eldoutilities.localization.Replacement;
import de.eldoria.eldoutilities.simplecommands.EldoCommand;
import de.eldoria.eldoutilities.simplecommands.TabCompleteUtil;
import de.eldoria.eldoutilities.utils.ArgumentUtils;
import de.eldoria.eldoutilities.utils.ArrayUtil;
import de.eldoria.eldoutilities.utils.EnumUtil;
import de.eldoria.eldoutilities.utils.Parser;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
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

import java.util.*;
import java.util.stream.Collectors;

public class ManageMobs extends EldoCommand {
    private final BukkitAudiences bukkitAudiences = BukkitAudiences.create(BloodNight.getInstance());
    private final Configuration configuration;
    private final InventoryListener inventoryListener;

    public ManageMobs(Plugin plugin, Configuration configuration, InventoryListener inventoryListener) {
        super(plugin);
        this.configuration = configuration;
        this.inventoryListener = inventoryListener;
    }

    // world field value
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (denyConsole(sender)) {
            return true;
        }

        if (denyAccess(sender, Permissions.Admin.MANAGE_MOBS)) {
            return true;
        }

        Player player = getPlayerFromSender(sender);

        World world = ArgumentUtils.getOrDefault(args, 0, ArgumentUtils::getWorld, player.getWorld());

        if (world == null) {
            messageSender().sendError(sender, localizer().getMessage("error.invalidWorld"));
            return true;
        }

        WorldSettings worldSettings = configuration.getWorldSettings(world);
        MobSettings mobSettings = worldSettings.getMobSettings();
        if (args.length < 2) {
            sendInfo(sender, worldSettings);
            return true;
        }

        if (argumentsInvalid(sender, args, 3,
                "[" + localizer().getMessage("syntax.worldName") + "] [<"
                        + localizer().getMessage("syntax.field") + "> <"
                        + localizer().getMessage("syntax.value") + ">]")) {
            return true;
        }

        String field = args[1];
        String value = args[2];

        if (ArrayUtil.arrayContains(new String[]{"spawnPercentage", "dropAmount", "vanillaDropAmount"}, field)) {
            Optional<Integer> optionalInt = Parser.parseInt(value);
            if (!optionalInt.isPresent()) {
                messageSender().sendError(player, localizer().getMessage("error.invalidNumber"));
                return true;
            }
            if (invalidRange(sender, optionalInt.get(), 1, 100)) {
                return true;
            }
            if ("spawnPercentage".equalsIgnoreCase(field)) {
                mobSettings.setSpawnPercentage(optionalInt.get());
            }
            if ("dropAmount".equalsIgnoreCase(field)) {
                mobSettings.setDropAmount(optionalInt.get());
            }
            if ("vanillaDropAmount".equalsIgnoreCase(field)) {
                mobSettings.getVanillaMobSettings().setExtraDrops(optionalInt.get());
            }
            configuration.save();
            sendInfo(sender, worldSettings);
            return true;
        }

        if (ArrayUtil.arrayContains(new String[]{"monsterDamage", "vanillaMonsterDamage", "vanillaMonsterHealth",
                "monsterHealth", "experience", "vanillaDropsMulti"}, field)) {
            Optional<Double> optionalDouble = Parser.parseDouble(value);
            if (!optionalDouble.isPresent()) {
                messageSender().sendError(sender, localizer().getMessage("error.invalidNumber"));
                return true;
            }

            if (invalidRange(sender, optionalDouble.get(), 1, 200)) {
                return true;
            }

            if ("monsterDamage".equalsIgnoreCase(field)) {
                mobSettings.setDamageMultiplier(optionalDouble.get());
            }
            if ("monsterHealth".equalsIgnoreCase(field)) {
                mobSettings.setHealthModifier(optionalDouble.get());
            }
            if ("vanillaMonsterDamage".equalsIgnoreCase(field)) {
                mobSettings.getVanillaMobSettings().setDamageMultiplier(optionalDouble.get());
            }
            if ("vanillaMonsterHealth".equalsIgnoreCase(field)) {
                mobSettings.getVanillaMobSettings().setHealthMultiplier(optionalDouble.get());
            }
            if ("experience".equalsIgnoreCase(field)) {
                mobSettings.setExperienceMultiplier(optionalDouble.get());
            }
            if ("vanillaDropsMulti".equalsIgnoreCase(field)) {
                mobSettings.getVanillaMobSettings().setDropMultiplier(optionalDouble.get());
            }
            configuration.save();
            sendInfo(sender, worldSettings);
            return true;
        }
        if (ArrayUtil.arrayContains(new String[]{"forcePhantoms", "displayName", "naturalDrops"}, field)) {
            Optional<Boolean> optionalBoolean = Parser.parseBoolean(value);
            if (!optionalBoolean.isPresent()) {
                messageSender().sendError(sender, localizer().getMessage("error.invalidBoolean"));
                return true;
            }
            if ("forcePhantoms".equalsIgnoreCase(field)) {
                mobSettings.setForcePhantoms(optionalBoolean.get());
            }
            if ("displayName".equalsIgnoreCase(field)) {
                mobSettings.setDisplayMobNames(optionalBoolean.get());
            }
            if ("naturalDrops".equalsIgnoreCase(field)) {
                mobSettings.setNaturalDrops(optionalBoolean.get());
            }
            sendInfo(sender, worldSettings);
            configuration.save();
            return true;
        }

        if ("defaultDrops".equalsIgnoreCase(field)) {
            if ("changeContent".equalsIgnoreCase(value)) {
                Inventory inv = Bukkit.createInventory(player, 54, "Drops");
                List<ItemStack> stacks = mobSettings.getDefaultDrops().stream().map(Drop::getWeightedItem).collect(Collectors.toList());
                inv.setContents(stacks.toArray(new ItemStack[0]));
                player.openInventory(inv);
                inventoryListener.registerModification(player, new InventoryListener.InventoryActionHandler() {
                    @Override
                    public void onInventoryClose(InventoryCloseEvent event) {
                        List<Drop> collect = Arrays.stream(event.getInventory().getContents())
                                .filter(Objects::nonNull)
                                .map(Drop::fromItemStack)
                                .collect(Collectors.toList());
                        mobSettings.setDefaultDrops(collect);
                        sendInfo(sender, worldSettings);
                    }

                    @Override
                    public void onInventoryClick(InventoryClickEvent event) {
                    }
                });
                return true;
            }
            if ("changeWeight".equalsIgnoreCase(value)) {
                List<ItemStack> stacks = mobSettings.getDefaultDrops().stream().map(Drop::getItemWithLoreWeight).collect(Collectors.toList());
                Inventory inv = Bukkit.createInventory(player, 54, "Weight");
                inv.setContents(stacks.toArray(new ItemStack[0]));
                player.openInventory(inv);
                inventoryListener.registerModification(player, new InventoryListener.InventoryActionHandler() {
                    @Override
                    public void onInventoryClose(InventoryCloseEvent event) {
                        List<Drop> collect = Arrays.stream(event.getInventory().getContents())
                                .filter(Objects::nonNull)
                                .map(Drop::fromItemStack)
                                .collect(Collectors.toList());
                        mobSettings.setDefaultDrops(collect);
                        sendInfo(sender, worldSettings);
                    }

                    @Override
                    public void onInventoryClick(InventoryClickEvent event) {
                        if (event.getInventory().getType() != InventoryType.CHEST) return;

                        if (!event.getView().getTopInventory().equals(event.getClickedInventory())) {
                            return;
                        }

                        switch (event.getClick()) {
                            case LEFT:
                                Drop.changeWeight(event.getCurrentItem(), 1);
                                break;
                            case SHIFT_LEFT:
                                Drop.changeWeight(event.getCurrentItem(), 10);
                                break;
                            case RIGHT:
                                Drop.changeWeight(event.getCurrentItem(), -1);
                                break;
                            case SHIFT_RIGHT:
                                Drop.changeWeight(event.getCurrentItem(), -10);
                                break;
                        }
                        event.setCancelled(true);
                    }
                });
                return true;
            }
            if ("clear".equalsIgnoreCase(value)) {
                mobSettings.setDefaultDrops(new ArrayList<>());
                configuration.save();
                sendInfo(sender, worldSettings);
                return true;
            }
            messageSender().sendError(sender, localizer().getMessage("error.invalidValue"));
            return true;
        }

        if ("vanillaDropMode".equalsIgnoreCase(field)) {
            Optional<VanillaDropMode> parse = EnumUtil.parse(value, VanillaDropMode.class);
            if (parse.isEmpty()) {
                messageSender().sendError(sender, localizer().getMessage("error.invalidValue"));
                return true;
            }
            mobSettings.getVanillaMobSettings().setVanillaDropMode(parse.get());
            sendInfo(sender, worldSettings);
            configuration.save();
            return true;
        }
        messageSender().sendError(sender, localizer().getMessage("error.invalidField"));
        return true;
    }


    private void sendInfo(CommandSender sender, WorldSettings worldSettings) {
        MobSettings mSet = worldSettings.getMobSettings();
        VanillaMobSettings vms = worldSettings.getMobSettings().getVanillaMobSettings();
        String cmd = "/bloodnight manageMobs " + ArgumentUtils.escapeWorldName(worldSettings.getWorldName()) + " ";
        TextComponent.Builder message = Component.text()
                .append(CommandUtil.getHeader(localizer().getMessage("manageMobs.title",
                        Replacement.create("WORLD", worldSettings.getWorldName()).addFormatting('6'))))
                .append(Component.newline())
                // spawn percentage
                .append(Component.text(localizer().getMessage("field.spawnPercentage") + ": ", NamedTextColor.AQUA))
                .append(Component.text(mSet.getSpawnPercentage() + "% ", NamedTextColor.GOLD))
                .append(Component.text("[" + localizer().getMessage("action.change") + "]", NamedTextColor.GREEN)
                        .clickEvent(ClickEvent.suggestCommand(cmd + "spawnPercentage ")))
                // Display mobNamens
                .append(Component.newline())
                .append(CommandUtil.getBooleanField(mSet.isDisplayMobNames(),
                        cmd + "displayName {bool}",
                        localizer().getMessage("field.showMobNames"),
                        localizer().getMessage("state.enabled"),
                        localizer().getMessage("state.disabled")))
                .append(Component.newline())
                // Monster damage
                .append(Component.text(localizer().getMessage("field.monsterDamage") + ": ", NamedTextColor.AQUA))
                .append(Component.text(mSet.getDamageMultiplier() + "x ", NamedTextColor.GOLD))
                .append(Component.text("[" + localizer().getMessage("action.change") + "]", NamedTextColor.GREEN)
                        .clickEvent(ClickEvent.suggestCommand(cmd + "monsterDamage ")))
                .append(Component.newline())
                // Player damage
                .append(Component.text(localizer().getMessage("field.monsterHealth") + ": ", NamedTextColor.AQUA))
                .append(Component.text(mSet.getHealthModifier() + "x ", NamedTextColor.GOLD))
                .append(Component.text("[" + localizer().getMessage("action.change") + "]", NamedTextColor.GREEN)
                        .clickEvent(ClickEvent.suggestCommand(cmd + "monsterHealth ")))
                .append(Component.newline())
                // experience multiply
                .append(Component.text(localizer().getMessage("field.experienceMultiplier") + ": ", NamedTextColor.AQUA))
                .append(Component.text(mSet.getExperienceMultiplier() + "x ", NamedTextColor.GOLD))
                .append(Component.text("[" + localizer().getMessage("action.change") + "]", NamedTextColor.GREEN)
                        .clickEvent(ClickEvent.suggestCommand(cmd + "experience ")))
                .append(Component.newline())
                // force phantoms
                .append(CommandUtil.getBooleanField(mSet.isForcePhantoms(),
                        cmd + "forcePhantoms {bool}",
                        localizer().getMessage("field.forcePhantoms"),
                        localizer().getMessage("state.enabled"),
                        localizer().getMessage("state.disabled")))
                .append(Component.newline())
                // natural drops
                .append(CommandUtil.getBooleanField(mSet.isNaturalDrops(),
                        cmd + "naturalDrops {bool}",
                        localizer().getMessage("field.naturalDrops"),
                        localizer().getMessage("state.allow"),
                        localizer().getMessage("state.deny")))
                .append(Component.newline())
                // default drops
                .append(Component.text(localizer().getMessage("field.defaultDrops") + ": ", NamedTextColor.AQUA))
                .append(Component.text(mSet.getDefaultDrops().size() + " " + localizer().getMessage("field.drops"), NamedTextColor.GOLD))
                .append(Component.text(" [" + localizer().getMessage("action.content") + "]", NamedTextColor.GREEN)
                        .clickEvent(ClickEvent.runCommand(cmd + "defaultDrops changeContent")))
                .append(Component.text(" [" + localizer().getMessage("action.weight") + "]", NamedTextColor.GOLD)
                        .clickEvent(ClickEvent.runCommand(cmd + "defaultDrops changeWeight")))
                .append(Component.text(" [" + localizer().getMessage("action.clear") + "]", NamedTextColor.RED)
                        .clickEvent(ClickEvent.runCommand(cmd + "defaultDrops clear")))
                .append(Component.newline())
                // default drop amount
                .append(Component.text(localizer().getMessage("field.dropAmount") + ": ", NamedTextColor.AQUA))
                .append(Component.text(mSet.getDropAmount() + "x ", NamedTextColor.GOLD))
                .append(Component.text("[" + localizer().getMessage("action.change") + "]", NamedTextColor.GREEN)
                        .clickEvent(ClickEvent.suggestCommand(cmd + "dropAmount ")))
                .append(Component.newline())
                // Vanilla Mobs submenu
                .append(Component.text(localizer().getMessage("field.vanillaMobs") + ": ", NamedTextColor.AQUA))
                .append(Component.newline().append(Component.text("  ")))
                // Monster damage
                .append(Component.text(localizer().getMessage("field.monsterDamage") + ": ", NamedTextColor.AQUA))
                .append(Component.text(vms.getDamageMultiplier() + "x ", NamedTextColor.GOLD))
                .append(Component.text("[" + localizer().getMessage("action.change") + "]", NamedTextColor.GREEN)
                        .clickEvent(ClickEvent.suggestCommand(cmd + "vanillaMonsterDamage ")))
                .append(Component.newline().append(Component.text("  ")))
                // Player damage
                .append(Component.text(localizer().getMessage("field.monsterHealth") + ": ", NamedTextColor.AQUA))
                .append(Component.text(vms.getHealthMultiplier() + "x ", NamedTextColor.GOLD))
                .append(Component.text("[" + localizer().getMessage("action.change") + "]", NamedTextColor.GREEN)
                        .clickEvent(ClickEvent.suggestCommand(cmd + "vanillaMonsterHealth ")))
                .append(Component.newline().append(Component.text("  ")))
                // drops
                .append(Component.text(localizer().getMessage("field.dropsMultiplier") + ": ", NamedTextColor.AQUA))
                .append(Component.text(vms.getDropMultiplier() + "x ", NamedTextColor.GOLD))
                .append(Component.text("[" + localizer().getMessage("action.change") + "]", NamedTextColor.GREEN)
                        .clickEvent(ClickEvent.suggestCommand(cmd + "vanillaDropsMulti ")))
                .append(Component.newline().append(Component.text("  ")))
                // Drop Mode
                .append(Component.text(localizer().getMessage("field.dropMode") + ": ", NamedTextColor.AQUA))
                .append(CommandUtil.getToggleField(vms.getVanillaDropMode() == VanillaDropMode.VANILLA,
                        cmd + "vanillaDropMode VANILLA",
                        localizer().getMessage("state.vanilla")))
                .append(Component.space())
                .append(CommandUtil.getToggleField(vms.getVanillaDropMode() == VanillaDropMode.COMBINE,
                        cmd + "vanillaDropMode COMBINE",
                        localizer().getMessage("state.combine")))
                .append(Component.space())
                .append(CommandUtil.getToggleField(vms.getVanillaDropMode() == VanillaDropMode.CUSTOM,
                        cmd + "vanillaDropMode CUSTOM",
                        localizer().getMessage("state.custom")));
        if (vms.getVanillaDropMode() != VanillaDropMode.VANILLA) {
            message.append(Component.newline().append(Component.text("  ")))
                    .append(Component.text(localizer().getMessage("field.customDropAmount") + ": ", NamedTextColor.AQUA))
                    .append(Component.text(vms.getExtraDrops() + "x ", NamedTextColor.GOLD))
                    .append(Component.text("[" + localizer().getMessage("action.change") + "]", NamedTextColor.GREEN)
                            .clickEvent(ClickEvent.suggestCommand(cmd + "vanillaDropAmount ")));
        }
        bukkitAudiences.sender(sender).sendMessage(Identity.nil(), message);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return TabCompleteUtil.completeWorlds(args[0]);
        }
        if (args.length == 2) {
            return TabCompleteUtil.complete(args[1], "spawnPercentage", "dropAmount", "monsterDamage",
                    "vanillaMonsterDamage", "vanillaMonsterHealth", "experience", "drops",
                    "forcePhantoms", "displayName");
        }

        String field = args[1];
        String value = args[2];
        if (TabCompleteUtil.isCommand(field, "spawnPercentage", "dropAmount", "vanillaDropAmount", "vanillaDropAmount")) {
            return TabCompleteUtil.completeInt(value, 1, 100, localizer());
        }

        if (TabCompleteUtil.isCommand(field, "monsterDamage", "vanillaMonsterDamage", "vanillaMonsterHealth", "monsterHealth",
                "experience", "drops")) {
            return TabCompleteUtil.completeDouble(value, 1, 200, localizer());
        }
        if (TabCompleteUtil.isCommand(field, "forcePhantoms", "displayName", "naturalDrops")) {
            return TabCompleteUtil.completeBoolean(value);
        }
        if (TabCompleteUtil.isCommand(field, "defaultDrops")) {
            return TabCompleteUtil.complete(value, "changeContent", "changeWeight", "clear");
        }
        if (TabCompleteUtil.isCommand(field, "vanillaDropMode")) {
            return TabCompleteUtil.complete(value, VanillaDropMode.class);
        }

        return Collections.emptyList();
    }
}
