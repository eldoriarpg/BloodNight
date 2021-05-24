package de.eldoria.bloodnight.bloodmob.node;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.eldoria.bloodnight.bloodmob.IBloodMob;
import de.eldoria.bloodnight.bloodmob.node.contextcontainer.ContextContainer;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.Set;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "clazz")
public interface Node extends ConfigurationSerializable, ClassesProvider {
    void handle(IBloodMob mob, ContextContainer context);

    default ContextContainer getTransformedOutput(ContextContainer context) {
        return context;
    }

    @Override
    default Set<Class<?>> getClasses(Set<Class<?>> set) {
        set.add(getClass());
        return set;
    }

    default void removeLast() {
    }

    default boolean isLast() {
        return true;
    }

    default boolean addNode(Node node) {
        return false;
    }

    default Node getLast() {
        return this;
    }
}