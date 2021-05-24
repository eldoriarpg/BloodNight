package de.eldoria.bloodnight.bloodmob.node.context;

import org.bukkit.entity.Player;

public interface IPlayerContext extends ILivingEntityContext{
    static IPlayerContext of(Player player) {
        return () -> player;
    }

    @Override
    Player getEntity();
}
