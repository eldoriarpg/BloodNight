package de.eldoria.bloodnight.bloodmob.node.predicate;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.eldoria.bloodnight.bloodmob.IBloodMob;
import de.eldoria.bloodnight.bloodmob.node.ClassesProvider;
import de.eldoria.bloodnight.bloodmob.node.contextcontainer.ContextContainer;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "clazz")
public interface PredicateNode extends ConfigurationSerializable, ClassesProvider {
    boolean test(IBloodMob mob, ContextContainer context);
}
