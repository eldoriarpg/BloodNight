package de.eldoria.bloodnight.command.bloodnight;

import de.eldoria.bloodnight.command.InventoryListener;
import de.eldoria.bloodnight.command.util.CommandUtil;
import de.eldoria.bloodnight.command.util.KyoriColors;
import de.eldoria.bloodnight.config.Configuration;
import de.eldoria.bloodnight.config.Drop;
import de.eldoria.bloodnight.config.MobSetting;
import de.eldoria.bloodnight.config.WorldSettings;
import de.eldoria.bloodnight.core.BloodNight;
import de.eldoria.eldoutilities.localization.Localizer;
import de.eldoria.eldoutilities.messages.MessageSender;
import de.eldoria.eldoutilities.simplecommands.EldoCommand;
import de.eldoria.eldoutilities.utils.ArgumentUtils;
import de.eldoria.eldoutilities.utils.ArrayUtil;
import de.eldoria.eldoutilities.utils.Parser;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Collectors;

public class ManageMob extends EldoCommand {
    public final Configuration configuration;
    private final InventoryListener inventoryListener;
    private final BukkitAudiences bukkitAudiences;

    public ManageMob(Localizer localizer, MessageSender messageSender, Configuration configuration, InventoryListener inventoryListener) {
        super(localizer, messageSender);
        this.configuration = configuration;
        this.inventoryListener = inventoryListener;
        bukkitAudiences = BukkitAudiences.create(BloodNight.getInstance());
    }

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

        if (worldSettings.isEnabled()) {
            messageSender().sendError(player, "Blood Night is not active in this world.");
            return true;
        }

        if (args.length < 2) {
            sendMobListPage(world, sender, 0);
            return true;
        }

        // world field value [page]
        if (argumentsInvalid(sender, args, 4, "some syntax")) {
            return true;
        }

        String mobString = args[1];
        String field = args[2];
        String value = args[3];

        OptionalInt optPage = ArgumentUtils.getOptionalParameter(args, 4, OptionalInt.empty(), Parser::parseInt);

        if (!"none".equalsIgnoreCase(field)) {
            Optional<MobSetting> mobByName = worldSettings.getMobSettings().getMobByName(args[1]);
            if (!mobByName.isPresent()) {
                messageSender().sendError(sender, "Invalid mob");
                return true;
            }

            MobSetting mob = mobByName.get();

            if ("state".equalsIgnoreCase(field)) {

                Optional<Boolean> aBoolean = Parser.parseBoolean(value);
                if (!aBoolean.isPresent()) {
                    messageSender().sendError(sender, "invalid boolean");
                    return true;
                }
                mob.setActive(aBoolean.get());
            }
            if ("dropAmount".equalsIgnoreCase(field)) {
                OptionalInt num = Parser.parseInt(value);
                if (!num.isPresent()) {
                    messageSender().sendError(sender, "invalid number");
                    return true;
                }
                mob.setDropAmount(num.getAsInt());
            }
            if ("overrideDefault".equalsIgnoreCase(field)) {
                Optional<Boolean> aBoolean = Parser.parseBoolean(value);
                if (!aBoolean.isPresent()) {
                    messageSender().sendError(sender, "invalid boolean");
                    return true;
                }
                mob.setOverrideDefaultDrops(aBoolean.get());
            }
            if ("drops".equalsIgnoreCase(field)) {
                if (!ArrayUtil.arrayContains(new String[] {"changeContent", "changeWeight", "clear"}, value)) {
                    messageSender().sendError(sender, "invalid value");
                    return true;
                }

                World finalWorld = world;
                if ("changeContent".equalsIgnoreCase(value)) {
                    Inventory inv = Bukkit.createInventory(player, 54, "Drops");
                    inv.setContents(mob.getDrops().stream().map(Drop::getItem).toArray(ItemStack[]::new));
                    player.openInventory(inv);
                    inventoryListener.registerModification(player, new InventoryListener.InventoryActionHandler() {
                        @Override
                        public void onInventoryClose(InventoryCloseEvent event) {
                            List<Drop> collect = Arrays.stream(event.getInventory().getContents())
                                    .filter(Objects::nonNull)
                                    .map(Drop::fromItemStack)
                                    .collect(Collectors.toList());
                            mob.setDrops(collect);
                            optPage.ifPresent(i -> sendMobListPage(finalWorld, sender, i));
                        }

                        @Override
                        public void onInventoryClick(InventoryClickEvent event) {
                        }
                    });
                    return true;
                }

                if ("changeWeight".equalsIgnoreCase(value)) {
                    List<ItemStack> stacks = mob.getDrops().stream().map(Drop::getItemWithLoreWeight).collect(Collectors.toList());
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
                            mob.setDrops(collect);
                            optPage.ifPresent(i -> sendMobListPage(finalWorld, sender, i));
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
                    mob.setDrops(new ArrayList<>());
                    optPage.ifPresent(i -> sendMobListPage(finalWorld, sender, i));
                    return true;
                }
            }
        }
        World finalWorld1 = world;
        optPage.ifPresent(i -> sendMobListPage(finalWorld1, sender, i));
        configuration.safeConfig();
        return true;
    }

    private void sendMobListPage(World world, CommandSender sender, int page) {
        TextComponent component = CommandUtil.getPage(
                new ArrayList<>(configuration.getWorldSettings(world).getMobSettings().getMobTypes()),
                page,
                4, 4,
                entry -> {
                    String cmd = "/bloodnight manageMob " + world.getName() + " " + entry.getMobName() + " ";
                    return TextComponent.builder()
                            // Mob name
                            .append(TextComponent.builder(entry.getMobName(), KyoriColors.GOLD)
                                    .decoration(TextDecoration.BOLD, true).build()).append(" ")
                            // Mob state
                            .append(CommandUtil.getBooleanField(entry.isActive(),
                                    cmd + "state {bool} " + page,
                                    "", "enabled", "disabled"))
                            .append(TextComponent.newline()).append("  ")
                            // Drop amount
                            .append(TextComponent.builder("Drop Amount: ", KyoriColors.AQUA))
                            .append(TextComponent.builder(
                                    entry.getDropAmount() == -1 ? "default " : entry.getDropAmount() + "x ", KyoriColors.GOLD))
                            .append(TextComponent.builder("[change]", KyoriColors.GREEN)
                                    .clickEvent(ClickEvent.suggestCommand(cmd + "dropAmount ")))
                            .append(TextComponent.newline()).append("  ")
                            // drops
                            .append(TextComponent.builder("Drops: ", KyoriColors.AQUA))
                            .append(TextComponent.builder(entry.getDrops().size() + " Drops ", KyoriColors.GOLD))
                            .append(TextComponent.builder("[content] ", KyoriColors.GREEN)
                                    .clickEvent(ClickEvent.runCommand(cmd + "drops changeContent")))
                            .append(TextComponent.builder("[weight] ", KyoriColors.GOLD)
                                    .clickEvent(ClickEvent.runCommand(cmd + "drops changeWeight")))
                            .append(TextComponent.builder("[clear]", KyoriColors.RED)
                                    .clickEvent(ClickEvent.runCommand(cmd + "drops clear")))
                            // override drops
                            .append(TextComponent.newline()).append("  ")
                            .append(CommandUtil.getBooleanField(entry.isOverrideDefaultDrops(),
                                    cmd + "overrideDefault {bool} " + page,
                                    "Override Default Drops", "override", "combine"))
                            .build();
                },
                "Mob States",
                "/bloodNight manageMob " + world.getName() + " none none none {page}");

        bukkitAudiences.audience(sender).sendMessage(component);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            Set<String> strings = configuration.getWorldSettings().keySet();
            return ArrayUtil.startingWithInArray(args[0], strings.toArray(new String[0])).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
