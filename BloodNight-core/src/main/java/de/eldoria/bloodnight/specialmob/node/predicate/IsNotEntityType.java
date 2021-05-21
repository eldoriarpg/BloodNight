package de.eldoria.bloodnight.specialmob.node.predicate;

import de.eldoria.bloodnight.specialmob.ISpecialMob;
import de.eldoria.bloodnight.specialmob.node.context.ContextContainer;
import de.eldoria.bloodnight.specialmob.node.context.IEntityContext;
import de.eldoria.eldoutilities.serialization.SerializationUtil;
import de.eldoria.eldoutilities.serialization.TypeResolvingMap;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class IsNotEntityType implements PredicateNode {
    private List<EntityType> types;

    public IsNotEntityType(Map<String, Object> objectMap) {
        TypeResolvingMap map = SerializationUtil.mapOf(objectMap);
        types = map.getValueOrDefault("types", new ArrayList<>(), EntityType.class);
    }

    @Override
    @NotNull
    public Map<String, Object> serialize() {
        return SerializationUtil.newBuilder()
                .addEnum("types", types)
                .build();
    }

    @Override
    public boolean test(ISpecialMob mob, ContextContainer context) {
        if (context instanceof IEntityContext) {
            return !types.contains(((IEntityContext) context).getEntity().getType());
        }
        return true;
    }
}
