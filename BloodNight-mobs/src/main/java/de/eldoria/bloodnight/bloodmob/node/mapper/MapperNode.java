package de.eldoria.bloodnight.bloodmob.node.mapper;

import de.eldoria.bloodnight.bloodmob.IBloodMob;
import de.eldoria.bloodnight.bloodmob.node.Node;
import de.eldoria.bloodnight.bloodmob.node.NodeHolder;
import de.eldoria.bloodnight.bloodmob.node.contextcontainer.ContextContainer;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Instance to map context to another
 */
@NoArgsConstructor
public abstract class MapperNode extends NodeHolder {
    public MapperNode(Node node) {
        super(node);
    }

    public MapperNode(Map<String, Object> objectMap) {
        super(objectMap);
    }

    @Override
    public void handle(IBloodMob mob, ContextContainer context) {
        map(context);
        if (nextNode() != null) nextNode().handle(mob, context);
    }

    /**
     * Change the provided context to your preference.
     *
     * @param context context
     */
    public abstract void map(ContextContainer context);
}
