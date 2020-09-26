package de.eldoria.bloodnight.listener;

import de.eldoria.bloodnight.events.BloodNightBeginEvent;
import de.eldoria.bloodnight.events.BloodNightEndEvent;
import de.eldoria.eldoutilities.localization.Localizer;
import de.eldoria.eldoutilities.localization.Replacement;
import de.eldoria.eldoutilities.messages.MessageSender;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class MessageListener implements Listener {
    private final Localizer localizer;
    private final NightManager nightManager;
    private final MessageSender messageSender;

    public MessageListener(Localizer localizer, NightManager nightManager, MessageSender messageSender) {
        this.localizer = localizer;
        this.nightManager = nightManager;
        this.messageSender = messageSender;
    }

    @EventHandler
    public void onBloodNightEnd(BloodNightEndEvent event) {
        String message = localizer.getMessage("notify.nightEnd",
                Replacement.create("WORLD", event.getWorld().getName()).addFormatting('6'));
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            messageSender.sendMessage(player, message);
        }
    }

    @EventHandler
    public void onBloodNightStart(BloodNightBeginEvent event) {
        String message = localizer.getMessage("notify.nightStart",
                Replacement.create("WORLD", event.getWorld().getName()).addFormatting('6'));
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            messageSender.sendMessage(player, message);
        }
    }

    @EventHandler
    public void onPlayerWorldChange(PlayerChangedWorldEvent event) {
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
        if (nightManager.isBloodNightActive(event.getPlayer().getWorld())) {
            messageSender.sendMessage(event.getPlayer(), localizer.getMessage("notify.bloodNightJoined"));
        }
    }
}