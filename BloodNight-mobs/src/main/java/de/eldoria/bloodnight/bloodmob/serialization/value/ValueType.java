package de.eldoria.bloodnight.bloodmob.serialization.value;

import de.eldoria.bloodnight.bloodmob.drop.Drop;
import de.eldoria.bloodnight.bloodmob.node.Node;
import de.eldoria.bloodnight.bloodmob.node.predicate.PredicateNode;
import de.eldoria.bloodnight.bloodmob.registry.items.SimpleItem;
import de.eldoria.bloodnight.bloodmob.settings.Behaviour;
import de.eldoria.bloodnight.bloodmob.settings.Drops;
import de.eldoria.bloodnight.bloodmob.settings.Equipment;
import de.eldoria.bloodnight.bloodmob.settings.Extension;
import de.eldoria.bloodnight.bloodmob.settings.Stats;

public enum ValueType {
    // simple Types
    STRING,
    NUMBER,
    NUMERIC,
    BOOLEAN,
    // collections
    LIST,
    MULTI_LIST,
    MAP,
    // bukkit
    COLOR,
    ITEM(SimpleItem.class),
    // custom settings
    EXTENSION(Extension.class),
    EQUIPMENT(Equipment.class),
    STATS(Stats.class),
    DROPS(Drops.class),
    DROP(Drop.class),
    BEHAVIOUR(Behaviour.class),
    // custom objects
    PREDICATE(PredicateNode.class),
    NODE(Node.class);

    private final Class<?> clazz;

    ValueType(Class<?> stringsClass) {
        clazz = stringsClass;
    }

    ValueType() {
        clazz = null;
    }

    public Class<?> clazz() {
        return clazz;
    }
}