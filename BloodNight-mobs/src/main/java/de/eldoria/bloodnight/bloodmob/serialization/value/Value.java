package de.eldoria.bloodnight.bloodmob.serialization.value;

public class Value<T> extends SimpleValue {
    private final T values;

    public Value(String field, String name, String descr, ValueType type, T values) {
        super(field, name, descr, type);
        this.values = values;
    }
}