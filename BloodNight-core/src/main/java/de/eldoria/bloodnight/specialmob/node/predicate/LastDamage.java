package de.eldoria.bloodnight.specialmob.node.predicate;

import de.eldoria.bloodnight.specialmob.ISpecialMob;
import de.eldoria.bloodnight.specialmob.node.Node;
import de.eldoria.bloodnight.specialmob.node.context.IActionContext;
import de.eldoria.bloodnight.specialmob.node.filter.FilterNode;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.Map;

public class LastDamage extends FilterNode {
    private int after;

    public LastDamage(Node node) {
        super(node);
    }

    public LastDamage(Map<String, Object> objectMap) {
        super(objectMap);
    }

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
