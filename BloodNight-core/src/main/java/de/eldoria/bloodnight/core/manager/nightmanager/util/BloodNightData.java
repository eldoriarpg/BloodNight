package de.eldoria.bloodnight.core.manager.nightmanager.util;

import de.eldoria.bloodnight.config.worldsettings.sound.SoundSettings;
import de.eldoria.bloodnight.core.BloodNight;
import de.eldoria.bloodnight.util.C;
import de.eldoria.eldoutilities.messages.MessageSender;
import lombok.Getter;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.UUID;

@Getter
public
class BloodNightData {
    private final World world;
    private final net.kyori.adventure.bossbar.BossBar bossBar;
    private final Queue<PlayerSound> playerSoundQueue = new PriorityQueue<>(PlayerSound::compareTo);
    private final Map<UUID, ConsistencyCache> playerConsistencyMap = new HashMap<>();


    public BloodNightData(World world, BossBar bossBar) {
        this.world = world;
        this.bossBar = bossBar;
    }

    public void addPlayer(Player player) {
        MessageSender.getPluginMessageSender(BloodNight.class).sendBossBar(player, bossBar);
        playerConsistencyMap.put(player.getUniqueId(), new ConsistencyCache(player));
        playerSoundQueue.add(new PlayerSound(player));
    }

    public void removePlayer(Player player) {
        MessageSender.getPluginMessageSender(BloodNight.class).hideBossBar(player, bossBar);
        if (playerConsistencyMap.containsKey(player.getUniqueId())) {
            playerConsistencyMap.remove(player.getUniqueId()).revert(player);
        }
        playerSoundQueue.removeIf(e -> e.getPlayer().getUniqueId() == player.getUniqueId());
    }

    /**
     * Plays a random sound to the player in the queue.
     *
     * @param settings sound settings for the current world
     */
    public void playRandomSound(SoundSettings settings) {
        if (playerSoundQueue.isEmpty()) return;

        while (!playerSoundQueue.isEmpty() && playerSoundQueue.peek().isNext()) {
            PlayerSound sound = playerSoundQueue.poll();

            if (!sound.getPlayer().isOnline()) continue;

            Player player = sound.getPlayer();
            Location location = player.getLocation();
            Vector direction = player.getEyeLocation().toVector();
            location.add(direction.multiply(-1));

            BloodNight.logger().config("Played random sound to " + sound.player.getName());

            settings.playRandomSound(player, location);

            sound.scheduleNext(settings.getWaitSeconds());

            playerSoundQueue.offer(sound);
        }
    }

    public void resolveAll() {
        NamespacedKey key = C.getBossBarNamespace(world);
        world.getPlayers().forEach(p -> bossBar.removeViewer(MessageSender.getPluginMessageSender(BloodNight.class).asAudience(p)));
        playerConsistencyMap.forEach((uuid, cache) -> cache.revert(Bukkit.getOfflinePlayer(uuid)));
    }

    private static class PlayerSound implements Comparable<PlayerSound> {
        @Getter
        private final Player player;
        private Instant next;

        public PlayerSound(@NotNull Player player) {
            this.player = player;
            next = Instant.now().plus(10, ChronoUnit.SECONDS);
        }

        public boolean isNext() {
            return next.isBefore(Instant.now());
        }

        public void scheduleNext(int seconds) {
            next = next.plus(seconds, ChronoUnit.SECONDS);
        }


        @Override
        public int compareTo(@NotNull PlayerSound o) {
            if (o.next == next) return 0;
            return o.next.isAfter(next) ? 1 : -1;
        }
    }
}
