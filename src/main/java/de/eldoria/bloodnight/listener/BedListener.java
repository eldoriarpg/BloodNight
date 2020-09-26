package de.eldoria.bloodnight.listener;

import de.eldoria.bloodnight.config.Configuration;
import de.eldoria.bloodnight.config.NightSettings;
import de.eldoria.eldoutilities.localization.Localizer;
import de.eldoria.eldoutilities.messages.MessageSender;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;

public class BedListener implements Listener {
    private final Configuration configuration;
    private final NightManager nightManager;
    private final Localizer localizer;
    private final MessageSender messageSender;

    public BedListener(Configuration configuration, NightManager nightManager, Localizer localizer, MessageSender messageSender) {
        this.configuration = configuration;
        this.nightManager = nightManager;
        this.localizer = localizer;
        this.messageSender = messageSender;
    }

    public void onBedEnter(PlayerBedEnterEvent event) {
        if (!nightManager.isBloodNightActive(event.getPlayer().getWorld())) return;
        NightSettings nightSettings = configuration.getWorldSettings(event.getPlayer().getWorld()).getNightSettings();
        if (nightSettings.isSkippable()) return;
        messageSender.sendMessage(event.getPlayer(), localizer.getMessage("notify.youCantSleep"));
        event.setCancelled(true);
    }
}
