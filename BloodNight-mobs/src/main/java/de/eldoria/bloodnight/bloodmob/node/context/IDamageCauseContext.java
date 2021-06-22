package de.eldoria.bloodnight.bloodmob.node.context;

import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public interface IDamageCauseContext extends IContext {
    static IDamageCauseContext of(DamageCause cause) {
        return () -> cause;
    }

    DamageCause getDamageCause();
}
