package de.eldoria.bloodnight.bloodmob.node.context;

import org.bukkit.entity.Entity;

public interface IEntityContext extends IContext {
    static IEntityContext of(Entity entity) {
        return () -> entity;
    }

    Entity getEntity();
}
