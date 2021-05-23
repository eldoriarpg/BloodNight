package de.eldoria.bloodnight.bloodmob.node;

import de.eldoria.eldoutilities.serialization.SerializationUtil;
import de.eldoria.eldoutilities.serialization.TypeResolvingMap;
import lombok.NoArgsConstructor;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Set;

@NoArgsConstructor
public abstract class NodeHolder implements Node, ConfigurationSerializable {
    private Node nextNode;

    public NodeHolder(Node nextNode) {
        this.nextNode = nextNode;
    }

    public NodeHolder(Map<String, Object> objectMap) {
        TypeResolvingMap map = SerializationUtil.mapOf(objectMap);
        nextNode = map.getValue("node");
    }

    @Override
    @NotNull
    public Map<String, Object> serialize() {
        return SerializationUtil.newBuilder()
                .add("node", nextNode)
                .build();
    }

    public Node node() {
        return nextNode;
    }

    @Override
    public Set<Class<?>> getClasses(Set<Class<?>> set) {
        Node.super.getClasses(set);
        return nextNode.getClasses(set);
    }
}
