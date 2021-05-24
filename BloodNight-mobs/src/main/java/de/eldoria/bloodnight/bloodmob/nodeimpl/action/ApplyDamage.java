package de.eldoria.bloodnight.bloodmob.nodeimpl.action;

import de.eldoria.bloodnight.bloodmob.IBloodMob;
import de.eldoria.bloodnight.bloodmob.node.Node;
import de.eldoria.bloodnight.bloodmob.node.annotations.RequiresContext;
import de.eldoria.bloodnight.bloodmob.node.context.ILivingEntityContext;
import de.eldoria.bloodnight.bloodmob.node.contextcontainer.ContextContainer;
import de.eldoria.bloodnight.bloodmob.node.contextcontainer.ContextType;
import lombok.NoArgsConstructor;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@NoArgsConstructor
@RequiresContext(ILivingEntityContext.class)
public class ApplyDamage implements Node {

    @Override
    public void handle(IBloodMob mob, ContextContainer context) {
        context.get(ContextType.LIVING_ENTITY).ifPresent(entity -> {
            AttributeInstance attribute = mob.getBase().getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
            entity.getEntity().damage(attribute.getValue(), mob.getBase());
        });
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        return null;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        return obj.getClass() == getClass();
    }
}
