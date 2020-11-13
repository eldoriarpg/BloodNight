package de.eldoria.bloodnight.command.bloodnight;

import de.eldoria.bloodnight.command.InventoryListener;
import de.eldoria.bloodnight.command.util.CommandUtil;
import de.eldoria.bloodnight.config.Configuration;
import de.eldoria.bloodnight.config.worldsettings.WorldSettings;
import de.eldoria.bloodnight.config.worldsettings.mobsettings.Drop;
import de.eldoria.bloodnight.config.worldsettings.mobsettings.MobSetting;
import de.eldoria.bloodnight.config.worldsettings.mobsettings.MobSettings;
import de.eldoria.bloodnight.config.worldsettings.mobsettings.MobValueModifier;
import de.eldoria.bloodnight.core.BloodNight;
import de.eldoria.bloodnight.core.mobfactory.MobFactory;
import de.eldoria.bloodnight.core.mobfactory.MobGroup;
import de.eldoria.bloodnight.core.mobfactory.SpecialMobRegistry;
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
import net.kyori.adventure.text.format.TextDecoration;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Collectors;

public class ManageMob extends EldoCommand {
    public final Configuration configuration;
    private final InventoryListener inventoryListener;
    private final BukkitAudiences bukkitAudiences;

    public ManageMob(Plugin plugin, Configuration configuration, InventoryListener inventoryListener) {
        super(plugin);
        this.configuration = configuration;
        this.inventoryListener = inventoryListener;
        bukkitAudiences = BukkitAudiences.create(BloodNight.getInstance());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (isConsole(sender)) {
            messageSender().sendError(sender, localizer().getMessage("error.console"));
            return true;
        }

        if (denyAccess(sender, Permissions.MANAGE_MOB)) {
            return true;
        }

        if (!(sender instanceof Player)) return true;

        Player player = (Player) sender;

        World world = ArgumentUtils.getOrDefault(args, 1, ArgumentUtils::getWorld, player.getWorld());

        if (world == null) {
            messageSender().sendError(sender, localizer().getMessage("error.invalidWorld"));
            return true;
        }

        WorldSettings worldSettings = configuration.getWorldSettings(world);

        if (!worldSettings.isEnabled()) {
            messageSender().sendError(player, localizer().getMessage("error.worldNotEnabled",
                    Replacement.create("WORLD", world.getName())));
            return true;
        }

        // group world mob value [page]
        if (argumentsInvalid(sender, args, 1, "<" + localizer().getMessage("syntax.mobGroup") + ">")) {
            return true;
        }

        String mobGroupName = args[0];

        Optional<Map.Entry<String, Set<MobSetting>>> optionalMobGroup = worldSettings.getMobSettings().getMobTypes().getGroup(mobGroupName);

        if (!optionalMobGroup.isPresent()) {
            messageSender().sendError(sender, localizer().getMessage("error.invalidMobGroup"));
            return true;
        }

        Map.Entry<String, Set<MobSetting>> mobGroup = optionalMobGroup.get();

        if (args.length < 3) {
            sendMobListPage(world, sender, mobGroup, 0);
            return true;
        }

        // group world page %page%
        if (argumentsInvalid(sender, args, 4,
                "<" + localizer().getMessage("syntax.mobGroup") + "> [<"
                        + localizer().getMessage("syntax.worldName") + "> <"
                        + localizer().getMessage("syntax.mob") + "> <"
                        + localizer().getMessage("syntax.field") + "> <"
                        + localizer().getMessage("syntax.value") + ">]")) {
            return true;
        }

        String mobString = args[2];
        String field = args[3];

        if ("page".equalsIgnoreCase(mobString)) {
            OptionalInt optPage = Parser.parseInt(field);
            if (optPage.isPresent()) {
                sendMobListPage(world, sender, mobGroup, optPage.getAsInt());
            }
            return true;
        }

        // group world mob value
        if (argumentsInvalid(sender, args, 4,
                "<" + localizer().getMessage("syntax.mobGroup") + "> [<"
                        + localizer().getMessage("syntax.worldName") + "> <"
                        + localizer().getMessage("syntax.mob") + "> <"
                        + localizer().getMessage("syntax.field") + "> <"
                        + localizer().getMessage("syntax.value") + ">]")) {
            return true;
        }

        String value = args[4];

        Optional<MobSetting> mobByName = worldSettings.getMobSettings().getMobByName(mobString);
        if (!mobByName.isPresent()) {
            messageSender().sendError(sender, localizer().getMessage("error.invalidMob"));
            return true;
        }

        MobSetting mob = mobByName.get();

        OptionalInt optPage = CommandUtil.findPage(mobGroup.getValue(), 2, m -> m.getMobName().equalsIgnoreCase(mobByName.get().getMobName()));

        if (ArrayUtil.arrayContains(new String[] {"state", "overrideDefault"}, field)) {
            Optional<Boolean> aBoolean = Parser.parseBoolean(value);
            if (!aBoolean.isPresent()) {
                messageSender().sendError(sender, localizer().getMessage("error.invalidBoolean"));
                return true;
            }
            if ("state".equalsIgnoreCase(field)) {
                mob.setActive(aBoolean.get());
            }
            if ("overrideDefault".equalsIgnoreCase(field)) {
                mob.setOverrideDefaultDrops(aBoolean.get());
            }
            optPage.ifPresent(i -> sendMobListPage(world, sender, mobGroup, i));
            configuration.save();
            return true;
        }

        if (ArrayUtil.arrayContains(new String[] {"displayName"}, field)) {
            String s = String.join(" ", Arrays.copyOfRange(args, 4, args.length));
            if ("displayName".equalsIgnoreCase(field)) {
                mob.setDisplayName(s);
            }
            optPage.ifPresent(i -> sendMobListPage(world, sender, mobGroup, i));
            configuration.save();
            return true;
        }

        if (ArrayUtil.arrayContains(new String[] {"dropAmount"}, field)) {
            OptionalInt num = Parser.parseInt(value);
            if (!num.isPresent()) {
                messageSender().sendError(sender, localizer().getMessage("error.invalidNumber"));
                return true;
            }
            if ("dropAmount".equalsIgnoreCase(field)) {
                if (invalidRange(sender, num.getAsInt(), 0, 100)) {
                    return true;
                }
                mob.setDropAmount(num.getAsInt());
            }
            optPage.ifPresent(i -> sendMobListPage(world, sender, mobGroup, i));
            configuration.save();
            return true;
        }

        if (ArrayUtil.arrayContains(new String[] {"health", "damage"}, field)) {
            OptionalDouble num = Parser.parseDouble(value);
            if (!num.isPresent()) {
                messageSender().sendError(sender, localizer().getMessage("error.invalidNumber"));
                return true;
            }
            if ("health".equalsIgnoreCase(field)) {
                if (invalidRange(sender, num.getAsDouble(), 1, 500)) {
                    return true;
                }
                mob.setHealth(num.getAsDouble());
            }
            if ("damage".equalsIgnoreCase(field)) {
                if (invalidRange(sender, num.getAsDouble(), 1, 500)) {
                    return true;
                }
                mob.setDamage(num.getAsDouble());
            }
            optPage.ifPresent(i -> sendMobListPage(world, sender, mobGroup, i));
            configuration.save();
            return true;
        }
        if (ArrayUtil.arrayContains(new String[] {"healthModifier", "damageModifier"}, field)) {
            MobValueModifier val = EnumUtil.parse(value, MobValueModifier.class);
            if (val == null) {
                messageSender().sendError(sender, localizer().getMessage("error.invalidValue"));
                return true;
            }
            if ("healthModifier".equalsIgnoreCase(field)) {
                mob.setHealthModifier(val);
            }
            if ("damageModifier".equalsIgnoreCase(field)) {
                mob.setDamageModifier(val);
            }
            optPage.ifPresent(i -> sendMobListPage(world, sender, mobGroup, i));
            configuration.save();
            return true;
        }

        if ("drops".equalsIgnoreCase(field)) {
            if (!ArrayUtil.arrayContains(new String[] {"changeContent", "changeWeight", "clear"}, value)) {
                messageSender().sendError(sender, localizer().getMessage("error.invalidValue"));
            }

            if ("changeContent".equalsIgnoreCase(value)) {
                Inventory inv = Bukkit.createInventory(player, 54, localizer().getMessage("drops.dropsTitle"));
                inv.setContents(mob.getDrops().stream().map(Drop::getWeightedItem).toArray(ItemStack[]::new));
                player.openInventory(inv);
                inventoryListener.registerModification(player, new InventoryListener.InventoryActionHandler() {
                    @Override
                    public void onInventoryClose(InventoryCloseEvent event) {
                        List<Drop> collect = Arrays.stream(event.getInventory().getContents())
                                .filter(Objects::nonNull)
                                .map(Drop::fromItemStack)
                                .collect(Collectors.toList());
                        mob.setDrops(collect);
                        optPage.ifPresent(i -> sendMobListPage(world, sender, mobGroup, i));
                    }

                    @Override
                    public void onInventoryClick(InventoryClickEvent event) {
                    }
                });
            }

            if ("changeWeight".equalsIgnoreCase(value)) {
                List<ItemStack> stacks = mob.getDrops().stream().map(Drop::getItemWithLoreWeight).collect(Collectors.toList());
                Inventory inv = Bukkit.createInventory(player, 54, localizer().getMessage("drops.weightTitle"));
                inv.setContents(stacks.toArray(new ItemStack[0]));
                player.openInventory(inv);
                inventoryListener.registerModification(player, new InventoryListener.InventoryActionHandler() {
                    @Override
                    public void onInventoryClose(InventoryCloseEvent event) {
                        List<Drop> collect = Arrays.stream(event.getInventory().getContents())
                                .filter(Objects::nonNull)
                                .map(Drop::fromItemStack)
                                .collect(Collectors.toList());
                        mob.setDrops(collect);
                        optPage.ifPresent(i -> sendMobListPage(world, sender, mobGroup, i));
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
            }

            if ("clear".equalsIgnoreCase(value)) {
                mob.setDrops(new ArrayList<>());
                optPage.ifPresent(i -> sendMobListPage(world, sender, mobGroup, i));
                configuration.save();
            }
            return true;
        }

        messageSender().sendError(sender, localizer().getMessage("error.invalidField"));
        return true;
    }

    private void sendMobListPage(World world, CommandSender
            sender, Map.Entry<String, Set<MobSetting>> mobGroup, int page) {
        MobSettings mobSettings = configuration.getWorldSettings(world).getMobSettings();
        TextComponent component = CommandUtil.getPage(
                new ArrayList<>(mobGroup.getValue()),
                page,
                2, 7,
                entry -> {
                    String cmd = "/bloodnight manageMob " + mobGroup.getKey() + " " + ArgumentUtils.escapeWorldName(world.getName()) + " " + entry.getMobName() + " ";
                    TextComponent.Builder builder = Component.text()
                            // Mob name
                            .append(Component.text(entry.getMobName(), NamedTextColor.GOLD, TextDecoration.BOLD))
                            .append(Component.space())
                            // Mob state
                            .append(CommandUtil.getBooleanField(entry.isActive(),
                                    cmd + "state {bool}",
                                    "",
                                    localizer().getMessage("state.enabled"),
                                    localizer().getMessage("state.disabled")))
                            // Display name
                            .append(Component.newline()).append(Component.text("  "))
                            .append(Component.text(localizer().getMessage("field.displayName") + ": ", NamedTextColor.AQUA))
                            .append(Component.text(entry.getDisplayName(), NamedTextColor.GOLD))
                            .append(Component.text(" [" + localizer().getMessage("action.change") + "]", NamedTextColor.GREEN)
                                    .clickEvent(ClickEvent.suggestCommand(cmd + "displayName " + entry.getDisplayName().replace("§", "&"))))
                            .append(Component.newline()).append(Component.text("  "))
                            // Drop amount
                            .append(Component.text(localizer().getMessage("field.dropAmount") + ": ", NamedTextColor.AQUA))
                            .append(Component.text(
                                    entry.getDropAmount() <= 0 ? localizer().getMessage("action.default") : entry.getDropAmount() + "x", NamedTextColor.GOLD))
                            .append(Component.text(" [" + localizer().getMessage("action.change") + "]", NamedTextColor.GREEN)
                                    .clickEvent(ClickEvent.suggestCommand(cmd + "dropAmount ")))
                            .append(Component.newline()).append(Component.text("  "))
                            // drops
                            .append(Component.text(localizer().getMessage("field.drops") + ": ", NamedTextColor.AQUA))
                            .append(Component.text(entry.getDrops().size() + " " + localizer().getMessage("field.drops"), NamedTextColor.GOLD))
                            .append(Component.text(" [" + localizer().getMessage("action.content") + "]", NamedTextColor.GREEN)
                                    .clickEvent(ClickEvent.runCommand(cmd + "drops changeContent")))
                            .append(Component.text(" [" + localizer().getMessage("action.weight") + "]", NamedTextColor.GOLD)
                                    .clickEvent(ClickEvent.runCommand(cmd + "drops changeWeight")))
                            .append(Component.text(" [" + localizer().getMessage("action.clear") + "]", NamedTextColor.RED)
                                    .clickEvent(ClickEvent.runCommand(cmd + "drops clear")))
                            // override drops
                            .append(Component.newline()).append(Component.text("  "))
                            .append(CommandUtil.getBooleanField(entry.isOverrideDefaultDrops(),
                                    cmd + "overrideDefault {bool} " + page,
                                    localizer().getMessage("field.overrideDefaultDrops"),
                                    localizer().getMessage("state.override"),
                                    localizer().getMessage("state.combine")))
                            .append(Component.newline()).append(Component.text("  "))
                            // health modifier
                            .append(Component.text("Health Modifier: ", NamedTextColor.AQUA))
                            .append(CommandUtil.getToggleField(entry.getHealthModifier() == MobValueModifier.DEFAULT,
                                    cmd + "healthModifier DEFAULT",
                                    localizer().getMessage("action.default")))
                            .append(Component.space())
                            .append(CommandUtil.getToggleField(entry.getHealthModifier() == MobValueModifier.MULTIPLY,
                                    cmd + "healthModifier MULTIPLY",
                                    localizer().getMessage("action.multiply")))
                            .append(Component.space())
                            .append(CommandUtil.getToggleField(entry.getHealthModifier() == MobValueModifier.VALUE,
                                    cmd + "healthModifier VALUE",
                                    localizer().getMessage("action.value")))
                            .append(Component.newline()).append(Component.text("  "))
                            .append(Component.text(localizer().getMessage("field.health") + ": ", NamedTextColor.AQUA));
                    switch (entry.getHealthModifier()) {
                        case DEFAULT:
                            builder.append(Component.text(localizer().getMessage("action.default") + " (" + mobSettings.getHealthModifier() + "x)", NamedTextColor.GOLD));
                            break;
                        case MULTIPLY:
                            builder.append(Component.text(entry.getHealth() + "x", NamedTextColor.GOLD));
                            break;
                        case VALUE:
                            builder.append(Component.text(entry.getHealth() + " " + localizer().getMessage("field.health"), NamedTextColor.GOLD));
                            break;
                    }
                    builder.append(Component.text(" [" + localizer().getMessage("action.change") + "]", NamedTextColor.GREEN)
                            .clickEvent(ClickEvent.suggestCommand(cmd + "health ")));
                    // damage modifier
                    builder.append(Component.newline()).append(Component.text("  "))
                            .append(Component.text("Damage Modifier: ", NamedTextColor.AQUA))
                            .append(CommandUtil.getToggleField(entry.getDamageModifier() == MobValueModifier.DEFAULT,
                                    cmd + "damageModifier DEFAULT",
                                    localizer().getMessage("action.default")))
                            .append(Component.space())
                            .append(CommandUtil.getToggleField(entry.getDamageModifier() == MobValueModifier.MULTIPLY,
                                    cmd + "damageModifier MULTIPLY",
                                    localizer().getMessage("action.multiply")))
                            .append(Component.space())
                            .append(CommandUtil.getToggleField(entry.getDamageModifier() == MobValueModifier.VALUE,
                                    cmd + "damageModifier VALUE",
                                    localizer().getMessage("action.value")))
                            .append(Component.newline()).append(Component.text("  "))
                            .append(Component.text(localizer().getMessage("field.damage") + ": ", NamedTextColor.AQUA));
                    switch (entry.getDamageModifier()) {
                        case DEFAULT:
                            builder.append(Component.text(localizer().getMessage("action.default") + " (" + mobSettings.getHealthModifier() + "x)", NamedTextColor.GOLD));
                            break;
                        case MULTIPLY:
                            builder.append(Component.text(entry.getDamage() + "x", NamedTextColor.GOLD));
                            break;
                        case VALUE:
                            builder.append(Component.text(entry.getDamage() + " " + localizer().getMessage("field.damage"), NamedTextColor.GOLD));
                            break;
                    }
                    builder.append(Component.text(" [" + localizer().getMessage("action.change") + "]", NamedTextColor.GREEN)
                            .clickEvent(ClickEvent.suggestCommand(cmd + "damage ")));
                    return builder.build();
                },
                localizer().getMessage("manageMob.title",
                        Replacement.create("TYPE", mobGroup.getKey()),
                        Replacement.create("WORLD", world.getName())),
                "/bloodNight manageMob " + mobGroup.getKey() + " " + ArgumentUtils.escapeWorldName(world) + " page {page}");

        bukkitAudiences.sender(sender).sendMessage(Identity.nil(), component);
    }

    //group world mob field value
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command
            command, @NotNull String alias, @NotNull String[] args) {
        // mobgroup
        if (args.length == 1) {
            return TabCompleteUtil.complete(args[0], SpecialMobRegistry.getMobGroups().keySet(), Class::getSimpleName);
        }
        // world
        if (args.length == 2) {
            return TabCompleteUtil.complete(args[1], configuration.getWorldSettings().keySet());
        }
        // mob
        if (args.length == 3) {
            Optional<MobGroup> mobGroup = SpecialMobRegistry.getMobGroup(args[0]);
            if (!mobGroup.isPresent()) {
                return Collections.singletonList(localizer().getMessage("error.invalidMobGroup"));
            }
            return TabCompleteUtil.complete(args[2], mobGroup.get().getFactories(), MobFactory::getMobName);
        }
        // field
        String scmd = args[3];
        if (args.length == 4) {
            return TabCompleteUtil.complete(scmd, "state", "overrideDefault", "displayName",
                    "dropAmount", "health", "damage", "healthModifier", "damageModifier", "drops");
        }

        String val = args[4];
        if (args.length == 5) {
            if (TabCompleteUtil.isCommand(scmd, "state", "overrideDefault")) {
                return TabCompleteUtil.completeBoolean(val);
            }

            if (TabCompleteUtil.isCommand(scmd, "dropAmount")) {
                return TabCompleteUtil.completeInt(val, 1, 100, localizer());
            }

            if (TabCompleteUtil.isCommand(scmd, "health", "damage")) {
                return TabCompleteUtil.completeInt(val, 1, 500, localizer());
            }

            if (TabCompleteUtil.isCommand(scmd, "healthModifier", "damageModifier")) {
                return TabCompleteUtil.complete(val, MobValueModifier.class);
            }

            if (TabCompleteUtil.isCommand(scmd, "drops")) {
                return TabCompleteUtil.complete(val, "changeContent", "changeWeight", "clear");
            }
        }

        if (TabCompleteUtil.isCommand(scmd, "displayName")) {
            return TabCompleteUtil.completeFreeInput(ArgumentUtils.getRangeAsString(args, 4),
                    16, localizer().getMessage("field.displayName"), localizer());
        }

        return Collections.emptyList();
    }
}
