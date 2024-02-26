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
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
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
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static de.eldoria.bloodnight.command.util.CommandUtil.changeButton;
import static de.eldoria.bloodnight.command.util.CommandUtil.changeableValue;
import static de.eldoria.bloodnight.command.util.CommandUtil.getBooleanField;
import static de.eldoria.bloodnight.command.util.CommandUtil.getToggleField;
import static de.eldoria.eldoutilities.localization.ILocalizer.escape;

public class ManageMob extends AdvancedCommand implements IPlayerTabExecutor {
    public final Configuration configuration;
    private final InventoryListener inventoryListener;
    private final BukkitAudiences bukkitAudiences;

    public ManageMob(Plugin plugin, Configuration configuration, InventoryListener inventoryListener) {
        super(plugin, CommandMeta.builder("manageMob")
                .withPermission(Permissions.Admin.MANAGE_MOB)
                .addArgument("syntax.mobGroup", true)
                .addArgument("syntax.worldName", false)
                .addArgument("syntax.field", false)
                .addArgument("syntax.value", false)
                .build());
        this.configuration = configuration;
        this.inventoryListener = inventoryListener;
        bukkitAudiences = BukkitAudiences.create(BloodNight.getInstance());
    }

    @Override
    public void onCommand(@NotNull Player player, @NotNull String alias, @NotNull Arguments args) throws CommandException {
        World world = args.asWorld(1, player.getWorld());

        WorldSettings worldSettings = configuration.getWorldSettings(world);
        CommandAssertions.isTrue(worldSettings.isEnabled(), "error.worldNotEnabled", Replacement.create("WORLD", world));

        String mobGroupName = args.asString(0);

        var optionalMobGroup = worldSettings.getMobSettings().getMobTypes().getGroup(mobGroupName);

        CommandAssertions.isTrue(optionalMobGroup.isPresent(), "error.invalidMobGroup");

        var mobGroup = optionalMobGroup.get();

        if (args.size() < 3) {
            sendMobListPage(world, player, mobGroup, 0);
            return;
        }

        String mobString = args.asString(2);
        Input field = args.get(3);

        if ("page".equalsIgnoreCase(mobString)) {
            sendMobListPage(world, player, mobGroup, field.asInt());
            return;
        }

        Input value = args.get(4);

        Optional<MobSetting> mobByName = worldSettings.getMobSettings().getMobByName(mobString);
        CommandAssertions.isTrue(mobByName.isPresent(), "error.invalidMob");

        MobSetting mob = mobByName.get();

        Optional<Integer> optPage = CommandUtil.findPage(mobGroup.getValue(), 2, m -> m.getMobName().equalsIgnoreCase(mobByName.get().getMobName()));

        if (Completion.isCommand(field.asString(), "state", "overrideDefault")) {
            if ("state".equalsIgnoreCase(field.asString())) {
                mob.setActive(value.asBoolean());
            }
            if ("overrideDefault".equalsIgnoreCase(field.asString())) {
                mob.setOverrideDefaultDrops(value.asBoolean());
            }
            optPage.ifPresent(i -> sendMobListPage(world, player, mobGroup, i));
            configuration.save();
            return;
        }

        if (Completion.isCommand(field.asString(), "displayname")) {
            if ("displayName".equalsIgnoreCase(field.asString())) {
                mob.setDisplayName(args.join(4));
            }
            optPage.ifPresent(i -> sendMobListPage(world, player, mobGroup, i));
            configuration.save();
            return;
        }

        if (Completion.isCommand(field.asString(), "dropAmount")) {
            if ("dropAmount".equalsIgnoreCase(field.asString())) {
                CommandAssertions.range(value.asInt(), 0, 100);
                mob.setDropAmount(value.asInt());
            }
            optPage.ifPresent(i -> sendMobListPage(world, player, mobGroup, i));
            configuration.save();
            return;
        }

        if (Completion.isCommand(field.asString(), "health", "damage")) {
            if ("health".equalsIgnoreCase(field.asString())) {
                CommandAssertions.range(value.asInt(), 1, 500);
                mob.setHealth(value.asInt());
            }
            if ("damage".equalsIgnoreCase(field.asString())) {
                CommandAssertions.range(value.asInt(), 1, 500);
                mob.setDamage(value.asInt());
            }
            optPage.ifPresent(i -> sendMobListPage(world, player, mobGroup, i));
            configuration.save();
            return;
        }
        if (Completion.isCommand(field.asString(), "healthModifier", "damageModifier")) {
            if ("healthModifier".equalsIgnoreCase(field.asString())) {
                mob.setHealthModifier(value.asEnum(MobValueModifier.class));
            }
            if ("damageModifier".equalsIgnoreCase(field.asString())) {
                mob.setDamageModifier(value.asEnum(MobValueModifier.class));
            }
            optPage.ifPresent(i -> sendMobListPage(world, player, mobGroup, i));
            configuration.save();
            return;
        }

        if ("drops".equalsIgnoreCase(field.asString())) {
            CommandAssertions.isTrue(Completion.isCommand(value.asString(), "changeContent", "changeWeight", "clear"), "error.invalidValue");

            if ("changeContent".equalsIgnoreCase(value.asString())) {
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
                        optPage.ifPresent(i -> sendMobListPage(world, player, mobGroup, i));
                    }

                    @Override
                    public void onInventoryClick(InventoryClickEvent event) {
                    }
                });
            }

            if ("changeWeight".equalsIgnoreCase(value.asString())) {
                Inventory inv = Bukkit.createInventory(player, 54, localizer().getMessage("drops.weightTitle"));
                inv.setContents(mob.getDrops().stream().map(Drop::getItemWithLoreWeight).toArray(ItemStack[]::new));
                player.openInventory(inv);
                inventoryListener.registerModification(player, new InventoryListener.InventoryActionHandler() {
                    @Override
                    public void onInventoryClose(InventoryCloseEvent event) {
                        List<Drop> collect = Arrays.stream(event.getInventory().getContents())
                                .filter(Objects::nonNull)
                                .map(Drop::fromItemStack)
                                .collect(Collectors.toList());
                        mob.setDrops(collect);
                        optPage.ifPresent(i -> sendMobListPage(world, player, mobGroup, i));
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
            }

            if ("clear".equalsIgnoreCase(value.asString())) {
                mob.setDrops(new ArrayList<>());
                optPage.ifPresent(i -> sendMobListPage(world, player, mobGroup, i));
                configuration.save();
            }
            return;
        }

        messageSender().sendError(player, localizer().getMessage("error.invalidField"));
    }

    private void sendMobListPage(World world, CommandSender sender, Map.Entry<String, Set<MobSetting>> mobGroup, int page) {
        MobSettings mobSettings = configuration.getWorldSettings(world).getMobSettings();
        List<TagResolver> resolver = new ArrayList<>();
        String component = CommandUtil.getPage(
                new ArrayList<>(mobGroup.getValue()),
                page,
                2, 7,
                entry -> {
                    String cmd = "/bloodnight manageMob " + mobGroup.getKey() + " " + ArgumentUtils.escapeWorldName(world.getName()) + " " + entry.getMobName() + " ";
                    var healthModifier = switch (entry.getHealthModifier()) {
                        case DEFAULT ->
                                "<value>%s (%sx)".formatted(escape("action.default"), mobSettings.getHealthModifier());
                        case MULTIPLY -> "<value>%sx".formatted(entry.getHealth());
                        case VALUE -> "<value>%s %s".formatted(entry.getHealth(), escape("field.health"));
                    };
                    var damageModifier = switch (entry.getDamageModifier()) {
                        case DEFAULT ->
                                "<value>%s (%sx)".formatted(escape("action.default"), mobSettings.getDamageMultiplier());
                        case MULTIPLY -> "<value>%sx".formatted(entry.getDamage());
                        case VALUE -> "<value>%s %s".formatted(entry.getDamage(), escape("field.health"));
                    };


                    return """
                            <header>%s</header> %s
                              %s
                              %s
                              <field>%s: <value>%s %s %s %s
                              %s
                              <field>Health Modifier: %s %s %s
                              <field>%s: %s %s
                              <field>Damage Modifier: %s %s %s
                              <field>%s: %s %s
                            """.stripIndent()
                            .formatted(
                                    // Mob name state
                                    entry.getMobName(), getBooleanField(entry.isActive(), cmd + "state {bool}", "", "state.enabled", "state.disabled"),
                                    // Display Name
                                    changeableValue("field.displayName", entry.getDisplayName(), cmd + "displayName " + entry.getDisplayName().replace("§", "&")),
                                    // Drop amount
                                    changeableValue("field.dropAmount", entry.getDropAmount() == 0 ? escape("action.default") : entry.getDropAmount() + "x", cmd + "dropAmount "),
                                    // Drops
                                    escape("field.drops"),
                                    entry.getDrops().size() + " " + escape("field.drops"),
                                    changeButton(cmd + "drops changeContent", "action.content", "change"),
                                    changeButton(cmd + "drops changeWeight", "action.weight", "weight"),
                                    changeButton(cmd + "drops clear", "action.clear", "delete"),
                                    getBooleanField(entry.isOverrideDefaultDrops(), cmd + "overrideDefault {bool} " + page, "field.overrideDefaultDrops", "state.override", "state.combine"),
                                    getToggleField(entry.getHealthModifier() == MobValueModifier.DEFAULT, cmd + "healthModifier DEFAULT", "action.default"),
                                    getToggleField(entry.getHealthModifier() == MobValueModifier.MULTIPLY, cmd + "healthModifier MULTIPLY", "action.multiply"),
                                    getToggleField(entry.getHealthModifier() == MobValueModifier.VALUE, cmd + "healthModifier VALUE", "action.value"),
                                    escape("field.health"), healthModifier, changeButton(cmd + "health "),
                                    getToggleField(entry.getDamageModifier() == MobValueModifier.DEFAULT, cmd + "damageModifier DEFAULT", localizer().getMessage("action.default")),
                                    getToggleField(entry.getDamageModifier() == MobValueModifier.MULTIPLY, cmd + "damageModifier MULTIPLY", "action.multiply"),
                                    getToggleField(entry.getDamageModifier() == MobValueModifier.VALUE, cmd + "damageModifier VALUE", "action.value"),
                                    escape("field.damage"), healthModifier, changeButton(cmd + "damage ")
                            );
                },
                "manageMob.title",
                "/bloodnight manageMob " + mobGroup.getKey() + " " + ArgumentUtils.escapeWorldName(world) + " page {page}");
        messageSender().sendMessage(sender, component, Replacement.create("TYPE", mobGroup.getKey()), Replacement.create("WORLD", world.getName()));
    }

    //group world mob field value

    @Override
    public @Nullable List<String> onTabComplete(@NotNull Player player, @NotNull String alias, @NotNull Arguments args) throws CommandException {
        // mobgroup
        if (args.size() == 1) {
            return Completion.complete(args.asString(0), SpecialMobRegistry.getMobGroups().keySet(), Class::getSimpleName);
        }
        // world
        if (args.size() == 2) {
            return Completion.complete(args.asString(1), configuration.getWorldSettings().keySet());
        }
        // mob
        if (args.size() == 3) {
            Optional<MobGroup> mobGroup = SpecialMobRegistry.getMobGroup(args.asString(0));
            if (!mobGroup.isPresent()) {
                return Collections.singletonList(localizer().getMessage("error.invalidMobGroup"));
            }
            return Completion.complete(args.asString(2), mobGroup.get().getFactories(), MobFactory::getMobName);
        }
        // field
        String scmd = args.asString(3);
        if (args.size() == 4) {
            return Completion.complete(scmd, "state", "overrideDefault", "displayName",
                    "dropAmount", "health", "damage", "healthModifier", "damageModifier", "drops");
        }

        String val = args.asString(4);
        if (args.size() == 5) {
            if (Completion.isCommand(scmd, "state", "overrideDefault")) {
                return Completion.completeBoolean(val);
            }

            if (Completion.isCommand(scmd, "dropAmount")) {
                return Completion.completeInt(val, 1, 100);
            }

            if (Completion.isCommand(scmd, "health", "damage")) {
                return Completion.completeInt(val, 1, 500);
            }

            if (Completion.isCommand(scmd, "healthModifier", "damageModifier")) {
                return Completion.complete(val, MobValueModifier.class);
            }

            if (Completion.isCommand(scmd, "drops")) {
                return Completion.complete(val, "changeContent", "changeWeight", "clear");
            }
        }

        if (Completion.isCommand(scmd, "displayName")) {
            return Completion.completeFreeInput(args.join(4),
                    16, localizer().getMessage("field.displayName"));
        }

        return Collections.emptyList();
    }
}
