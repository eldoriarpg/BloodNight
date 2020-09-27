package de.eldoria.bloodnight.core.manager;

import de.eldoria.bloodnight.config.Configuration;
import de.eldoria.bloodnight.core.BloodNight;
import de.eldoria.bloodnight.core.events.BloodNightBeginEvent;
import de.eldoria.bloodnight.core.events.BloodNightEndEvent;
import de.eldoria.eldoutilities.localization.Localizer;
import de.eldoria.eldoutilities.localization.Replacement;
import de.eldoria.eldoutilities.messages.MessageSender;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Collection;

public class NotificationManager implements Listener {
    private final Localizer localizer;
    private final NightManager nightManager;
    private final MessageSender messageSender;
    private final Configuration configuration;

    public NotificationManager(Configuration configuration, NightManager nightManager) {
        this.configuration = configuration;
        this.localizer = BloodNight.localizer();
        this.nightManager = nightManager;
        this.messageSender = MessageSender.get(BloodNight.getInstance());
    }

    @EventHandler
    public void onBloodNightEnd(BloodNightEndEvent event) {
        sendBroadcast(event.getWorld(),
                localizer.getMessage("notify.nightEnd",
                        Replacement.create("WORLD", event.getWorld().getName()).addFormatting('6'))
        );
    }

    @EventHandler
    public void onBloodNightStart(BloodNightBeginEvent event) {
        sendBroadcast(event.getWorld(),
                localizer.getMessage("notify.nightStart",
                        Replacement.create("WORLD", event.getWorld().getName()).addFormatting('6'))
        );
    }

    private void sendBroadcast(World world, String message) {
        Collection<? extends Player> players;
        switch (configuration.getGeneralSettings().getBroadcastLevel()) {
            case SERVER:
                players = Bukkit.getOnlinePlayers();
                break;
            case WORLD:
                players = world.getPlayers();
                break;
            case NONE:
                return;
            default:
                throw new IllegalStateException("Unexpected value: " + configuration.getGeneralSettings().getBroadcastLevel());
        }

        for (Player player : players) {
            messageSender.sendMessage(player, message);
        }

    }

    @EventHandler
    public void onPlayerWorldChange(PlayerChangedWorldEvent event) {
        if (!configuration.getGeneralSettings().isJoinWorldWarning()) return;

        if (nightManager.isBloodNightActive(event.getFrom())) {
            if (nightManager.isBloodNightActive(event.getPlayer().getWorld())) {
                // no action needs to be taken
                return;
            }
            messageSender.sendMessage(event.getPlayer(), localizer.getMessage("notify.bloodNightJoined"));
        } else {
            if (nightManager.isBloodNightActive(event.getPlayer().getWorld())) {
                messageSender.sendMessage(event.getPlayer(), localizer.getMessage("notify.bloodNightLeft"));
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!configuration.getGeneralSettings().isJoinWorldWarning()) return;

        if (nightManager.isBloodNightActive(event.getPlayer().getWorld())) {
            messageSender.sendMessage(event.getPlayer(), localizer.getMessage("notify.bloodNightJoined"));
        }
    }
}