package de.eldoria.bloodnight.bloodmob.nodeimpl.filter;

import de.eldoria.bloodnight.bloodmob.IBloodMob;
import de.eldoria.bloodnight.bloodmob.node.Node;
import de.eldoria.bloodnight.bloodmob.node.contextcontainer.ContextContainer;
import de.eldoria.bloodnight.bloodmob.node.filter.FilterNode;
import de.eldoria.bloodnight.bloodmob.node.predicate.PredicateNode;
import de.eldoria.bloodnight.bloodmob.serialization.annotation.Property;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

@NoArgsConstructor
public class PredicateFilter extends FilterNode {
    @Property(name = "", descr = "")
    boolean invert = false;
    @Property(name = "", descr = "")
    private PredicateNode predicate;

    public PredicateFilter(Node node, boolean invert, PredicateNode predicate) {
        super(node);
        this.invert = invert;
        this.predicate = predicate;
    }

    public PredicateFilter(Node node) {
        super(node);
    }

    public PredicateFilter(Map<String, Object> objectMap) {
        super(objectMap);
    }

    @Override
    public boolean check(IBloodMob mob, ContextContainer context) {
        return invert != predicate.test(mob, context);
    }

    @Override
    public Set<Class<?>> getClasses(Set<Class<?>> set) {
        set.add(predicate.getClass());
        return super.getClasses(set);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PredicateFilter that = (PredicateFilter) o;

        if (invert != that.invert) return false;
        return Objects.equals(predicate, that.predicate);
    }

    @Override
    public int hashCode() {
        int result = (invert ? 1 : 0);
        result = 31 * result + (predicate != null ? predicate.hashCode() : 0);
        return result;
    }
}
