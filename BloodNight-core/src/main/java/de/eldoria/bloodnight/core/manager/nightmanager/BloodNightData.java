package de.eldoria.bloodnight.core.manager.nightmanager;

import de.eldoria.bloodnight.core.BloodNight;
import de.eldoria.bloodnight.config.worldsettings.sound.SoundSettings;
import de.eldoria.bloodnight.util.C;
import de.eldoria.eldoutilities.utils.ObjUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.boss.BossBar;
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
	private final BossBar bossBar;
	private final Queue<PlayerSound> playerSoundQueue = new PriorityQueue<>(PlayerSound::compareTo);
	private final Map<UUID, ConsistencyCache> playerConsistencyMap = new HashMap<>();


	public BloodNightData(World world, BossBar bossBar) {
		this.world = world;
		this.bossBar = bossBar;
	}

	public void addPlayer(Player player) {
		ObjUtil.nonNull(bossBar, b -> {
			b.addPlayer(player);
		});
		playerConsistencyMap.put(player.getUniqueId(), new ConsistencyCache(player));
		playerSoundQueue.add(new PlayerSound(player));
	}

	public void removePlayer(Player player) {
		ObjUtil.nonNull(Bukkit.getBossBar(C.getBossBarNamespace(world)), b -> {
			b.removePlayer(player);
		});
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
			PlayerSound poll = playerSoundQueue.poll();

			Player player = poll.getPlayer();
			Location location = player.getLocation();
			Vector direction = player.getEyeLocation().toVector();
			location.add(direction.multiply(-1));

			if (BloodNight.isDebug()) {
				BloodNight.logger().info("Played random sound to " + poll.player.getName());
			}

			settings.playRandomSound(player, location);

			poll.scheduleNext(settings.getWaitSeconds());

			playerSoundQueue.offer(poll);
		}
	}

	public void resolveAll() {
		NamespacedKey key = C.getBossBarNamespace(world);
		ObjUtil.nonNull(Bukkit.getBossBar(key), b -> {
			b.removeAll();
			if (!Bukkit.removeBossBar(key)) {
				if (BloodNight.isDebug()) {
					BloodNight.logger().warning("Could not remove boss bar " + key);
				}
			}
		});

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
