package de.eldoria.bloodnight.core.manager;

import lombok.Getter;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;

@Getter
class ConsistencyCache {
    private final int timeSinceRest;

    public ConsistencyCache(Player player) {
        timeSinceRest = player.getStatistic(Statistic.TIME_SINCE_REST);
    }

    public void revert(OfflinePlayer player) {
        player.setStatistic(Statistic.TIME_SINCE_REST, timeSinceRest);
    }
}
