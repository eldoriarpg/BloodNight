package de.eldoria.bloodnight.serialization;

public class DataDescriptionContainer<T> {
    Object data;
    T definition;

    public DataDescriptionContainer(Object object, T of) {
        data = object;
        definition = of;
    }

    public static DataDescriptionContainer<ClassDefinition> of(Object object) {
        return new DataDescriptionContainer<>(object, ClassDefinition.of(object.getClass()));
    }

    public static  DataDescriptionContainer<ClassDefinition> of(Object object, Class<?> clazz) {
        return new DataDescriptionContainer<>(object, ClassDefinition.of(clazz));
    }

    public static <T> DataDescriptionContainer<T> of(Object object, T definition) {
        return new DataDescriptionContainer<>(object, definition);
    }
}
