package de.eldoria.bloodnight.listener;

import de.eldoria.bloodnight.config.Configuration;
import de.eldoria.eldoutilities.localization.Localizer;
import de.eldoria.eldoutilities.messages.MessageSender;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;

public class BedListener implements Listener {
    private final Configuration configuration;
    private final NightListener nightListener;
    private final Localizer localizer;
    private final MessageSender messageSender;

    public BedListener(Configuration configuration, NightListener nightListener, Localizer localizer, MessageSender messageSender) {
        this.configuration = configuration;
        this.nightListener = nightListener;
        this.localizer = localizer;
        this.messageSender = messageSender;
    }

    public void onBedEnter(PlayerBedEnterEvent event) {
        if (!nightListener.isBloodNightActive(event.getPlayer().getWorld())) return;
        if (configuration.getNightSettings().isSkippable()) return;
        messageSender.sendMessage(event.getPlayer(), localizer.getMessage("notify.youCantSleep"));
        event.setCancelled(true);
    }
}
