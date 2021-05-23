package de.eldoria.bloodnight.bloodmob.node.context;

import org.bukkit.entity.Player;

public interface IPlayerContext extends ILivingEntityContext{
    @Override
    Player getEntity();
}
