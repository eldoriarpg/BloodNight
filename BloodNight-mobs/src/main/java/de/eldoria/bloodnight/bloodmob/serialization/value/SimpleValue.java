package de.eldoria.bloodnight.bloodmob.serialization.value;

public class SimpleValue {
    protected final String field;
    protected final String name;
    protected final String descr;
    protected final ValueType type;

    public SimpleValue(String field, String name, String descr, ValueType type) {
        this.field = field;
        this.name = name;
        this.descr = descr;
        this.type = type;
    }
}
