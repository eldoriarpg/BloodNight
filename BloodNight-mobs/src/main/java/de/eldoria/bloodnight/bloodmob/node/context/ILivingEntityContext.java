package de.eldoria.bloodnight.bloodmob.node.context;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public interface ILivingEntityContext extends IEntityContext {
    static ILivingEntityContext of(LivingEntity entity) {
        return () -> entity;
    }

    static ILivingEntityContext of(Entity entity) {
        return () -> (LivingEntity) entity;
    }

    LivingEntity getEntity();
}
