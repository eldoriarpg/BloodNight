package de.eldoria.bloodnight.core.manager.nightmanager;

import de.eldoria.bloodnight.config.Configuration;
import de.eldoria.bloodnight.core.BloodNight;
import de.eldoria.bloodnight.util.Permissions;
import de.eldoria.eldoutilities.messages.MessageSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CommandBlocker implements Listener {
    private final NightManager nightManager;
    private final Configuration configuration;
    private final MessageSender sender;

    public CommandBlocker(NightManager nightManager, Configuration configuration) {
        this.nightManager = nightManager;
        this.configuration = configuration;
        this.sender = MessageSender.getPluginMessageSender(BloodNight.class);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCommandPreprocessEvent(PlayerCommandPreprocessEvent event) {
        if (!nightManager.isBloodNightActive(event.getPlayer().getWorld())) return;

        if (isBlocked(event.getMessage()) && event.getPlayer().hasPermission(Permissions.Bypass.COMMAND_BLOCK)) {
            sender.sendError(event.getPlayer(), "error.commandBlocked");
            event.setCancelled(true);
        }
    }

    private boolean isBlocked(String command) {
        String lowerCommand = command.toLowerCase();
        for (String blockedCommand : configuration.getGeneralSettings().getBlockedCommands()) {
            if (lowerCommand.startsWith(blockedCommand.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}
