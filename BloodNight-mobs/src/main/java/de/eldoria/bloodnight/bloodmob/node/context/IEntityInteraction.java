package de.eldoria.bloodnight.bloodmob.node.context;

import org.bukkit.entity.Entity;

public interface IEntityInteraction extends IContext {
    static IEntityInteraction of(Entity actor, Entity target) {
        return new IEntityInteraction() {
            @Override
            public Entity getActor() {
                return actor;
            }

            @Override
            public Entity getTarget() {
                return target;
            }
        };
    }

    Entity getActor();

    Entity getTarget();
}
