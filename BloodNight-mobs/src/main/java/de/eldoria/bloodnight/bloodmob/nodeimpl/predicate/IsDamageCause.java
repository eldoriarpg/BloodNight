package de.eldoria.bloodnight.bloodmob.nodeimpl.predicate;

import de.eldoria.bloodnight.bloodmob.IBloodMob;
import de.eldoria.bloodnight.bloodmob.node.contextcontainer.ContextContainer;
import de.eldoria.bloodnight.bloodmob.node.contextcontainer.ContextType;
import de.eldoria.bloodnight.bloodmob.node.predicate.PredicateNode;
import de.eldoria.bloodnight.bloodmob.serialization.annotation.Property;
import de.eldoria.eldoutilities.serialization.SerializationUtil;
import de.eldoria.eldoutilities.serialization.TypeResolvingMap;
import de.eldoria.eldoutilities.utils.EnumUtil;
import lombok.NoArgsConstructor;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@NoArgsConstructor
public class IsDamageCause implements PredicateNode {
    @Property(name = "", descr = "")
    private EntityDamageEvent.DamageCause cause;

    public IsDamageCause(EntityDamageEvent.DamageCause cause) {
        this.cause = cause;
    }

    public IsDamageCause(Map<String, Object> objectMap) {
        TypeResolvingMap map = SerializationUtil.mapOf(objectMap);
        cause = map.getValue("cause", s -> EnumUtil.parse(s, EntityDamageEvent.DamageCause.class));
    }

    @Override
    @NotNull
    public Map<String, Object> serialize() {
        return SerializationUtil.newBuilder()
                .add("cause", cause.name())
                .build();
    }

    @Override
    public boolean test(IBloodMob mob, ContextContainer context) {
        return context.get(ContextType.DAMAGE_CAUSE)
                .map(cause -> this.cause == cause.getDamageCause())
                .orElse(false);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IsDamageCause that = (IsDamageCause) o;

        return cause == that.cause;
    }

    @Override
    public int hashCode() {
        return cause != null ? cause.hashCode() : 0;
    }
}
