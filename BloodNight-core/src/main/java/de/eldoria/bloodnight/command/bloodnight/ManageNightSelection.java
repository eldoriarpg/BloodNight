package de.eldoria.bloodnight.command.bloodnight;

import de.eldoria.bloodnight.command.InventoryListener;
import de.eldoria.bloodnight.command.util.CommandUtil;
import de.eldoria.bloodnight.config.Configuration;
import de.eldoria.bloodnight.config.worldsettings.NightSelection;
import de.eldoria.bloodnight.config.worldsettings.WorldSettings;
import de.eldoria.bloodnight.core.BloodNight;
import de.eldoria.bloodnight.util.Permissions;
import de.eldoria.eldoutilities.commands.Completion;
import de.eldoria.eldoutilities.commands.command.AdvancedCommand;
import de.eldoria.eldoutilities.commands.command.CommandMeta;
import de.eldoria.eldoutilities.commands.command.util.Arguments;
import de.eldoria.eldoutilities.commands.command.util.Input;
import de.eldoria.eldoutilities.commands.exceptions.CommandException;
import de.eldoria.eldoutilities.commands.executor.IPlayerTabExecutor;
import de.eldoria.eldoutilities.container.Pair;
import de.eldoria.eldoutilities.localization.ILocalizer;
import de.eldoria.eldoutilities.messages.Replacement;
import de.eldoria.eldoutilities.utils.ArgumentUtils;
import de.eldoria.eldoutilities.utils.EMath;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
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
import java.util.Optional;
import java.util.stream.Collectors;

import static de.eldoria.bloodnight.command.util.CommandUtil.changeButton;
import static de.eldoria.bloodnight.command.util.CommandUtil.changeableValue;
import static de.eldoria.bloodnight.command.util.CommandUtil.getToggleField;
import static de.eldoria.bloodnight.config.worldsettings.NightSelection.NightSelectionType;
import static de.eldoria.bloodnight.config.worldsettings.NightSelection.NightSelectionType.CURVE;
import static de.eldoria.bloodnight.config.worldsettings.NightSelection.NightSelectionType.INTERVAL;
import static de.eldoria.bloodnight.config.worldsettings.NightSelection.NightSelectionType.MOON_PHASE;
import static de.eldoria.bloodnight.config.worldsettings.NightSelection.NightSelectionType.PHASE;
import static de.eldoria.bloodnight.config.worldsettings.NightSelection.NightSelectionType.RANDOM;
import static de.eldoria.bloodnight.config.worldsettings.NightSelection.NightSelectionType.REAL_MOON_PHASE;
import static de.eldoria.eldoutilities.localization.ILocalizer.escape;

public class ManageNightSelection extends AdvancedCommand implements IPlayerTabExecutor {
    private final Configuration configuration;
    private final InventoryListener inventoryListener;
    private final BukkitAudiences bukkitAudiences;

    public ManageNightSelection(Plugin plugin, Configuration configuration, InventoryListener inventoryListener) {
        super(plugin, CommandMeta.builder("nightSelection")
                .addArgument("syntax.worldName", false)
                .addArgument("syntax.field", false)
                .addArgument("syntax.value", false)
                .withPermission(Permissions.Admin.MANAGE_WORLDS)
                .build());
        this.configuration = configuration;
        this.inventoryListener = inventoryListener;
        bukkitAudiences = BukkitAudiences.create(BloodNight.getInstance());
    }

    private static String getMoonPhaseName(int phase) {
        return "state.phase" + phase;
    }

    private static String getMoonPhaseSign(int phase) {
        return switch (phase) {
            case 0 -> "<white>████";
            case 1 -> "<white>███<gray>█";
            case 2 -> "<white>██<gray>██";
            case 3 -> "<white>█<gray>███";
            case 4 -> "<gray>████";
            case 5 -> "<gray>███<white>█";
            case 6 -> "<gray>██<white>██";
            case 7 -> "<gray>█<white>███";
            default -> throw new IllegalStateException("Unexpected value: " + phase);
        };
    }
    private static String getMoonPhaseSignLegacy(int phase) {
        return switch (phase) {
            case 0 -> "§f████";
            case 1 -> "§f███§7█";
            case 2 -> "§f██§7██";
            case 3 -> "§f█§7███";
            case 4 -> "§7████";
            case 5 -> "§7███§f█";
            case 6 -> "§7██§f██";
            case 7 -> "§7█§f███";
            default -> throw new IllegalStateException("Unexpected value: " + phase);
        };
    }

    // world field value

    @Override
    public void onCommand(@NotNull Player player, @NotNull String alias, @NotNull Arguments args) throws CommandException {
        World world = args.asWorld(0, player.getWorld());

        WorldSettings worldSettings = configuration.getWorldSettings(world);

        if (args.size() < 2) {
            sendWorldPage(world, player, 0);
            return;
        }

        String field = args.asString(1);
        Input value = args.get(2, Input.of(plugin(), "none"));

        Optional<Integer> optPage = CommandUtil.findPage(configuration.getWorldSettings().values(), 3,
                w -> w.getWorldName().equalsIgnoreCase(world.getName()));

        if ("page".equalsIgnoreCase(field)) {
            sendWorldPage(world, player, value.asInt());
            return;
        }

        final NightSelection sel = worldSettings.getNightSelection();
        if (Completion.isCommand(field, "interval", "intervalProbability", "probability", "phaseAmount",
                "period", "minCurveVal", "maxCurveVal")) {

            if ("interval".equalsIgnoreCase(field)) {
                sel.setInterval(EMath.clamp(1, 100, value.asInt()));
            }
            if ("intervalProbability".equalsIgnoreCase(field)) {
                sel.setIntervalProbability(EMath.clamp(0, 100, value.asInt()));
            }
            if ("probability".equalsIgnoreCase(field)) {
                sel.setProbability(EMath.clamp(0, 100, value.asInt()));
            }
            if ("phaseAmount".equalsIgnoreCase(field)) {
                sel.setPhaseCount(EMath.clamp(1, 54, value.asInt()));
            }
            if ("period".equalsIgnoreCase(field)) {
                sel.setPeriod(EMath.clamp(3, 100, value.asInt()));
            }
            if ("minCurveVal".equalsIgnoreCase(field)) {
                sel.setMinCurveVal(EMath.clamp(0, 100, value.asInt()));
            }
            if ("maxCurveVal".equalsIgnoreCase(field)) {
                sel.setMaxCurveVal(EMath.clamp(0, 100, value.asInt()));
            }
            optPage.ifPresent(p -> sendWorldPage(world, player, p));
            configuration.save();
            return;
        }

        if (Completion.isCommand(field, "moonPhase", "phase")) {
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
                    optPage.ifPresent(i -> sendWorldPage(world, player, i));
                }

                @Override
                public void onInventoryClick(InventoryClickEvent event) {
                    if (event.getInventory().getType() != InventoryType.CHEST) return;

                    if (!event.getView().getTopInventory().equals(event.getClickedInventory())) {
                        return;
                    }

                    switch (event.getClick()) {
                        case LEFT -> PhaseItem.changeProbability(event.getCurrentItem(), 1, moonPhase);
                        case SHIFT_LEFT -> PhaseItem.changeProbability(event.getCurrentItem(), 10, moonPhase);
                        case RIGHT -> PhaseItem.changeProbability(event.getCurrentItem(), -1, moonPhase);
                        case SHIFT_RIGHT -> PhaseItem.changeProbability(event.getCurrentItem(), -10, moonPhase);
                    }
                    event.setCancelled(true);
                }
            });
            return;
        }

        if (Completion.isCommand(field, "type")) {
            sel.setNightSelectionType(value.asEnum(NightSelectionType.class));
            configuration.save();
            optPage.ifPresent(p -> sendWorldPage(world, player, p));
        }
    }

    private void sendWorldPage(World world, CommandSender sender, int p) {
        String page = CommandUtil.getPage(configuration.getWorldSettings().values(), p, 3, 4, s -> {
                    NightSelection ns = s.getNightSelection();
                    String cmd = "/bloodnight nightSelection " + ArgumentUtils.escapeWorldName(s.getWorldName()) + " ";
                    String type = switch (ns.getNightSelectionType()) {
                        case RANDOM -> """
                                %s
                                                                
                                                                
                                """.stripIndent()
                                .formatted(changeableValue("field.probability", ns.getProbability(), cmd + "probability "));
                        case REAL_MOON_PHASE, MOON_PHASE -> {
                            var phases = ns.getMoonPhase().entrySet().stream().map(e -> {
                                var hover = """
                                        %s %s
                                        %s
                                        %s
                                        %s: %s
                                        """.stripIndent()
                                        .formatted(escape("field.moonPhase"), e.getKey(),
                                                escape(getMoonPhaseName(e.getKey())),
                                                getMoonPhaseSign(e.getKey()),
                                                escape("field.probability"), e.getValue());
                                return "<value><hover:show_text:'%s'>| %s:%s |".formatted(hover, e.getKey(), e.getValue());
                            }).toList();
                            yield """
                                    <field>%s: %s
                                    %s
                                    """.stripIndent()
                                    .formatted(
                                            escape("field.moonPhase"),
                                            changeButton(cmd + "moonPhase none"),
                                            String.join(" ", phases)
                                    );
                        }
                        case INTERVAL -> """
                                %s
                                %s
                                """.stripIndent()
                                .formatted(
                                        changeableValue("field.interval", ns.getInterval(), cmd + "interval "),
                                        changeableValue("field.intervalProbability", ns.getIntervalProbability(), cmd + "intervalProbability ")
                                );
                        case PHASE -> """
                                %s
                                %s %s
                                """.stripIndent()
                                .formatted(
                                        changeableValue("field.amount", ns.getPhaseCustom().size(), cmd + "phaseAmount "),
                                        escape("field.amount"),
                                        changeButton(cmd + "phase none ")
                                );
                        case CURVE -> """
                                %s
                                %s
                                %s
                                """.stripIndent()
                                .formatted(
                                        changeableValue("field.length", ns.getPeriod(), cmd + "period "),
                                        changeableValue("field.minProb", ns.getMinCurveVal(), cmd + "minCurveVal "),
                                        changeableValue("field.maxProb", ns.getMaxCurveVal(), cmd + "maxCurveVal ")
                                );
                    };

                    var a = """
                            <header>%s</header>
                            %s:
                            %s %s %s %s %s %s
                            %s
                            """.stripIndent()
                            .formatted(
                                    s.getWorldName(),
                                    // Night selection
                                    escape("field.nightSelectionType"),
                                    // Types
                                    getToggleField(ns.getNightSelectionType() == RANDOM, cmd + "type random", "state.random"),
                                    getToggleField(ns.getNightSelectionType() == MOON_PHASE, cmd + "type moon_phase", "state.moonPhase"),
                                    getToggleField(ns.getNightSelectionType() == REAL_MOON_PHASE, cmd + "type real_moon_phase", "state.realMoonPhase"),
                                    getToggleField(ns.getNightSelectionType() == INTERVAL, cmd + "type interval", "state.interval"),
                                    getToggleField(ns.getNightSelectionType() == PHASE, cmd + "type phase", "state.phase"),
                                    getToggleField(ns.getNightSelectionType() == CURVE, cmd + "type curve", "state.curve"),
                                    type
                            );
                    TextComponent.Builder builder = Component.text()
                            .append(Component.text(s.getWorldName(), NamedTextColor.GOLD, TextDecoration.BOLD))
                            .append(Component.newline())
                            .append(Component.text(localizer().getMessage("field.nightSelectionType") + ":", NamedTextColor.AQUA))
                            .append(Component.newline())
                            .append(Component.space())
                            .append(Component.newline());

                    return a;
                }, "nightSelection.title.menu",
                "/bloodnight nightSelection " + ArgumentUtils.escapeWorldName(world) + " page {page}");
        messageSender().sendMessage(sender, page, Replacement.create("WORLD", world));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull Player player, @NotNull String alias, @NotNull Arguments args) throws CommandException {
        if (args.size() == 1) {
            return Completion.completeWorlds(args.asString(0));
        }
        if (args.size() == 2) {
            return Completion.complete(args.asString(1), "interval", "intervalProbability", "probability", "moonPhase", "type");
        }

        String field = args.asString(1);
        String value = args.asString(2);
        if (Completion.isCommand(field, "interval")) {
            return Completion.completeInt(value, 1, 100);
        }
        if (Completion.isCommand(field, "intervalProbability", "probability", "minCurveVal", "maxCurveVal")) {
            return Completion.completeInt(value, 0, 100);
        }
        if (Completion.isCommand(field, "phaseAmount")) {
            return Completion.completeInt(value, 0, 54);
        }
        if (Completion.isCommand(field, "period")) {
            return Completion.completeInt(value, 3, 100);
        }

        if (Completion.isCommand(field, "moonPhase", "phase")) {
            return Collections.emptyList();
        }

        if (Completion.isCommand(field, "type")) {
            return Completion.complete(value, NightSelectionType.class);
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
                result.add(getMoonPhaseSignLegacy(phase));
            }
            result.add("§6" + ILocalizer.getPluginLocalizer(BloodNight.class).getMessage("field.probability") + ": " + probability);
            return result;
        }
    }
}
