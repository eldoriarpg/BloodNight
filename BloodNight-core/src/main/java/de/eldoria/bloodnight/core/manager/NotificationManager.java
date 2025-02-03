package de.eldoria.bloodnight.core.manager;

import de.eldoria.bloodnight.config.Configuration;
import de.eldoria.bloodnight.core.BloodNight;
import de.eldoria.bloodnight.core.manager.nightmanager.NightManager;
import de.eldoria.bloodnight.events.BloodNightBeginEvent;
import de.eldoria.bloodnight.events.BloodNightEndEvent;
import de.eldoria.bloodnight.hooks.HookService;
import de.eldoria.eldoutilities.localization.ILocalizer;
import de.eldoria.eldoutilities.messages.MessageSender;
import de.eldoria.eldoutilities.messages.Replacement;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Collection;

import static de.eldoria.eldoutilities.localization.ILocalizer.escape;
import static java.time.Duration.ofSeconds;
import static net.kyori.adventure.title.Title.Times.times;

public class NotificationManager implements Listener {
    private final ILocalizer localizer;
    private final NightManager nightManager;
    private final MessageSender messageSender;
    private final Configuration configuration;
    private final HookService hookService;

    public NotificationManager(Configuration configuration, NightManager nightManager, HookService hookService) {
        this.configuration = configuration;
        this.hookService = hookService;
        this.localizer = ILocalizer.getPluginLocalizer(BloodNight.class);
        this.nightManager = nightManager;
        this.messageSender = MessageSender.getPluginMessageSender(BloodNight.class);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBloodNightEnd(BloodNightEndEvent event) {
        dispatchBroadcast(event.getWorld(),
                escape("notify.nightEnd"),
                Replacement.create("world", getAlias(event.getWorld()))
        );
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBloodNightStart(BloodNightBeginEvent event) {
        dispatchBroadcast(event.getWorld(),
                escape("notify.nightStart"),
                Replacement.create("world", getAlias(event.getWorld()))
        );
    }

    private void dispatchBroadcast(World world, String message, TagResolver... tagResolver) {
        Collection<? extends Player> players;
        switch (configuration.getGeneralSettings().broadcastLevel()) {
            case SERVER:
                players = Bukkit.getOnlinePlayers();
                break;
            case WORLD:
                players = world.getPlayers();
                break;
            case NONE:
                return;
            default:
                throw new IllegalStateException("Unexpected value: " + configuration.getGeneralSettings().broadcastLevel());
        }


        for (Player player : players) {
            sendBroadcast(player, message, tagResolver);
        }
    }

    private void sendBroadcast(Player player, String message, TagResolver... tagResolver) {
        switch (configuration.getGeneralSettings().broadcastMethod()) {
            case CHAT -> messageSender.sendMessage(player, message, tagResolver);
            case TITLE ->
                    messageSender.sendTitle(player, message, "", times(ofSeconds(1), ofSeconds(5), ofSeconds(1)), tagResolver);
            case SUBTITLE ->
                    messageSender.sendTitle(player, "", message, times(ofSeconds(1), ofSeconds(5), ofSeconds(1)), tagResolver);
        }
    }

    private void sendMessage(Player player, String message) {
        switch (configuration.getGeneralSettings().messageMethod()) {
            case CHAT -> messageSender.sendMessage(player, message);
            case TITLE -> messageSender.sendTitle(player, message, "", times(ofSeconds(1), ofSeconds(5), ofSeconds(1)));
            case SUBTITLE ->
                    messageSender.sendTitle(player, "", message, times(ofSeconds(1), ofSeconds(5), ofSeconds(1)));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerWorldChange(PlayerChangedWorldEvent event) {
        if (!configuration.getGeneralSettings().joinWorldWarning()) return;

        boolean origin = nightManager.isBloodNightActive(event.getFrom());
        boolean destination = nightManager.isBloodNightActive(event.getPlayer().getWorld());
        if (destination) {
            sendMessage(event.getPlayer(), localizer.getMessage("notify.bloodNightJoined"));
            return;
        }

        if (origin) {
            sendMessage(event.getPlayer(), localizer.getMessage("notify.bloodNightLeft"));
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!configuration.getGeneralSettings().joinWorldWarning()) return;

        if (nightManager.isBloodNightActive(event.getPlayer().getWorld())) {
            sendMessage(event.getPlayer(), localizer.getMessage("notify.bloodNightJoined"));
        }
    }

    public String getAlias(World world) {
        return hookService.getWorldManager().getAlias(world);
    }
}
