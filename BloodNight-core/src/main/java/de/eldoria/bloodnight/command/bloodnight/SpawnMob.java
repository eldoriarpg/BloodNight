package de.eldoria.bloodnight.command.bloodnight;

import de.eldoria.bloodnight.core.manager.mobmanager.MobManager;
import de.eldoria.bloodnight.core.manager.nightmanager.NightManager;
import de.eldoria.bloodnight.core.mobfactory.MobFactory;
import de.eldoria.bloodnight.core.mobfactory.SpecialMobRegistry;
import de.eldoria.bloodnight.util.Permissions;
import de.eldoria.eldoutilities.commands.command.AdvancedCommand;
import de.eldoria.eldoutilities.commands.command.CommandMeta;
import de.eldoria.eldoutilities.commands.command.util.Arguments;
import de.eldoria.eldoutilities.commands.command.util.CommandAssertions;
import de.eldoria.eldoutilities.commands.exceptions.CommandException;
import de.eldoria.eldoutilities.commands.executor.IPlayerTabExecutor;
import de.eldoria.eldoutilities.utils.ArrayUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SpawnMob extends AdvancedCommand implements IPlayerTabExecutor {
    private final NightManager nightManager;
    private final MobManager mobManager;

    public SpawnMob(Plugin plugin, NightManager nightManager, MobManager mobManager) {
        super(plugin, CommandMeta.builder("spawnMob")
                .withPermission(Permissions.Admin.SPAWN_MOB)
                .addArgument("mobName", true)
                .build());
        this.nightManager = nightManager;
        this.mobManager = mobManager;
    }

    @Override
    public void onCommand(@NotNull Player sender, @NotNull String alias, @NotNull Arguments args) throws CommandException {
        if (nightManager.isBloodNightActive(sender.getWorld())) {
            Block targetBlock = sender.getTargetBlock(null, 100);
            CommandAssertions.isTrue(targetBlock.getType() != Material.AIR, "No Block in sight.");

            Optional<MobFactory> mobFactoryByName = SpecialMobRegistry.getMobFactoryByName(args.asString(0));

            CommandAssertions.isTrue(mobFactoryByName.isPresent(), "Invalid mob type");

            MobFactory mobFactory = mobFactoryByName.get();

            Entity entity = targetBlock.getWorld().spawnEntity(targetBlock.getLocation().add(0, 1, 0), mobFactory.getEntityType());
            mobManager.getSpecialMobManager().wrapMob(entity, mobFactory);
        } else {
            messageSender().sendError(sender, "No blood night active");
        }
    }


    @Override
    public @Nullable List<String> onTabComplete(@NotNull Player player, @NotNull String alias, @NotNull Arguments args) throws CommandException {
        if (args.sizeIs(1)) {
            String[] strings = SpecialMobRegistry.getRegisteredMobs().stream()
                    .map(MobFactory::getMobName)
                    .toArray(String[]::new);
            return ArrayUtil.startingWithInArray(args.asString(0), strings)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

}
