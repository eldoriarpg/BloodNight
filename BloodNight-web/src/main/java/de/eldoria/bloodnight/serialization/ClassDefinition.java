package de.eldoria.bloodnight.serialization;

import de.eldoria.bloodnight.bloodmob.serialization.PropertyGenerator;
import de.eldoria.bloodnight.bloodmob.serialization.value.SimpleValue;

import java.util.List;

public class ClassDefinition {
    private String clazz;
    private List<SimpleValue> values;

    public ClassDefinition(Class<?> clazz, List<SimpleValue> values) {
        this.clazz = clazz.getName();
        this.values = values;
    }

    public static ClassDefinition of(Class<?> clazz) {
        var values = PropertyGenerator.generateValues(clazz);
        return new ClassDefinition(clazz, values);
    }

    public String clazz() {
        return clazz;
    }

    public List<SimpleValue> values() {
        return values;
    }
}
