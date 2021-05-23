package de.eldoria.bloodnight.bloodmob.node.effect;

import de.eldoria.bloodnight.bloodmob.IBloodMob;
import de.eldoria.bloodnight.bloodmob.node.Node;
import de.eldoria.bloodnight.bloodmob.node.contextcontainer.ContextContainer;
import org.bukkit.entity.LivingEntity;

public interface EffectNode extends Node {

    @Override
    default void handle(IBloodMob mob, ContextContainer context) {
        apply(mob.getBase());
    }

    void apply(LivingEntity entity);
}
