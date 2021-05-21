package de.eldoria.bloodnight.specialmob.node.action;

import de.eldoria.bloodnight.specialmob.ISpecialMob;
import de.eldoria.bloodnight.specialmob.node.Node;
import de.eldoria.bloodnight.specialmob.node.context.ContextContainer;
import de.eldoria.bloodnight.specialmob.node.context.ILivingEntityContext;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class ApplyDamage implements Node {
    @Override
    public void handle(ISpecialMob mob, ContextContainer context) {
        if (context instanceof ILivingEntityContext) {
            AttributeInstance attribute = mob.getBase().getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
            ((ILivingEntityContext) context).getEntity().damage(attribute.getValue(), mob.getBase());
        }
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        return null;
    }
}
