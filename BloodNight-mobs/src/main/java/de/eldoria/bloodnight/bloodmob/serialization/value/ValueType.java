package de.eldoria.bloodnight.bloodmob.serialization.value;

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
    ITEM,
    // custom settings
    EXTENSION,
    EQUIPMENT,
    STATS,
    DROPS,
    BEHAVIOUR,
    // custom objects
    PREDICATE
}