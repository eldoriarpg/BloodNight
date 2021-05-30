package de.eldoria.bloodnight.bloodmob.node;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.eldoria.bloodnight.bloodmob.node.contextcontainer.ContextContainer;
import de.eldoria.bloodnight.bloodmob.serialization.annotation.Property;
import de.eldoria.eldoutilities.serialization.SerializationUtil;
import de.eldoria.eldoutilities.serialization.TypeResolvingMap;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "clazz")
public abstract class NodeHolder implements Node, ConfigurationSerializable {
    @Nullable
    @Property(name = "", descr = "")
    private Node nextNode;

    public NodeHolder(Node nextNode) {
        this.nextNode = nextNode;
    }

    public NodeHolder() {
    }

    @Nullable
    public Node nextNode() {
        return nextNode;
    }

    public void nextNode(Node nextNode) {
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

    @Override
    public ContextContainer getTransformedOutput(ContextContainer context) {
        return nextNode == null ? context : nextNode.getTransformedOutput(context);
    }

    @Override
    public Set<Class<?>> getClasses(Set<Class<?>> set) {
        Node.super.getClasses(set);
        return nextNode != null ? nextNode.getClasses(set) : set;
    }

    @Override
    public void removeLast() {
        if (nextNode != null && nextNode.isLast()) {
            nextNode = null;
        }
    }

    @Override
    public boolean isLast() {
        return nextNode == null;
    }

    @Override
    public boolean addNode(Node node) {
        if (nextNode != null) {
            return nextNode.addNode(node);
        }
        nextNode = node;
        return true;
    }

    @Override
    public Node getLast() {
        if (isLast()) {
            return this;
        }
        return nextNode.getLast();
    }
}
