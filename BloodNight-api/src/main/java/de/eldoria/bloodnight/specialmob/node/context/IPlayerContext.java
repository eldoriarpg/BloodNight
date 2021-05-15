package de.eldoria.bloodnight.specialmob.node.context;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public interface IPlayerContext extends ILivingEntityContext{
    @Override
    Player getEntity();
}
