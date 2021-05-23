package de.eldoria.bloodnight.bloodmob.nodeimpl.filter;

import de.eldoria.bloodnight.bloodmob.IBloodMob;
import de.eldoria.bloodnight.bloodmob.node.Node;
import de.eldoria.bloodnight.bloodmob.node.contextcontainer.ContextContainer;
import de.eldoria.bloodnight.bloodmob.node.filter.FilterNode;
import de.eldoria.bloodnight.bloodmob.serialization.annotation.NumberProperty;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.Map;

@NoArgsConstructor
public class LastDamage extends FilterNode {
    @NumberProperty(name = "", descr = "")
    private int after;

    public LastDamage(Node node, int after) {
        super(node);
        this.after = after;
    }

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
    public boolean check(IBloodMob mob, ContextContainer context) {
        return mob.lastDamage().isBefore(Instant.now().minusSeconds(after));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LastDamage that = (LastDamage) o;

        return after == that.after;
    }

    @Override
    public int hashCode() {
        return after;
    }
}
