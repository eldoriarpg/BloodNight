package de.eldoria.bloodnight.serialization;

import de.eldoria.bloodnight.bloodmob.serialization.PropertyGenerator;
import de.eldoria.bloodnight.bloodmob.serialization.value.SimpleValue;
import de.eldoria.eldoutilities.container.Pair;

import java.util.List;
import java.util.stream.Collectors;

public class ClassDefinition {
    private String clazz;
    private String name;
    private String description;
    private List<SimpleValue> values;

    public ClassDefinition(Class<?> clazz, String name, String description, List<SimpleValue> values) {
        this.clazz = clazz.getName();
        this.name = name;
        this.description = description;
        this.values = values;
    }

    public static ClassDefinition of(Class<?> clazz) {
        var values = PropertyGenerator.generateValues(clazz);
        var classProperty = PropertyGenerator.getClassProperty(clazz);
        return new ClassDefinition(clazz, classProperty.first, classProperty.second, values);
    }

    public static List<ClassDefinition> of(List<Class<?>> clazz) {
        return clazz.stream().map(ClassDefinition::of).collect(Collectors.toList());
    }

    public String clazz() {
        return clazz;
    }

    public List<SimpleValue> values() {
        return values;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }
}
