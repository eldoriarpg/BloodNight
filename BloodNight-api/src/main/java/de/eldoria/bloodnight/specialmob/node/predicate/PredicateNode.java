package de.eldoria.bloodnight.specialmob.node.predicate;

import de.eldoria.bloodnight.specialmob.ISpecialMob;
import de.eldoria.bloodnight.specialmob.node.context.IActionContext;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

public interface PredicateNode extends ConfigurationSerializable {
    boolean test(ISpecialMob mob, IActionContext context);
}
