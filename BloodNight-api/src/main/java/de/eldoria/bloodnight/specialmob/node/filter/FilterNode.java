package de.eldoria.bloodnight.specialmob.node.filter;

import de.eldoria.bloodnight.specialmob.ISpecialMob;
import de.eldoria.bloodnight.specialmob.node.Node;
import de.eldoria.bloodnight.specialmob.node.NodeHolder;
import de.eldoria.bloodnight.specialmob.node.context.ContextContainer;

import java.util.Map;

public abstract class FilterNode extends NodeHolder {
    public FilterNode(Node node) {
        super(node);
    }

    public FilterNode(Map<String, Object> objectMap) {
        super(objectMap);
    }

    public abstract boolean check(ISpecialMob mob, ContextContainer context);

    @Override
    public void handle(ISpecialMob mob, ContextContainer context) {
        if (check(mob, context)) node().handle(mob, context);
    }
}
