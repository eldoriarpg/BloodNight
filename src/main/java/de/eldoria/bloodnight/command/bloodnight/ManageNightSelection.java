package de.eldoria.bloodnight.command.bloodnight;

import de.eldoria.bloodnight.command.InventoryListener;
import de.eldoria.bloodnight.command.util.CommandUtil;
import de.eldoria.bloodnight.config.Configuration;
import de.eldoria.bloodnight.config.worldsettings.NightSelection;
import de.eldoria.bloodnight.config.worldsettings.WorldSettings;
import de.eldoria.bloodnight.core.BloodNight;
import de.eldoria.bloodnight.util.C;
import de.eldoria.bloodnight.util.Permissions;
import de.eldoria.eldoutilities.container.Pair;
import de.eldoria.eldoutilities.localization.ILocalizer;
import de.eldoria.eldoutilities.simplecommands.EldoCommand;
import de.eldoria.eldoutilities.simplecommands.TabCompleteUtil;
import de.eldoria.eldoutilities.utils.ArgumentUtils;
import de.eldoria.eldoutilities.utils.EMath;
import de.eldoria.eldoutilities.utils.EnumUtil;
import de.eldoria.eldoutilities.utils.Parser;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
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

import java.util.ArrayList;
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

    private static String getMoonPhaseName(int phase) {
        return "state.phase" + phase;
    }

    private static String getMoonPhaseSign(int phase) {
        switch (phase) {
            case 0:
                return "§f████";
            case 1:
                return "§f███§8█";
            case 2:
                return "§f██§8██";
            case 3:
                return "§f█§8███";
            case 4:
                return "§8████";
            case 5:
                return "§8███§f█";
            case 6:
                return "§8██§f██";
            case 7:
                return "§8█§f███";
            default:
                throw new IllegalStateException("Unexpected value: " + phase);
        }
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

        World world = args.length > 0 ? Bukkit.getWorld(C.unescapeWorldName(args[0])) : player.getWorld();

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

        final NightSelection sel = worldSettings.getNightSelection();
        if (TabCompleteUtil.isCommand(field, "interval", "intervalProbability", "probability", "phaseAmount",
                "period", "minCurveVal", "maxCurveVal")) {
            OptionalInt optionalInt = Parser.parseInt(value);
            if (!optionalInt.isPresent()) {
                messageSender().sendError(player, localizer().getMessage("error.invalidNumber"));
                return true;
            }

            if ("interval".equalsIgnoreCase(field)) {
                sel.setInterval(EMath.clamp(1, 100, optionalInt.getAsInt()));
            }
            if ("intervalProbability".equalsIgnoreCase(field)) {
                sel.setInterval(EMath.clamp(0, 100, optionalInt.getAsInt()));
            }
            if ("probability".equalsIgnoreCase(field)) {
                sel.setProbability(EMath.clamp(1, 100, optionalInt.getAsInt()));
            }
            if ("phaseAmount".equalsIgnoreCase(field)) {
                sel.setPhaseCount(EMath.clamp(1, 54, optionalInt.getAsInt()));
            }
            if ("period".equalsIgnoreCase(field)) {
                sel.setPeriod(EMath.clamp(3, 100, optionalInt.getAsInt()));
            }
            if ("minCurveVal".equalsIgnoreCase(field)) {
                sel.setMinCurveVal(EMath.clamp(0, 100, optionalInt.getAsInt()));
            }
            if ("maxCurveVal".equalsIgnoreCase(field)) {
                sel.setMaxCurveVal(EMath.clamp(0, 100, optionalInt.getAsInt()));
            }
            optPage.ifPresent(p -> sendWorldPage(world, sender, p));
            configuration.save();
            return true;
        }

        if (TabCompleteUtil.isCommand(field, "moonPhase", "phase")) {
            boolean moonPhase = "moonPhase".equalsIgnoreCase(field);
            Inventory inv = Bukkit.createInventory(player, moonPhase ? 9 : 54,
                    localizer().getMessage(moonPhase ? "nightSelection.title.moonPhase" : "nightSelection.title.phase"));
            List<ItemStack> stacks = PhaseItem.getPhaseItems(moonPhase ? sel.getMoonPhase() : sel.getPhaseCustom(), moonPhase);
            inv.setContents(stacks.toArray(new ItemStack[0]));
            player.openInventory(inv);
            inventoryListener.registerModification(player, new InventoryListener.InventoryActionHandler() {
                @Override
                public void onInventoryClose(InventoryCloseEvent event) {
                    Arrays.stream(event.getInventory().getContents())
                            // Null check is required... Thanks spigot.
                            .filter(Objects::nonNull)
                            .map(PhaseItem::fromItemStack)
                            .forEach(s -> {
                                if (moonPhase) {
                                    sel.setMoonPhase(s.first, s.second);
                                } else {
                                    sel.setPhaseCustom(s.first, s.second);
                                }
                            });
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
                            PhaseItem.changeProbability(event.getCurrentItem(), 1, moonPhase);
                            break;
                        case SHIFT_LEFT:
                            PhaseItem.changeProbability(event.getCurrentItem(), 10, moonPhase);
                            break;
                        case RIGHT:
                            PhaseItem.changeProbability(event.getCurrentItem(), -1, moonPhase);
                            break;
                        case SHIFT_RIGHT:
                            PhaseItem.changeProbability(event.getCurrentItem(), -10, moonPhase);
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
            sel.setNightSelectionType(parse);
            configuration.save();
            optPage.ifPresent(p -> sendWorldPage(world, sender, p));
            return true;
        }
        return true;
    }

    private void sendWorldPage(World world, CommandSender sender, int p) {
        TextComponent page = CommandUtil.getPage(configuration.getWorldSettings().values(), p, 3, 4, s -> {
            NightSelection ns = s.getNightSelection();
            String cmd = "/bloodnight nightSelection " + C.escapeWorldName(s.getWorldName()) + " ";
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
                    .append(CommandUtil.getToggleField(ns.getNightSelectionType() == NightSelection.NightSelectionType.REAL_MOON_PHASE,
                            cmd + "type real_moon_phase",
                            localizer().getMessage("state.realMoonPhase")))
                    .append(Component.space())
                    .append(CommandUtil.getToggleField(ns.getNightSelectionType() == NightSelection.NightSelectionType.INTERVAL,
                            cmd + "type interval",
                            localizer().getMessage("state.interval")))
                    .append(Component.space())
                    .append(CommandUtil.getToggleField(ns.getNightSelectionType() == NightSelection.NightSelectionType.PHASE,
                            cmd + "type phase",
                            localizer().getMessage("state.phase")))
                    .append(Component.space())
                    .append(CommandUtil.getToggleField(ns.getNightSelectionType() == NightSelection.NightSelectionType.CURVE,
                            cmd + "type curve",
                            localizer().getMessage("state.curve")))
                    .append(Component.space())
                    .append(Component.newline());

            switch (ns.getNightSelectionType()) {
                case RANDOM:
                    builder.append(Component.text(localizer().getMessage("field.probability") + ": ", NamedTextColor.AQUA))
                            .append(Component.text(ns.getProbability(), NamedTextColor.GOLD))
                            .append(Component.text(" [" + localizer().getMessage("action.change") + "]", NamedTextColor.GREEN)
                                    .clickEvent(ClickEvent.suggestCommand(cmd + "probability ")))
                            .append(Component.newline())
                            .append(Component.newline());
                    break;
                case REAL_MOON_PHASE:
                case MOON_PHASE:
                    builder.append(Component.text(localizer().getMessage("field.moonPhase") + ": ", NamedTextColor.AQUA))
                            .append(Component.text(" [" + localizer().getMessage("action.change") + "]", NamedTextColor.GREEN)
                                    .clickEvent(ClickEvent.runCommand(cmd + "moonPhase none")))
                            .append(Component.newline());
                    ns.getMoonPhase().forEach((key, value) ->
                            builder.append(Component.text("| " + key + ":" + value + " |", NamedTextColor.GOLD)
                                    .hoverEvent(
                                            HoverEvent.showText(
                                                    Component.text()
                                                            .append(Component.text(localizer().getMessage("field.moonPhase") + " " + key, NamedTextColor.GOLD))
                                                            .append(Component.newline())
                                                            .append(Component.text(localizer().getMessage(getMoonPhaseName(key)), NamedTextColor.AQUA))
                                                            .append(Component.newline())
                                                            .append(Component.text(getMoonPhaseSign(key)))
                                                            .append(Component.newline())
                                                            .append(Component.text(localizer().getMessage("field.probability") + ": " + value, NamedTextColor.GREEN))
                                                            .build()
                                            )
                                    )
                            ));
                    builder.append(Component.newline());
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
                                    .clickEvent(ClickEvent.suggestCommand(cmd + "intervalProbability ")))
                            .append(Component.newline());

                    break;
                case PHASE:
                    builder.append(Component.text(localizer().getMessage("field.amount") + ": ", NamedTextColor.AQUA))
                            .append(Component.text(ns.getPhaseCustom().size(), NamedTextColor.GOLD))
                            .append(Component.text(" [" + localizer().getMessage("action.change") + "]", NamedTextColor.GREEN)
                                    .clickEvent(ClickEvent.suggestCommand(cmd + "phaseAmount ")))
                            .append(Component.newline())
                            .append(Component.text(localizer().getMessage("field.phase") + ": ", NamedTextColor.AQUA))
                            .append(Component.text(" [" + localizer().getMessage("action.change") + "]", NamedTextColor.GREEN)
                                    .clickEvent(ClickEvent.runCommand(cmd + "phase none")))
                            .append(Component.newline());
                    break;
                case CURVE:
                    builder.append(Component.text(localizer().getMessage("field.length") + ": ", NamedTextColor.AQUA))
                            .append(Component.text(ns.getPeriod(), NamedTextColor.GOLD))
                            .append(Component.text(" [" + localizer().getMessage("action.change") + "]", NamedTextColor.GREEN)
                                    .clickEvent(ClickEvent.suggestCommand(cmd + "period ")))
                            .append(Component.newline())
                            .append(Component.text(localizer().getMessage("field.minProb") + ": ", NamedTextColor.AQUA))
                            .append(Component.text(ns.getMinCurveVal(), NamedTextColor.GOLD))
                            .append(Component.text(" [" + localizer().getMessage("action.change") + "]", NamedTextColor.GREEN)
                                    .clickEvent(ClickEvent.suggestCommand(cmd + "minCurveVal ")))
                            .append(Component.newline())
                            .append(Component.text(localizer().getMessage("field.maxProb") + ": ", NamedTextColor.AQUA))
                            .append(Component.text(ns.getMaxCurveVal(), NamedTextColor.GOLD))
                            .append(Component.text(" [" + localizer().getMessage("action.change") + "]", NamedTextColor.GREEN)
                                    .clickEvent(ClickEvent.suggestCommand(cmd + "maxCurveVal ")));
                    break;
            }
            return builder.build();
        }, localizer().getMessage("nightSelection.title.menu"),
                "/bloodNight nightSelection " + C.escapeWorldName(world) + " page {page}");
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
        if (TabCompleteUtil.isCommand(field, "intervalProbability", "probability", "minCurveVal", "maxCurveVal")) {
            return TabCompleteUtil.completeInt(value, 0, 100, localizer());
        }
        if (TabCompleteUtil.isCommand(field, "phaseAmount")) {
            return TabCompleteUtil.completeInt(value, 0, 54, localizer());
        }
        if (TabCompleteUtil.isCommand(field, "period")) {
            return TabCompleteUtil.completeInt(value, 3, 100, localizer());
        }

        if (TabCompleteUtil.isCommand(field, "moonPhase", "phase")) {
            return Collections.emptyList();
        }

        if (TabCompleteUtil.isCommand(field, "type")) {
            return TabCompleteUtil.complete(value, NightSelection.NightSelectionType.class);
        }
        return Collections.emptyList();
    }

    private static final class PhaseItem {
        private static final NamespacedKey PHASE = BloodNight.getNamespacedKey("phase");
        private static final NamespacedKey PROBABILITY = BloodNight.getNamespacedKey("probability");

        public static Pair<Integer, Integer> fromItemStack(ItemStack itemStack) {
            if (itemStack == null) return null;
            return Pair.of(getPhase(itemStack), getProbability(itemStack));
        }

        private static ItemStack toPhaseItem(Map.Entry<Integer, Integer> entry, boolean moon) {
            int phase = entry.getKey() + 1;
            ILocalizer localizer = ILocalizer.getPluginLocalizer(BloodNight.class);
            ItemStack stack = new ItemStack(Material.FIREWORK_STAR, phase);
            ItemMeta itemMeta = stack.getItemMeta();
            itemMeta.setDisplayName("§2" + localizer.getMessage("phaseItem.phase") + ": " + phase
                    + (moon ? " (" + localizer.getMessage(getMoonPhaseName(entry.getKey())) + ")" : ""));
            itemMeta.setLore(getLore(entry.getKey(), entry.getValue(), moon));
            PersistentDataContainer container = itemMeta.getPersistentDataContainer();
            container.set(PROBABILITY, PersistentDataType.INTEGER, entry.getValue());
            container.set(PHASE, PersistentDataType.INTEGER, entry.getKey());
            stack.setItemMeta(itemMeta);
            return stack;
        }

        private static int getPhase(ItemStack itemStack) {
            return itemStack.getItemMeta().getPersistentDataContainer().get(PHASE, PersistentDataType.INTEGER);
        }

        private static int getProbability(ItemStack itemStack) {
            return itemStack.getItemMeta().getPersistentDataContainer().get(PROBABILITY, PersistentDataType.INTEGER);
        }

        private static void setProbability(ItemStack item, int weight) {
            ItemMeta itemMeta = item.hasItemMeta() ? item.getItemMeta() : Bukkit.getItemFactory().getItemMeta(item.getType());
            PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();
            dataContainer.set(PROBABILITY, PersistentDataType.INTEGER, weight);
            item.setItemMeta(itemMeta);
        }

        private static void setProbabilityIfAbstent(ItemStack item, int weight) {
            ItemMeta itemMeta = item.hasItemMeta() ? item.getItemMeta() : Bukkit.getItemFactory().getItemMeta(item.getType());
            PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();
            if (!dataContainer.has(PROBABILITY, PersistentDataType.INTEGER)) {
                setProbability(item, weight);
            }
        }

        public static void changeProbability(ItemStack item, int change, boolean moon) {
            int currProb = getProbability(item);
            int phase = getPhase(item);
            int newProb = Math.min(Math.max(currProb + change, 0), 100);
            setProbability(item, newProb);
            ItemMeta itemMeta = item.getItemMeta();
            assert itemMeta != null;
            itemMeta.setLore(getLore(phase, newProb, moon));
            item.setItemMeta(itemMeta);
        }

        public static List<ItemStack> getPhaseItems(Map<Integer, Integer> selection, boolean moon) {
            return selection.entrySet().stream().map(e -> toPhaseItem(e, moon)).collect(Collectors.toList());
        }

        private static List<String> getLore(int phase, int probability, boolean moon) {
            List<String> result = new ArrayList<>();
            if (moon) {
                result.add(getMoonPhaseSign(phase));
            }
            result.add("§6" + ILocalizer.getPluginLocalizer(BloodNight.class).getMessage("field.probability") + ": " + probability);
            return result;
        }
    }
}
