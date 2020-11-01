package de.eldoria.bloodnight.command.bloodnight;

import de.eldoria.bloodnight.command.InventoryListener;
import de.eldoria.bloodnight.command.util.CommandUtil;
import de.eldoria.bloodnight.config.Configuration;
import de.eldoria.bloodnight.config.worldsettings.NightSelection;
import de.eldoria.bloodnight.config.worldsettings.WorldSettings;
import de.eldoria.bloodnight.core.BloodNight;
import de.eldoria.bloodnight.util.Permissions;
import de.eldoria.eldoutilities.container.Pair;
import de.eldoria.eldoutilities.localization.ILocalizer;
import de.eldoria.eldoutilities.simplecommands.EldoCommand;
import de.eldoria.eldoutilities.simplecommands.TabCompleteUtil;
import de.eldoria.eldoutilities.utils.ArgumentUtils;
import de.eldoria.eldoutilities.utils.EnumUtil;
import de.eldoria.eldoutilities.utils.Parser;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.stream.Collectors;

public class ManageNightSelection extends EldoCommand {
    private final Configuration configuration;
    private final InventoryListener inventoryListener;
    private final BukkitAudiences bukkitAudiences;

    public ManageNightSelection(Plugin plugin, Configuration configuration, InventoryListener inventoryListener) {
        super(plugin);
        this.configuration = configuration;
        this.inventoryListener = inventoryListener;
        bukkitAudiences = BukkitAudiences.create(BloodNight.getInstance());
    }

    // world field value
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (isConsole(sender)) {
            messageSender().sendError(sender, localizer().getMessage("error.console"));
            return true;
        }

        if (denyAccess(sender, Permissions.MANAGE_WORLDS)) {
            return true;
        }

        Player player = (Player) sender;

        World world = args.length > 0 ? Bukkit.getWorld(args[0]) : player.getWorld();

        if (world == null) {
            messageSender().sendError(sender, localizer().getMessage("error.invalidWorld"));
            return true;
        }

        WorldSettings worldSettings = configuration.getWorldSettings(world);

        if (args.length < 2) {
            sendWorldPage(world, sender, 0);
            return true;
        }

        // world field value
        if (argumentsInvalid(sender, args, 3,
                "[" + localizer().getMessage("syntax.worldName") + "] [<"
                        + localizer().getMessage("syntax.field") + "> <"
                        + localizer().getMessage("syntax.value") + ">]")) {
            return true;
        }
        String field = args[1];
        String value = ArgumentUtils.getOptionalParameter(args, 2, "none", s -> s);

        OptionalInt optPage = CommandUtil.findPage(configuration.getWorldSettings().values(), 3,
                w -> w.getWorldName().equalsIgnoreCase(world.getName()));

        if ("page".equalsIgnoreCase(field)) {
            OptionalInt optionalInt = Parser.parseInt(value);
            if (optionalInt.isPresent()) {
                sendWorldPage(world, sender, optionalInt.getAsInt());
            }
            return true;
        }

        if (TabCompleteUtil.isCommand(field, "interval", "intervalProbability", "probability")) {
            OptionalInt optionalInt = Parser.parseInt(value);
            if (!optionalInt.isPresent()) {
                messageSender().sendError(player, localizer().getMessage("error.invalidNumber"));
                return true;
            }
            if ("interval".equalsIgnoreCase(field)) {
                worldSettings.getNightSelection().setInterval(optionalInt.getAsInt());
            }
            if ("intervalProbability".equalsIgnoreCase(field)) {
                worldSettings.getNightSelection().setInterval(optionalInt.getAsInt());
            }
            if ("probability".equalsIgnoreCase(field)) {
                worldSettings.getNightSelection().setProbability(optionalInt.getAsInt());
            }
            optPage.ifPresent(p -> sendWorldPage(world, sender, p));
            configuration.saveConfig();
            return true;
        }

        if (TabCompleteUtil.isCommand(field, "moonPhase")) {
            Inventory inv = Bukkit.createInventory(player, 9, "Moon Phase Settings");
            List<ItemStack> stacks = PhaseItem.getPhaseItems(worldSettings.getNightSelection());
            inv.setContents(stacks.toArray(new ItemStack[0]));
            player.openInventory(inv);
            inventoryListener.registerModification(player, new InventoryListener.InventoryActionHandler() {
                @Override
                public void onInventoryClose(InventoryCloseEvent event) {
                    Arrays.stream(event.getInventory().getContents())
                            .filter(Objects::nonNull)
                            .map(PhaseItem::fromItemStack)
                            .forEach(s -> worldSettings.getNightSelection().setPhase(s.first, s.second));
                    optPage.ifPresent(i -> sendWorldPage(world, sender, i));
                }

                @Override
                public void onInventoryClick(InventoryClickEvent event) {
                    if (event.getInventory().getType() != InventoryType.CHEST) return;

                    if (!event.getView().getTopInventory().equals(event.getClickedInventory())) {
                        return;
                    }

                    switch (event.getClick()) {
                        case LEFT:
                            PhaseItem.changeProbability(event.getCurrentItem(), 1);
                            break;
                        case SHIFT_LEFT:
                            PhaseItem.changeProbability(event.getCurrentItem(), 10);
                            break;
                        case RIGHT:
                            PhaseItem.changeProbability(event.getCurrentItem(), -1);
                            break;
                        case SHIFT_RIGHT:
                            PhaseItem.changeProbability(event.getCurrentItem(), -10);
                            break;
                    }
                    event.setCancelled(true);
                }
            });
            return true;
        }

        if (TabCompleteUtil.isCommand(field, "type")) {
            NightSelection.NightSelectionType parse = EnumUtil.parse(value, NightSelection.NightSelectionType.class);
            if (parse == null) {
                messageSender().sendLocalizedError(sender, "error.invalidValue");
                return true;
            }
            worldSettings.getNightSelection().setNightSelectionType(parse);
            configuration.saveConfig();
            optPage.ifPresent(p -> sendWorldPage(world, sender, p));
            return true;
        }
        return true;
    }

    private void sendWorldPage(World world, CommandSender sender, int p) {
        TextComponent page = CommandUtil.getPage(configuration.getWorldSettings().values(), p, 3, 4, s -> {
            NightSelection ns = s.getNightSelection();
            String cmd = "/bloodnight nightSelection " + s.getWorldName() + " ";
            TextComponent.Builder builder = Component.text()
                    .append(Component.text(s.getWorldName(), NamedTextColor.GOLD, TextDecoration.BOLD))
                    .append(Component.newline())
                    .append(Component.text(localizer().getMessage("field.nightSelectionType") + ":", NamedTextColor.AQUA))
                    .append(Component.newline())
                    .append(CommandUtil.getToggleField(ns.getNightSelectionType() == NightSelection.NightSelectionType.RANDOM,
                            cmd + "type random",
                            localizer().getMessage("state.random")))
                    .append(Component.space())
                    .append(CommandUtil.getToggleField(ns.getNightSelectionType() == NightSelection.NightSelectionType.MOON_PHASE,
                            cmd + "type moon_phase",
                            localizer().getMessage("state.moonPhase")))
                    .append(Component.space())
                    .append(CommandUtil.getToggleField(ns.getNightSelectionType() == NightSelection.NightSelectionType.INTERVAL,
                            cmd + "type interval",
                            localizer().getMessage("state.interval")))
                    .append(Component.newline());

            switch (ns.getNightSelectionType()) {
                case RANDOM:
                    builder.append(Component.text(localizer().getMessage("field.probability") + ": ", NamedTextColor.AQUA))
                            .append(Component.text(ns.getProbability(), NamedTextColor.GOLD))
                            .append(Component.text(" [" + localizer().getMessage("action.change") + "]", NamedTextColor.GREEN)
                                    .clickEvent(ClickEvent.suggestCommand(cmd + "probability ")));
                    break;
                case MOON_PHASE:
                    builder.append(Component.text(localizer().getMessage("field.moonPhase") + ": ", NamedTextColor.AQUA))
                            .append(Component.text(" [" + localizer().getMessage("action.change") + "]", NamedTextColor.GREEN)
                                    .clickEvent(ClickEvent.runCommand(cmd + "moonPhase none")))
                            .append(Component.newline());
                    ns.getPhases().forEach((key, value) -> builder.append(Component.text("| " + key + ":" + value + " |",NamedTextColor.GOLD)));
                    break;
                case INTERVAL:
                    builder.append(Component.text(localizer().getMessage("field.interval") + ": ", NamedTextColor.AQUA))
                            .append(Component.text(ns.getInterval(), NamedTextColor.GOLD))
                            .append(Component.text(" [" + localizer().getMessage("action.change") + "]", NamedTextColor.GREEN)
                                    .clickEvent(ClickEvent.suggestCommand(cmd + "interval ")))
                            .append(Component.newline())
                            .append(Component.text(localizer().getMessage("field.intervalProbability") + ": ", NamedTextColor.AQUA))
                            .append(Component.text(ns.getIntervalProbability(), NamedTextColor.GOLD))
                            .append(Component.text(" [" + localizer().getMessage("action.change") + "]", NamedTextColor.GREEN)
                                    .clickEvent(ClickEvent.suggestCommand(cmd + "intervalProbability ")));

                    break;
            }
            return builder.build();
        }, "Night Selection", "/bloodNight nightSelection " + world.getName() + " page {page}");
        bukkitAudiences.sender(sender).sendMessage(page);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return TabCompleteUtil.completeWorlds(args[0]);
        }
        if (args.length == 2) {
            return TabCompleteUtil.complete(args[1], "interval", "intervalProbability", "probability", "moonPhase", "type");
        }

        String field = args[1];
        String value = args[2];
        if (TabCompleteUtil.isCommand(field, "interval")) {
            return TabCompleteUtil.completeInt(value, 1, 100, localizer());
        }
        if (TabCompleteUtil.isCommand(field, "intervalProbability", "probability")) {
            return TabCompleteUtil.completeInt(value, 0, 100, localizer());
        }

        if (TabCompleteUtil.isCommand(field, "moonPhase")) {
            return Collections.emptyList();
        }

        if (TabCompleteUtil.isCommand(field, "type")) {
            return TabCompleteUtil.complete(value, NightSelection.NightSelectionType.class);
        }
        return Collections.emptyList();
    }

    private static class PhaseItem {
        public static Pair<Integer, Integer> fromItemStack(ItemStack itemStack) {
            if (itemStack == null) return null;
            return Pair.of(getPhase(itemStack), getProbability(itemStack));
        }

        private static int getPhase(ItemStack itemStack) {
            return itemStack.getItemMeta().getPersistentDataContainer().get(BloodNight.getNamespacedKey("phase"), PersistentDataType.INTEGER);
        }

        private static int getProbability(ItemStack itemStack) {
            return itemStack.getItemMeta().getPersistentDataContainer().get(BloodNight.getNamespacedKey("phase"), PersistentDataType.INTEGER);
        }

        public static void changeProbability(ItemStack item, int change) {
            int currWeight = getProbability(item);
            int newWeight = Math.min(Math.max(currWeight + change, 0), 100);
            setWeight(item, newWeight);
            ItemMeta itemMeta = item.getItemMeta();
            assert itemMeta != null;
            itemMeta.setLore(getLore(newWeight));
            item.setItemMeta(itemMeta);
        }

        private static void setWeight(ItemStack item, int weight) {
            ItemMeta itemMeta = item.hasItemMeta() ? item.getItemMeta() : Bukkit.getItemFactory().getItemMeta(item.getType());
            PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();
            dataContainer.set(BloodNight.getNamespacedKey("dropWeight"), PersistentDataType.INTEGER, weight);
            item.setItemMeta(itemMeta);
        }

        private static void setProbabilityIfAbstent(ItemStack item, int weight) {
            ItemMeta itemMeta = item.hasItemMeta() ? item.getItemMeta() : Bukkit.getItemFactory().getItemMeta(item.getType());
            PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();
            if (!dataContainer.has(BloodNight.getNamespacedKey("probability"), PersistentDataType.INTEGER)) {
                setWeight(item, weight);
            }
        }

        public static List<ItemStack> getPhaseItems(NightSelection selection) {
            return selection.getPhases().entrySet().stream().map(PhaseItem::toPhaseItem).collect(Collectors.toList());
        }

        private static ItemStack toPhaseItem(Map.Entry<Integer, Integer> entry) {
            ILocalizer localizer = ILocalizer.getPluginLocalizer(BloodNight.class);
            ItemStack stack = new ItemStack(Material.FIREWORK_STAR, entry.getKey());
            ItemMeta itemMeta = stack.getItemMeta();
            itemMeta.setDisplayName("§2" + localizer.getMessage("phaseItem.phase") + ": " + entry.getKey());
            itemMeta.setLore(getLore(entry.getValue()));
            PersistentDataContainer container = itemMeta.getPersistentDataContainer();
            container.set(BloodNight.getNamespacedKey("probability"), PersistentDataType.INTEGER, entry.getValue());
            container.set(BloodNight.getNamespacedKey("phase"), PersistentDataType.INTEGER, entry.getKey());
            stack.setItemMeta(itemMeta);
            return stack;
        }

        private static List<String> getLore(int value) {
            return Collections.singletonList("§6" + ILocalizer.getPluginLocalizer(BloodNight.class).getMessage("field.probability") + ": " + value);
        }
    }
}
