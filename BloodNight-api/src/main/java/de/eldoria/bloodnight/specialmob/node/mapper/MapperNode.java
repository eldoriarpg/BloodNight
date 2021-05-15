package de.eldoria.bloodnight.specialmob.node.mapper;

import de.eldoria.bloodnight.specialmob.ISpecialMob;
import de.eldoria.bloodnight.specialmob.node.Node;
import de.eldoria.bloodnight.specialmob.node.NodeHolder;
import de.eldoria.bloodnight.specialmob.node.context.IActionContext;

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
    public void handle(ISpecialMob mob, IActionContext context) {
        node().handle(mob, map(context));
    }

    public abstract IActionContext map(IActionContext context);
}
