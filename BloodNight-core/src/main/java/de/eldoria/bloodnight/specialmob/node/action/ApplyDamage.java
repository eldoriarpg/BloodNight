package de.eldoria.bloodnight.specialmob.node.action;

import de.eldoria.bloodnight.specialmob.ISpecialMob;
import de.eldoria.bloodnight.specialmob.node.Node;
import de.eldoria.bloodnight.specialmob.node.context.IActionContext;
import de.eldoria.bloodnight.specialmob.node.context.ILivingEntityContext;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class ApplyDamage implements Node {
    @Override
    public void handle(ISpecialMob mob, IActionContext context) {
        if (context instanceof ILivingEntityContext) {
            AttributeInstance attribute = mob.getBase().getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
            ((ILivingEntityContext) context).getEntity().damage(attribute.getValue(), mob.getBase());
        }
    }

    /**
     * Creates a Map representation of this class.
     * <p>
     * This class must provide a method to restore this class, as defined in
     * the {@link ConfigurationSerializable} interface javadocs.
     *
     * @return Map containing the current state of this class
     */
    @NotNull
    @Override
    public Map<String, Object> serialize() {
        return null;
    }
}
