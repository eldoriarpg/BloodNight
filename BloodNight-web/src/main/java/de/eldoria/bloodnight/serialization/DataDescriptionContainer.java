package de.eldoria.bloodnight.serialization;

public class DataDescriptionContainer<Data, T> {
    Data data;
    T definition;

    public DataDescriptionContainer(Data object, T of) {
        data = object;
        definition = of;
    }

    public static <Data> DataDescriptionContainer<Data, ClassDefinition> of(Data object) {
        return new DataDescriptionContainer<>(object, ClassDefinition.of(object.getClass()));
    }

    public static <Data> DataDescriptionContainer<Data, ClassDefinition> of(Data object, Class<?> clazz) {
        return new DataDescriptionContainer<>(object, ClassDefinition.of(clazz));
    }

    public static <Data, T> DataDescriptionContainer<Data, T> of(Data object, T definition) {
        return new DataDescriptionContainer<>(object, definition);
    }
}
