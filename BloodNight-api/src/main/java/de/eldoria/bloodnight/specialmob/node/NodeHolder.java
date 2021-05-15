package de.eldoria.bloodnight.specialmob.node;

import de.eldoria.eldoutilities.serialization.SerializationUtil;
import de.eldoria.eldoutilities.serialization.TypeResolvingMap;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public abstract class NodeHolder implements Node, ConfigurationSerializable {
    private Node node;

    public NodeHolder(Node node) {
        this.node = node;
    }

    public NodeHolder(Map<String, Object> objectMap) {
        TypeResolvingMap map = SerializationUtil.mapOf(objectMap);
        node = map.getValue("node");
    }

    @Override
    @NotNull
    public Map<String, Object> serialize() {
        return SerializationUtil.newBuilder()
                .add("node", node)
                .build();
    }

    public Node node() {
        return node;
    }
}
