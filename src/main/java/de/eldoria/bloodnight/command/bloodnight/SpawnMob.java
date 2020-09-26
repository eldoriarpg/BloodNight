package de.eldoria.bloodnight.command.bloodnight;

import de.eldoria.bloodnight.core.mobfactory.MobFactory;
import de.eldoria.bloodnight.core.mobfactory.SpecialMobRegistry;
import de.eldoria.bloodnight.listener.MobModifier;
import de.eldoria.bloodnight.listener.NightManager;
import de.eldoria.bloodnight.specialmobs.SpecialMobUtil;
import de.eldoria.eldoutilities.localization.Localizer;
import de.eldoria.eldoutilities.messages.MessageSender;
import de.eldoria.eldoutilities.simplecommands.EldoCommand;
import de.eldoria.eldoutilities.utils.ArrayUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SpawnMob extends EldoCommand {
    private final NightManager nightManager;
    private final MobModifier mobModifier;

    public SpawnMob(Localizer localizer, MessageSender messageSender, NightManager nightManager, MobModifier mobModifier) {
        super(localizer, messageSender);
        this.nightManager = nightManager;
        this.mobModifier = mobModifier;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }

        if (args.length == 0) {
            messageSender().sendError(sender, "invalid syntax");
            return true;
        }

        Player player = (Player) sender;

        if (nightManager.isBloodNightActive(player.getWorld())) {
            Block targetBlock = player.getTargetBlock(null, 100);
            if (targetBlock.getType() == Material.AIR) {
                messageSender().sendError(player, "No Block in sight.");
                return true;
            }

            Optional<MobFactory> mobFactoryByName = SpecialMobRegistry.getMobFactoryByName(args[0]);

            if (!mobFactoryByName.isPresent()) {
                messageSender().sendError(player, "Invalid mob type");
                return true;
            }

            MobFactory mobFactory = mobFactoryByName.get();

            Entity entity = SpecialMobUtil.spawnAndTagEntity(targetBlock.getLocation().add(0, 1, 0), mobFactory.getEntityType());
            mobModifier.wrapMob(entity, mobFactory);
        } else {
            messageSender().sendError(player, "no blood night active");
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            String[] strings = SpecialMobRegistry.getRegisteredMobs().stream()
                    .map(MobFactory::getMobName)
                    .toArray(String[]::new);
            return ArrayUtil.startingWithInArray(args[0], strings)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
