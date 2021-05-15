package de.eldoria.bloodnight.specialmob.node.context;

import org.bukkit.entity.Entity;

public interface IEntityContext extends IActionContext {
    Entity getEntity();
}
