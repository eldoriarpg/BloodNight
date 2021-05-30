package de.eldoria.bloodnight.bloodmob.node.filter;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.eldoria.bloodnight.bloodmob.IBloodMob;
import de.eldoria.bloodnight.bloodmob.node.Node;
import de.eldoria.bloodnight.bloodmob.node.NodeHolder;
import de.eldoria.bloodnight.bloodmob.node.contextcontainer.ContextContainer;
import lombok.NoArgsConstructor;

import java.util.Map;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "clazz")
public abstract class FilterNode extends NodeHolder {
    public FilterNode(Node node) {
        super(node);
    }

    public FilterNode(Map<String, Object> objectMap) {
        super(objectMap);
    }

    public FilterNode() {
    }

    public abstract boolean check(IBloodMob mob, ContextContainer context);

    @Override
    public void handle(IBloodMob mob, ContextContainer context) {
        if (check(mob, context) && nextNode() != null) nextNode().handle(mob, context);
    }
}
