package de.eldoria.bloodnight.command.bloodnight.managedeathactions;

import de.eldoria.bloodnight.command.util.CommandUtil;
import de.eldoria.bloodnight.config.Configuration;
import de.eldoria.bloodnight.config.worldsettings.deathactions.MobDeathActions;
import de.eldoria.eldoutilities.commands.command.AdvancedCommand;
import de.eldoria.eldoutilities.commands.command.CommandMeta;
import de.eldoria.eldoutilities.commands.command.util.Arguments;
import de.eldoria.eldoutilities.commands.exceptions.CommandException;
import de.eldoria.eldoutilities.commands.executor.IPlayerTabExecutor;
import de.eldoria.eldoutilities.inventory.InventoryActionHandler;
import de.eldoria.eldoutilities.scheduling.DelayedActions;
import de.eldoria.eldoutilities.utils.ArgumentUtils;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import static de.eldoria.eldoutilities.localization.ILocalizer.escape;

public class ManageMonsterDeathActions extends AdvancedCommand {
    private final Configuration configuration;

    public ManageMonsterDeathActions(Plugin plugin, Configuration configuration) {
        super(plugin, CommandMeta.builder("monster")
                .addArgument("syntax.worldName", false)
                .withDefaultCommand(new Show(plugin, configuration))
                .withSubCommand(new Lightning(plugin, configuration))
                .withSubCommand(new Shockwave(plugin, configuration))
                .build());
        this.configuration = configuration;
    }

    static class Base extends AdvancedCommand {
        private final Configuration configuration;
        protected final InventoryActionHandler inventoryActions;
        protected final DelayedActions delayedActions;

        public Base(Plugin plugin, CommandMeta meta, Configuration configuration) {
            super(plugin, meta);
            this.configuration = configuration;
            this.inventoryActions = InventoryActionHandler.create(plugin);
            this.delayedActions = DelayedActions.start(plugin);
        }

        MobDeathActions actions(Player player, Arguments args) {
            World world = args.asWorld(0, player.getWorld());
            return configuration.getWorldSettings(world).getDeathActionSettings().getMobDeathActions();
        }

        void sendMobDeathActions(Player player, World world) {
            String cmd = "/bloodnight deathActions monster {command} " + ArgumentUtils.escapeWorldName(world.getName());
            String action = """
                    %s
                    <aqua>%s <click:run_command:''><green>[%s]</click>
                    <aqua>%s <click:run_command:''><green>[%s]</click>
                    """.stripIndent()
                    .formatted(CommandUtil.getHeader("manageDeathActions.monster.title"),
                            escape("field.lightningSettings"), cmd.replace("{command}", "lightning"), escape("action.change"),
                            escape("field.shockwaveSettings"), cmd.replace("{command}", "shockwave"), escape("action.change")
                    );

            messageSender().sendMessage(player, action);
        }
    }

    static class Show extends Base implements IPlayerTabExecutor {
        public Show(Plugin plugin, Configuration configuration) {
            super(plugin, CommandMeta.builder("show").hidden().build(), configuration);
        }

        @Override
        public void onCommand(@NotNull Player player, @NotNull String alias, @NotNull Arguments args) throws CommandException {
            World world = args.asWorld(0, player.getWorld());
            sendMobDeathActions(player, world);
        }
    }

    static class Lightning extends Base implements IPlayerTabExecutor {
        private final Configuration configuration;

        public Lightning(Plugin plugin, Configuration configuration) {
            super(plugin, CommandMeta.builder("lightning").hidden().build(), configuration);
            this.configuration = configuration;
        }

        @Override
        public void onCommand(@NotNull Player player, @NotNull String alias, @NotNull Arguments args) throws CommandException {
            MobDeathActions mobDeathActions = actions(player, args);
            DeathActionUtil.buildLightningUI(mobDeathActions.getLightningSettings(), player, inventoryActions,
                    configuration, localizer(), () -> sendMobDeathActions(player, args.asWorld(0)));
        }

    }

    static class Shockwave extends Base implements IPlayerTabExecutor {
        private final Configuration configuration;

        public Shockwave(Plugin plugin, Configuration configuration) {
            super(plugin, CommandMeta.builder("lightning").hidden().build(), configuration);
            this.configuration = configuration;
        }

        @Override
        public void onCommand(@NotNull Player player, @NotNull String alias, @NotNull Arguments args) throws CommandException {
            MobDeathActions mobDeathActions = actions(player, args);
            DeathActionUtil.buildShockwaveUI(mobDeathActions.getShockwaveSettings(), player, inventoryActions, delayedActions,
                    configuration, localizer(), () -> sendMobDeathActions(player, args.asWorld(0)));
        }

    }
}
