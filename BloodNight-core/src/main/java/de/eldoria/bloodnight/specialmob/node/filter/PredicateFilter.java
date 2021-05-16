package de.eldoria.bloodnight.specialmob.node.filter;

import de.eldoria.bloodnight.specialmob.ISpecialMob;
import de.eldoria.bloodnight.specialmob.node.Node;
import de.eldoria.bloodnight.specialmob.node.context.IActionContext;
import de.eldoria.bloodnight.specialmob.node.predicate.PredicateNode;

import java.util.Map;

public class PredicateFilter extends FilterNode {
    private PredicateNode predicate;
    boolean invert = false;

    public PredicateFilter(Node node) {
        super(node);
    }

    public PredicateFilter(Map<String, Object> objectMap) {
        super(objectMap);
    }

    @Override
    public boolean check(ISpecialMob mob, IActionContext context) {
        return invert != predicate.test(mob, context);
    }
}
