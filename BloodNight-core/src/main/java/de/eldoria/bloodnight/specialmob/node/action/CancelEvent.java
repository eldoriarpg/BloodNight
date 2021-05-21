package de.eldoria.bloodnight.specialmob.node.action;

import de.eldoria.bloodnight.specialmob.ISpecialMob;
import de.eldoria.bloodnight.specialmob.node.Node;
import de.eldoria.bloodnight.specialmob.node.context.ContextContainer;
import de.eldoria.bloodnight.specialmob.node.context.ICancelableContext;
import de.eldoria.bloodnight.specialmob.node.predicate.PredicateNode;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class CancelEvent implements Node {
    PredicateNode predicate;

    @Override
    public void handle(ISpecialMob mob, ContextContainer context) {
        if (context instanceof ICancelableContext) {
            ((ICancelableContext) context).getCancelable().setCancelled(predicate.test(mob, context));
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
