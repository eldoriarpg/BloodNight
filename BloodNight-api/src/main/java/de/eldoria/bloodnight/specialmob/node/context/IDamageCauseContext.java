package de.eldoria.bloodnight.specialmob.node.context;

import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public interface IDamageCauseContext extends IContext {
    DamageCause getDamageCause();
}
