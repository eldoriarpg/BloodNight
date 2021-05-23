package de.eldoria.bloodnight.bloodmob.node;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.eldoria.bloodnight.bloodmob.IBloodMob;
import de.eldoria.bloodnight.bloodmob.node.contextcontainer.ContextContainer;
import de.eldoria.bloodnight.bloodmob.node.contextcontainer.ContextType;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.List;
import java.util.Set;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "clazz")
public interface Node extends ConfigurationSerializable, ClassesProvider {
    void handle(IBloodMob mob, ContextContainer context);

    default ContextType<?>[] requires() {
        return new ContextType[0];
    }

    default ContextContainer getTransformedOutput(ContextContainer context) {
        return context;
    }

    @Override
    default Set<Class<?>> getClasses(Set<Class<?>> set) {
        set.add(getClass());
        return set;
    }
}