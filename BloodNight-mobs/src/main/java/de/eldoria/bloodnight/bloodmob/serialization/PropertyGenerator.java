package de.eldoria.bloodnight.bloodmob.serialization;

import de.eldoria.bloodnight.bloodmob.node.predicate.PredicateNode;
import de.eldoria.bloodnight.bloodmob.serialization.annotation.*;
import de.eldoria.bloodnight.bloodmob.serialization.value.SimpleValue;
import de.eldoria.bloodnight.bloodmob.serialization.value.Value;
import de.eldoria.bloodnight.bloodmob.serialization.value.ValueProperties;
import de.eldoria.bloodnight.bloodmob.serialization.value.ValueType;
import de.eldoria.bloodnight.bloodmob.settings.Equipment;
import org.bukkit.Color;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class PropertyGenerator {
    private static final Map<Class<?>, List<String>> CLASS_VALUES = new HashMap<>();

    static {
        registerEnumLikeAdapter(PotionEffectType.class, enumLikeToList(PotionEffectType.class));
    }

    public static  <T> List<String> enumLikeToList(Class<T> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> field.getType().equals(clazz))
                .map(Field::getName)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public static void registerEnumLikeAdapter(Class<?> clazz, List<String> values) {
        CLASS_VALUES.put(clazz, values);
    }

    public static <T> List<SimpleValue> generateValues(Class<T> clazz) {
        Field[] declaredFields = clazz.getDeclaredFields();
        List<SimpleValue> values = new ArrayList<>();
        for (Field declaredField : declaredFields) {
            if (declaredField.isAnnotationPresent(Property.class)) {
                values.add(buildProperty(declaredField));
            }
            if (declaredField.isAnnotationPresent(EnumLikeProperty.class)) {
                values.add(buildEnumLikeProperty(declaredField));
            }
            if (declaredField.isAnnotationPresent(NumberProperty.class)) {
                values.add(buildNumberProperty(declaredField));
            }
            if (declaredField.isAnnotationPresent(NumericProperty.class)) {
                values.add(buildNumericProperty(declaredField));
            }
            if (declaredField.isAnnotationPresent(StringProperty.class)) {
                values.add(buildStringProperty(declaredField));
            }
        }
        return values;
    }

    private static SimpleValue buildStringProperty(Field field) {
        StringProperty annotation = field.getAnnotation(StringProperty.class);
        String id = field.getName();
        String name = annotation.name();
        String descr = annotation.descr();

        String pattern = annotation.pattern();
        int min = annotation.min();
        int max = annotation.max();
        return new Value<>(id, name, descr, ValueType.STRING, ValueProperties.ofString(pattern, min, max));
    }

    private static SimpleValue buildEnumLikeProperty(Field field) {
        EnumLikeProperty annotation = field.getAnnotation(EnumLikeProperty.class);
        String id = field.getName();
        String name = annotation.name();
        String descr = annotation.descr();

        if (CLASS_VALUES.containsKey(field.getType())) {
            return new Value<>(id, name, descr, ValueType.LIST, CLASS_VALUES.get(field.getType()));
        }
        throw new IllegalArgumentException("No enum like adapter is registered for field " + field.getDeclaringClass().getName() + "#" + field.getName() + " of type " + field.getType().getSimpleName());

    }

    private static SimpleValue buildProperty(Field field) {
        Property annotation = field.getAnnotation(Property.class);
        String id = field.getName();
        String name = annotation.name();
        String descr = annotation.descr();
        // Enums
        if (field.getType().isEnum()) {
            List<String> values = Arrays.stream(field.getType().getEnumConstants())
                    .map(e -> ((Enum<?>) e).name())
                    .collect(Collectors.toList());
            return new Value<>(id, name, descr, ValueType.LIST, values);
        }
        // Boolean
        if (field.getType().equals(boolean.class)) {
            return new SimpleValue(id, name, descr, ValueType.BOOLEAN);
        }
        // Color
        if (field.getType().equals(Color.class)) {
            return new SimpleValue(id, name, descr, ValueType.COLOR);
        }
        // Predicate
        if (field.getType().equals(PredicateNode.class)) {
            return new SimpleValue(id, name, descr, ValueType.PREDICATE);
        }
        // Item Stacks
        if (field.getType().equals(ItemStack.class)) {
            return new SimpleValue(id, name, descr, ValueType.ITEM);
        }
        // Equipment
        if (field.getType().equals(Equipment.class)) {
            return new SimpleValue(id, name, descr, ValueType.EQUIPMENT);
        }
        // Equipment
        if (field.getType().equals(Duration.class)) {
            return new SimpleValue(id, name, descr, ValueType.NUMBER);
        }
        throw new IllegalArgumentException("Field " + field.getDeclaringClass().getName() + "#" + field.getName() + " is can not be converted.");
    }

    private static SimpleValue buildNumberProperty(Field field) {
        NumberProperty annotation = field.getAnnotation(NumberProperty.class);
        String id = field.getName();
        String name = annotation.name();
        String descr = annotation.descr();
        int min = annotation.min();
        int max = annotation.max();

        return new Value<>(id, name, descr, ValueType.NUMBER, ValueProperties.ofInts(min, max));
    }

    private static SimpleValue buildNumericProperty(Field field) {
        NumericProperty annotation = field.getAnnotation(NumericProperty.class);
        String id = field.getName();
        String name = annotation.name();
        String descr = annotation.descr();
        float min = annotation.min();
        float max = annotation.max();

        return new Value<>(id, name, descr, ValueType.NUMERIC, ValueProperties.ofFloats(min, max));
    }
}
