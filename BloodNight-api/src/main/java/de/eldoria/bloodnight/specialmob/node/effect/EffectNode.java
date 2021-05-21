package de.eldoria.bloodnight.specialmob.node.effect;

import de.eldoria.bloodnight.specialmob.ISpecialMob;
import de.eldoria.bloodnight.specialmob.node.Node;
import de.eldoria.bloodnight.specialmob.node.context.ContextContainer;
import org.bukkit.entity.LivingEntity;

public interface EffectNode extends Node {

    @Override
    default void handle(ISpecialMob mob, ContextContainer context) {
        apply(mob.getBase());
    }

    void apply(LivingEntity entity);
}
