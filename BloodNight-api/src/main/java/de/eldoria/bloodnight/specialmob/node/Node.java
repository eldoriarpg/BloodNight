package de.eldoria.bloodnight.specialmob.node;

import de.eldoria.bloodnight.specialmob.ISpecialMob;
import de.eldoria.bloodnight.specialmob.node.context.IActionContext;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

public interface Node extends ConfigurationSerializable {
    void handle(ISpecialMob mob, IActionContext context);
}
