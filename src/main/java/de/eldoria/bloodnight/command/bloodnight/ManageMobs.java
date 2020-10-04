package de.eldoria.bloodnight.command.bloodnight;

import de.eldoria.bloodnight.command.InventoryListener;
import de.eldoria.bloodnight.command.util.CommandUtil;
import de.eldoria.bloodnight.command.util.KyoriColors;
import de.eldoria.bloodnight.config.Configuration;
import de.eldoria.bloodnight.config.worldsettings.WorldSettings;
import de.eldoria.bloodnight.config.worldsettings.mobsettings.Drop;
import de.eldoria.bloodnight.config.worldsettings.mobsettings.MobSettings;
import de.eldoria.bloodnight.config.worldsettings.mobsettings.VanillaDropMode;
import de.eldoria.bloodnight.config.worldsettings.mobsettings.VanillaMobSettings;
import de.eldoria.bloodnight.core.BloodNight;
import de.eldoria.bloodnight.util.Permissions;
import de.eldoria.eldoutilities.localization.Localizer;
import de.eldoria.eldoutilities.localization.Replacement;
import de.eldoria.eldoutilities.messages.MessageSender;
import de.eldoria.eldoutilities.simplecommands.EldoCommand;
import de.eldoria.eldoutilities.simplecommands.TabCompleteUtil;
import de.eldoria.eldoutilities.utils.ArrayUtil;
import de.eldoria.eldoutilities.utils.EnumUtil;
import de.eldoria.eldoutilities.utils.Parser;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.stream.Collectors;

public class ManageMobs extends EldoCommand {
    private final BukkitAudiences bukkitAudiences = BukkitAudiences.create(BloodNight.getInstance());
    private final Configuration configuration;
    private final InventoryListener inventoryListener;

    public ManageMobs(Localizer localizer, MessageSender messageSender, Configuration configuration, InventoryListener inventoryListener) {
        super(localizer, messageSender);
        this.configuration = configuration;
        this.inventoryListener = inventoryListener;
    }

    // world field value
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (isConsole(sender)) return true;

        if (denyAccess(sender, Permissions.MANAGE_MOBS)) {
            messageSender().sendError(sender, localizer().getMessage("error.console"));
            return true;
        }

        Player player = getPlayerFromSender(sender);

        World world = args.length > 0 ? Bukkit.getWorld(args[0]) : player.getWorld();

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

        if (ArrayUtil.arrayContains(new String[] {"spawnPercentage", "dropAmount", "vanillaDropAmount"}, field)) {
            OptionalInt optionalInt = Parser.parseInt(value);
            if (!optionalInt.isPresent()) {
                messageSender().sendError(player, localizer().getMessage("error.invalidNumber"));
                return true;
            }
            if (invalidRange(sender, optionalInt.getAsInt(), 1, 100)) {
                return true;
            }
            if ("spawnPercentage".equalsIgnoreCase(field)) {
                mobSettings.setSpawnPercentage(optionalInt.getAsInt());
            }
            if ("dropAmount".equalsIgnoreCase(field)) {
                mobSettings.setDropAmount(optionalInt.getAsInt());
            }
            if ("vanillaDropAmount".equalsIgnoreCase(field)) {
                mobSettings.getVanillaMobSettings().setDropAmount(optionalInt.getAsInt());
            }
            configuration.safeConfig();
            sendInfo(sender, worldSettings);
            return true;
        }

        if (ArrayUtil.arrayContains(new String[] {"monsterDamage", "vanillaMonsterDamage", "monsterHealth",
                "vanillaPlayerHealth", "experience", "drops"}, field)) {
            OptionalDouble optionalDouble = Parser.parseDouble(value);
            if (!optionalDouble.isPresent()) {
                messageSender().sendError(sender, localizer().getMessage("error.invalidNumber"));
                return true;
            }

            if (invalidRange(sender, optionalDouble.getAsDouble(), 1, 200)) {
                return true;
            }

            if ("monsterDamage".equalsIgnoreCase(field)) {
                mobSettings.setMonsterDamageMultiplier(optionalDouble.getAsDouble());
            }
            if ("vanillaMonsterDamage".equalsIgnoreCase(field)) {
                mobSettings.getVanillaMobSettings().setDamageMultiplier(optionalDouble.getAsDouble());
            }
            if ("monsterHealth".equalsIgnoreCase(field)) {
                mobSettings.setMonsterHealthModifier(optionalDouble.getAsDouble());
            }
            if ("vanillaPlayerHealth".equalsIgnoreCase(field)) {
                mobSettings.getVanillaMobSettings().setHealthMultiplier(optionalDouble.getAsDouble());
            }
            if ("experience".equalsIgnoreCase(field)) {
                mobSettings.setExperienceMultiplier(optionalDouble.getAsDouble());
            }
            if ("vanillaDropsMulti".equalsIgnoreCase(field)) {
                mobSettings.getVanillaMobSettings().setDropMultiplier(optionalDouble.getAsDouble());
            }
            configuration.safeConfig();
            sendInfo(sender, worldSettings);
            return true;
        }
        if (ArrayUtil.arrayContains(new String[] {"forcePhantoms", "displayName", "naturalDrops"}, field)) {
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
            configuration.safeConfig();
            return true;
        }

        if ("defaultDrops".equalsIgnoreCase(field)) {
            if ("changeContent".equalsIgnoreCase(value)) {
                Inventory inv = Bukkit.createInventory(player, 54, "Drops");
                List<ItemStack> stacks = mobSettings.getDefaultDrops().stream().map(Drop::getItem).collect(Collectors.toList());
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
                configuration.safeConfig();
                sendInfo(sender, worldSettings);
                return true;
            }
            messageSender().sendError(sender, localizer().getMessage("error.invalidValue"));
            return true;
        }

        if ("vanillaDropMode".equalsIgnoreCase(field)) {
            VanillaDropMode parse = EnumUtil.parse(value, VanillaDropMode.class);
            if (parse == null) {
                messageSender().sendError(sender, localizer().getMessage("error.invalidValue"));
                return true;
            }
            mobSettings.getVanillaMobSettings().setVanillaDropMode(parse);
            sendInfo(sender, worldSettings);
            configuration.safeConfig();
            return true;
        }
        messageSender().sendError(sender, localizer().getMessage("error.invalidField"));
        return true;
    }


    private void sendInfo(CommandSender sender, WorldSettings worldSettings) {
        MobSettings mSet = worldSettings.getMobSettings();
        VanillaMobSettings vms = worldSettings.getMobSettings().getVanillaMobSettings();
        String cmd = "/bloodnight manageMobs " + worldSettings.getWorldName() + " ";
        TextComponent.Builder message = TextComponent.builder()
                .append(CommandUtil.getHeader(localizer().getMessage("manageMobs.title",
                        Replacement.create("WORLD", worldSettings.getWorldName()).addFormatting('6'))))
                .append(TextComponent.newline())
                // spawn percentage
                .append(TextComponent.builder(localizer().getMessage("field.spawnPercentage") + ": ", KyoriColors.AQUA))
                .append(TextComponent.builder(mSet.getSpawnPercentage() + "% ", KyoriColors.GOLD))
                .append(TextComponent.builder("[" + localizer().getMessage("action.change") + "]", KyoriColors.GREEN)
                        .clickEvent(ClickEvent.suggestCommand(cmd + "spawnPercentage ")))
                // Display mobNamens
                .append(TextComponent.newline())
                .append(CommandUtil.getBooleanField(mSet.isDisplayMobNames(),
                        cmd + "displayName {bool}",
                        localizer().getMessage("field.showMobNames"),
                        localizer().getMessage("state.enabled"),
                        localizer().getMessage("state.disabled")))
                .append(TextComponent.newline())
                // Monster damage
                .append(TextComponent.builder(localizer().getMessage("field.monsterDamage") + ": ", KyoriColors.AQUA))
                .append(TextComponent.builder(mSet.getMonsterDamageMultiplier() + "x ", KyoriColors.GOLD))
                .append(TextComponent.builder("[" + localizer().getMessage("action.change") + "]", KyoriColors.GREEN)
                        .clickEvent(ClickEvent.suggestCommand(cmd + "monsterDamage ")))
                .append(TextComponent.newline())
                // Player damage
                .append(TextComponent.builder(localizer().getMessage("field.monsterHealth") + ": ", KyoriColors.AQUA))
                .append(TextComponent.builder(mSet.getMonsterHealthModifier() + "x ", KyoriColors.GOLD))
                .append(TextComponent.builder("[" + localizer().getMessage("action.change") + "]", KyoriColors.GREEN)
                        .clickEvent(ClickEvent.suggestCommand(cmd + "monsterHealth ")))
                .append(TextComponent.newline())
                // experience multiply
                .append(TextComponent.builder(localizer().getMessage("field.experienceMultiplier") + ": ", KyoriColors.AQUA))
                .append(TextComponent.builder(mSet.getExperienceMultiplier() + "x ", KyoriColors.GOLD))
                .append(TextComponent.builder("[" + localizer().getMessage("action.change") + "]", KyoriColors.GREEN)
                        .clickEvent(ClickEvent.suggestCommand(cmd + "experience ")))
                .append(TextComponent.newline())
                // force phantoms
                .append(CommandUtil.getBooleanField(mSet.isForcePhantoms(),
                        cmd + "forcePhantoms {bool}",
                        localizer().getMessage("field.forcePhantoms"),
                        localizer().getMessage("state.enabled"),
                        localizer().getMessage("state.disabled")))
                .append(TextComponent.newline())
                // natural drops
                .append(CommandUtil.getBooleanField(mSet.isNaturalDrops(),
                        cmd + "naturalDrops {bool}",
                        localizer().getMessage("field.naturalDrops"),
                        localizer().getMessage("state.allow"),
                        localizer().getMessage("state.deny")))
                .append(TextComponent.newline())
                // default drops
                .append(TextComponent.builder(localizer().getMessage("field.defaultDrops") + ": ", KyoriColors.AQUA))
                .append(TextComponent.builder(mSet.getDefaultDrops().size() + " " + localizer().getMessage("field.drops"), KyoriColors.GOLD))
                .append(TextComponent.builder(" [" + localizer().getMessage("action.content") + "]", KyoriColors.GREEN)
                        .clickEvent(ClickEvent.runCommand(cmd + "defaultDrops changeContent")))
                .append(TextComponent.builder(" [" + localizer().getMessage("action.weight") + "]", KyoriColors.GOLD)
                        .clickEvent(ClickEvent.runCommand(cmd + "defaultDrops changeWeight")))
                .append(TextComponent.builder(" [" + localizer().getMessage("action.clear") + "]", KyoriColors.RED)
                        .clickEvent(ClickEvent.runCommand(cmd + "defaultDrops clear")))
                .append(TextComponent.newline())
                // default drop amount
                .append(TextComponent.builder(localizer().getMessage("field.dropAmount") + ": ", KyoriColors.AQUA))
                .append(TextComponent.builder(mSet.getDropAmount() + "x ", KyoriColors.GOLD))
                .append(TextComponent.builder("[" + localizer().getMessage("action.change") + "]", KyoriColors.GREEN)
                        .clickEvent(ClickEvent.suggestCommand(cmd + "dropAmount ")))
                .append(TextComponent.newline())
                // Vanilla Mobs submenu
                .append(TextComponent.builder(localizer().getMessage("field.vanillaMobs") + ": ", KyoriColors.AQUA))
                .append(TextComponent.newline().append(TextComponent.builder("  ")))
                // Monster damage
                .append(TextComponent.builder(localizer().getMessage("field.monsterDamage") + ": ", KyoriColors.AQUA))
                .append(TextComponent.builder(vms.getDamageMultiplier() + "x ", KyoriColors.GOLD))
                .append(TextComponent.builder("[" + localizer().getMessage("action.change") + "]", KyoriColors.GREEN)
                        .clickEvent(ClickEvent.suggestCommand(cmd + "vanillaMonsterDamage ")))
                .append(TextComponent.newline().append(TextComponent.builder("  ")))
                // Player damage
                .append(TextComponent.builder(localizer().getMessage("field.monsterHealth") + ": ", KyoriColors.AQUA))
                .append(TextComponent.builder(vms.getHealthMultiplier() + "x ", KyoriColors.GOLD))
                .append(TextComponent.builder("[" + localizer().getMessage("action.change") + "]", KyoriColors.GREEN)
                        .clickEvent(ClickEvent.suggestCommand(cmd + "vanillaPlayerDamage ")))
                .append(TextComponent.newline().append(TextComponent.builder("  ")))
                // drops
                .append(TextComponent.builder(localizer().getMessage("field.dropsMultiplier") + ": ", KyoriColors.AQUA))
                .append(TextComponent.builder(vms.getDropMultiplier() + "x ", KyoriColors.GOLD))
                .append(TextComponent.builder("[" + localizer().getMessage("action.change") + "]", KyoriColors.GREEN)
                        .clickEvent(ClickEvent.suggestCommand(cmd + "vanillaDropsMulti ")))
                .append(TextComponent.newline().append(TextComponent.builder("  ")))
                // Drop Mode
                .append(TextComponent.builder(localizer().getMessage("field.dropMode") + ": ", KyoriColors.AQUA))
                .append(CommandUtil.getToggleField(vms.getVanillaDropMode() == VanillaDropMode.VANILLA,
                        cmd + "vanillaDropMode VANILLA",
                        localizer().getMessage("state.vanilla")))
                .append(" ")
                .append(CommandUtil.getToggleField(vms.getVanillaDropMode() == VanillaDropMode.COMBINE,
                        cmd + "vanillaDropMode COMBINE",
                        localizer().getMessage("state.combine")))
                .append(" ")
                .append(CommandUtil.getToggleField(vms.getVanillaDropMode() == VanillaDropMode.CUSTOM,
                        cmd + "vanillaDropMode CUSTOM",
                        localizer().getMessage("state.custom")));
        if (vms.getVanillaDropMode() != VanillaDropMode.VANILLA) {
            message.append(TextComponent.newline().append(TextComponent.builder("  ")))
                    .append(TextComponent.builder(localizer().getMessage("field.customDropAmount") + ": ", KyoriColors.AQUA))
                    .append(TextComponent.builder(vms.getDropAmount() + "x ", KyoriColors.GOLD))
                    .append(TextComponent.builder("[" + localizer().getMessage("action.change") + "]", KyoriColors.GREEN)
                            .clickEvent(ClickEvent.suggestCommand(cmd + "vanillaDropAmount ")));


        }
        bukkitAudiences.audience(sender).sendMessage(message);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return TabCompleteUtil.completeWorlds(args[0]);
        }
        if (args.length == 2) {
            return TabCompleteUtil.complete(args[1], "spawnPercentage", "dropAmount", "monsterDamage",
                    "vanillaMonsterDamage", "monsterHealth", "vanillaPlayerHealth", "experience", "drops",
                    "forcePhantoms", "displayName");
        }

        String field = args[1];
        String value = args[2];
        if (TabCompleteUtil.isCommand(field, "spawnPercentage", "dropAmount", "vanillaDropAmount")) {
            return TabCompleteUtil.completeInt(value, 1, 100, localizer());
        }

        if (TabCompleteUtil.isCommand(field, "monsterDamage", "vanillaMonsterDamage", "monsterHealth",
                "vanillaPlayerHealth", "experience", "drops")) {
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
