package de.eldoria.bloodnight.specialmob.node.mapper;

import de.eldoria.bloodnight.specialmob.ISpecialMob;
import de.eldoria.bloodnight.specialmob.node.Node;
import de.eldoria.bloodnight.specialmob.node.NodeHolder;
import de.eldoria.bloodnight.specialmob.node.context.ContextContainer;

import java.util.Map;

/**
 * Instance to map context to another
 */
public abstract class MapperNode extends NodeHolder {
    public MapperNode(Node node) {
        super(node);
    }

    public MapperNode(Map<String, Object> objectMap) {
        super(objectMap);
    }

    @Override
    public void handle(ISpecialMob mob, ContextContainer context) {
        map(context);
        node().handle(mob, context);
    }

    /**
     * Change the provided context to your preference.
     *
     * @param context context
     */
    public abstract void map(ContextContainer context);
}
