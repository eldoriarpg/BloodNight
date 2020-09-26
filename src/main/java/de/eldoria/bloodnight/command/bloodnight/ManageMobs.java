package de.eldoria.bloodnight.command.bloodnight;

import de.eldoria.bloodnight.command.InventoryListener;
import de.eldoria.bloodnight.command.util.CommandUtil;
import de.eldoria.bloodnight.command.util.KyoriColors;
import de.eldoria.bloodnight.config.Configuration;
import de.eldoria.bloodnight.config.Drop;
import de.eldoria.bloodnight.config.MobSettings;
import de.eldoria.bloodnight.config.WorldSettings;
import de.eldoria.bloodnight.core.BloodNight;
import de.eldoria.eldoutilities.localization.Localizer;
import de.eldoria.eldoutilities.messages.MessageSender;
import de.eldoria.eldoutilities.simplecommands.EldoCommand;
import de.eldoria.eldoutilities.utils.ArrayUtil;
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
import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.stream.Collectors;

public class ManageMobs extends EldoCommand {
    BukkitAudiences bukkitAudiences = BukkitAudiences.create(BloodNight.getInstance());
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
        if (!(sender instanceof Player)) return true;

        Player player = (Player) sender;

        World world = player.getWorld();

        if (args.length > 0) {
            world = Bukkit.getWorld(args[0]);
            if (world == null) {
                messageSender().sendError(sender, "invalid world");
                return true;
            }
        }

        WorldSettings worldSettings = configuration.getWorldSettings(world);
        MobSettings mobSettings = worldSettings.getMobSettings();
        if (args.length < 2) {
            sendInfo(sender, worldSettings);
            return true;
        }

        if (argumentsInvalid(sender, args, 3, "world field value")) {
            return true;
        }

        String cmd = args[1];
        String value = args[2];

        if (ArrayUtil.arrayContains(new String[] {"spawnPercentage", "dropAmount"})) {
            OptionalInt optionalInt = Parser.parseInt(value);
            if (!optionalInt.isPresent()) {
                messageSender().sendError(player, "invalid number");
                return true;
            }
            if ("spawnPercentage".equalsIgnoreCase(cmd)) {
                mobSettings.setSpawnPercentage(optionalInt.getAsInt());
            }
            if ("dropAmount".equalsIgnoreCase(cmd)) {
                mobSettings.setDropAmount(optionalInt.getAsInt());
            }
            sendInfo(sender, worldSettings);
            return true;
        }
        if ("defaultDrops".equalsIgnoreCase(cmd)) {
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
                sendInfo(sender, worldSettings);
                return true;
            }
            messageSender().sendError(sender, "invalid value");
            return true;
        }
        messageSender().sendError(sender, "Invalid field");
        return true;
    }


    private void sendInfo(CommandSender sender, WorldSettings worldSettings) {
        MobSettings mSet = worldSettings.getMobSettings();
        String cmd = "/bloodnight manageMobs " + worldSettings.getWorldName() + " ";
        TextComponent message = TextComponent.builder()
                .append(CommandUtil.getHeader("Manage Mobs of " + worldSettings.getWorldName()))
                .append(TextComponent.newline())
                .append(TextComponent.builder("Spawn Percentage: ", KyoriColors.AQUA))
                .append(TextComponent.builder(mSet.getSpawnPercentage() + "x ", KyoriColors.GOLD))
                .append(TextComponent.builder("[change]", KyoriColors.GREEN)
                        .clickEvent(ClickEvent.suggestCommand(cmd + "spawnPercentage ")))
                .append(TextComponent.newline())
                .append(CommandUtil.getBooleanField(mSet.isNaturalDrops(),
                        cmd + "naturalDrops {bool}",
                        "Natural Drops", "allow", "deny"))
                .append(TextComponent.newline())
                .append(TextComponent.builder("Default Drops: ", KyoriColors.AQUA))
                .append(TextComponent.builder(mSet.getDefaultDrops().size() + " Drops ", KyoriColors.GOLD))
                .append(TextComponent.builder("[content] ", KyoriColors.GREEN)
                        .clickEvent(ClickEvent.runCommand(cmd + "defaultDrops changeContent")))
                .append(TextComponent.builder("[weight] ", KyoriColors.GOLD)
                        .clickEvent(ClickEvent.runCommand(cmd + "defaultDrops changeWeight")))
                .append(TextComponent.builder("[clear]", KyoriColors.RED)
                        .clickEvent(ClickEvent.runCommand(cmd + "defaultDrops clear")))
                .append(TextComponent.newline())
                .append(TextComponent.builder("Drop Amount: ", KyoriColors.AQUA))
                .append(TextComponent.builder(mSet.getDropAmount() + "x ", KyoriColors.GOLD))
                .append(TextComponent.builder("[change]", KyoriColors.GREEN)
                        .clickEvent(ClickEvent.suggestCommand(cmd + "dropAmount ")))
                .append(TextComponent.newline()).build();
        bukkitAudiences.audience(sender).sendMessage(message);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return super.onTabComplete(sender, command, alias, args);
    }
}
