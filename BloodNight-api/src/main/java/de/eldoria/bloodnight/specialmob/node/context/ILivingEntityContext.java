package de.eldoria.bloodnight.specialmob.node.context;

import org.bukkit.entity.LivingEntity;

public interface ILivingEntityContext extends IEntityContext {
    LivingEntity getEntity();
}
