package de.eldoria.bloodnight.specialmob.node.predicate;

import de.eldoria.bloodnight.specialmob.ISpecialMob;
import de.eldoria.bloodnight.specialmob.node.Node;
import de.eldoria.bloodnight.specialmob.node.context.IActionContext;
import de.eldoria.bloodnight.specialmob.node.filter.FilterNode;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.Map;

public class LastDamage extends FilterNode {
    int after;

    public LastDamage(Node node) {
        super(node);
    }

    public LastDamage(Map<String, Object> objectMap) {
        super(objectMap);
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

    @Override
    public boolean check(ISpecialMob mob, IActionContext context) {
        return mob.lastDamage().isBefore(Instant.now().minusSeconds(after));
    }
}
