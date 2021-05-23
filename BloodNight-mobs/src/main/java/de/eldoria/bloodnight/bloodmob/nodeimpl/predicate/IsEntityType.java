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
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@NoArgsConstructor
public class IsEntityType implements PredicateNode {
    @Property(name = "", descr = "")
    private EntityType type;

    public IsEntityType(EntityType type) {
        this.type = type;
    }

    public IsEntityType(Map<String, Object> objectMap) {
        TypeResolvingMap map = SerializationUtil.mapOf(objectMap);
        type = map.getValue("types", s -> EnumUtil.parse(s, EntityType.class));
    }

    @Override
    @NotNull
    public Map<String, Object> serialize() {
        return SerializationUtil.newBuilder()
                .add("types", type.name())
                .build();
    }

    @Override
    public boolean test(IBloodMob mob, ContextContainer context) {
        return context.get(ContextType.ENTITY).map(c -> type == c.getEntity().getType()).orElse(false);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IsEntityType that = (IsEntityType) o;

        return type == that.type;
    }

    @Override
    public int hashCode() {
        return type != null ? type.hashCode() : 0;
    }
}
