package de.eldoria.bloodnight.specialmob.node.predicate;

import de.eldoria.bloodnight.specialmob.ISpecialMob;
import de.eldoria.bloodnight.specialmob.node.context.IActionContext;
import de.eldoria.bloodnight.specialmob.node.context.IDamageCauseContext;
import de.eldoria.bloodnight.specialmob.node.context.IEntityContext;
import de.eldoria.eldoutilities.serialization.SerializationUtil;
import de.eldoria.eldoutilities.serialization.TypeResolvingMap;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class IsNotDamageCause implements PredicateNode {
    private List<EntityDamageEvent.DamageCause> cause;

    public IsNotDamageCause(Map<String, Object> objectMap) {
        TypeResolvingMap map = SerializationUtil.mapOf(objectMap);
        cause = map.getValueOrDefault("cause", new ArrayList<>(), EntityDamageEvent.DamageCause.class);
    }

    @Override
    @NotNull
    public Map<String, Object> serialize() {
        return SerializationUtil.newBuilder()
                .addEnum("cause", cause)
                .build();
    }

    @Override
    public boolean test(ISpecialMob mob, IActionContext context) {
        if (context instanceof IDamageCauseContext) {
            return !cause.contains(((IDamageCauseContext) context).getDamageCause());
        }
        return true;
    }
}
