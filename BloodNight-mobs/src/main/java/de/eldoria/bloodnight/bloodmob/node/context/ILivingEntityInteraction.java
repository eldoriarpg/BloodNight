package de.eldoria.bloodnight.bloodmob.node.context;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public interface ILivingEntityInteraction extends IEntityInteraction {
    static ILivingEntityInteraction of(LivingEntity actor, LivingEntity target) {
        return new ILivingEntityInteraction() {
            @Override
            public LivingEntity getActor() {
                return actor;
            }

            @Override
            public LivingEntity getTarget() {
                return target;
            }
        };
    }

    static ILivingEntityInteraction of(Entity actor, Entity target) {
        return of((LivingEntity) actor, (LivingEntity) target);
    }

    @Override
    LivingEntity getActor();

    @Override
    LivingEntity getTarget();
}
