package de.eldoria.bloodnight.bloodmob.serialization;

import de.eldoria.bloodnight.bloodmob.node.Node;
import de.eldoria.bloodnight.bloodmob.node.predicate.PredicateNode;
import de.eldoria.bloodnight.bloodmob.serialization.annotation.EnumLikeProperty;
import de.eldoria.bloodnight.bloodmob.serialization.annotation.ItemProperty;
import de.eldoria.bloodnight.bloodmob.serialization.annotation.MapProperty;
import de.eldoria.bloodnight.bloodmob.serialization.annotation.MultiListProperty;
import de.eldoria.bloodnight.bloodmob.serialization.annotation.NumberProperty;
import de.eldoria.bloodnight.bloodmob.serialization.annotation.NumericProperty;
import de.eldoria.bloodnight.bloodmob.serialization.annotation.Property;
import de.eldoria.bloodnight.bloodmob.serialization.annotation.StringProperty;
import de.eldoria.bloodnight.bloodmob.serialization.value.MapValueEntry;
import de.eldoria.bloodnight.bloodmob.serialization.value.SimpleValue;
import de.eldoria.bloodnight.bloodmob.serialization.value.Value;
import de.eldoria.bloodnight.bloodmob.serialization.value.ValueProperties;
import de.eldoria.bloodnight.bloodmob.serialization.value.ValueType;
import de.eldoria.bloodnight.bloodmob.settings.Behaviour;
import de.eldoria.bloodnight.bloodmob.settings.Drops;
import de.eldoria.bloodnight.bloodmob.settings.Equipment;
import de.eldoria.bloodnight.bloodmob.settings.Extension;
import de.eldoria.bloodnight.bloodmob.settings.Stats;
import de.eldoria.eldoutilities.container.Pair;
import de.eldoria.eldoutilities.utils.ReflectionUtil;
import org.bukkit.Color;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PropertyGenerator {
    private static final Map<Class<?>, List<String>> CLASS_VALUES = new HashMap<>();

    static {
        registerEnumLikeAdapter(PotionEffectType.class, enumLikeToList(PotionEffectType.class));
    }

    public static <T> List<String> enumLikeToList(Class<T> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> field.getType().equals(clazz))
                .map(Field::getName)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public static void registerEnumLikeAdapter(Class<?> clazz, List<String> values) {
        CLASS_VALUES.put(clazz, values);
    }

    public static <T> List<SimpleValue> generateValues(Class<T> clazz) {
        Field[] declaredFields = ReflectionUtil.getAllFields(clazz);
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
            if (declaredField.isAnnotationPresent((MultiListProperty.class))) {
                values.add(buildMultiListProperty(declaredField));
            }
            if (declaredField.isAnnotationPresent((MapProperty.class))) {
                values.add(buildMapProperty(declaredField));
            }
            if (declaredField.isAnnotationPresent((ItemProperty.class))) {
                values.add(buildItemProperty(declaredField));
            }
        }
        return values;
    }

    public static <T> Pair<String, String> getClassProperty(Class<T> clazz) {
        if (clazz.isAnnotationPresent(Property.class)) {
            Property annotation = clazz.getAnnotation(Property.class);
            return Pair.of(annotation.name(), annotation.descr());
        }
        return Pair.of(null, null);
    }

    private static SimpleValue buildItemProperty(Field field) {
        ItemProperty annotation = field.getAnnotation(ItemProperty.class);
        String id = field.getName();
        String name = annotation.name();
        String descr = annotation.descr();
        return new SimpleValue(id, name, descr, ValueType.ITEM);
    }

    private static SimpleValue buildMultiListProperty(Field field) {
        MultiListProperty annotation = field.getAnnotation(MultiListProperty.class);
        String id = field.getName();
        String name = annotation.name();
        String descr = annotation.descr();
        Class<?> type;
        if (field.getType().isArray()) {
            type = field.getType().getComponentType();
        } else {
            // Gonna assume that this is some kind of single dimension collection.
            type = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
        }
        // Enums
        if (type.isEnum()) {
            List<String> values = Arrays.stream(type.getEnumConstants())
                    .map(e -> ((Enum<?>) e).name())
                    .collect(Collectors.toList());
            return new Value<>(id, name, descr, ValueType.MULTI_LIST, values);
        }
        // Color
        if (field.getType().equals(Color.class)) {
            return new Value<>(id, name, descr, ValueType.MULTI_LIST, ValueType.COLOR);
        }
        // Drops
        if (field.getType().equals(Drops.class)) {
            return new Value<>(id, name, descr, ValueType.MULTI_LIST, ValueType.DROPS);
        }
        return null;
    }

    private static SimpleValue buildMapProperty(Field field) {
        MapProperty annotation = field.getAnnotation(MapProperty.class);
        Class<?> key = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
        Class<?> value = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[1];
        String id = field.getName();
        String name = annotation.name();
        String descr = annotation.descr();

        MapValueEntry keyValue;
        if (key.isEnum()) {
            List<String> enumValues = getEnumValues(key);
            keyValue = MapValueEntry.withValues(enumValues);
        } else {
            keyValue = new MapValueEntry(annotation.key());
        }

        MapValueEntry valueValue;
        if (value.isEnum()) {
            List<String> enumValues = getEnumValues(key);
            valueValue = MapValueEntry.withValues(enumValues);
        } else {
            valueValue = new MapValueEntry(annotation.value());
        }

        return new Value<>(id, name, descr, ValueType.MAP, ValueProperties.ofMaps(keyValue, valueValue));
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
        // Duration
        if (field.getType().equals(Duration.class)) {
            return new SimpleValue(id, name, descr, ValueType.NUMBER);
        }
        // Extension
        if (field.getType().equals(Extension.class)) {
            return new SimpleValue(id, name, descr, ValueType.EXTENSION);
        }
        // Stats
        if (field.getType().equals(Stats.class)) {
            return new SimpleValue(id, name, descr, ValueType.STATS);
        }
        // Drops
        if (field.getType().equals(Drops.class)) {
            return new SimpleValue(id, name, descr, ValueType.DROPS);
        }
        // Behaviour
        if (field.getType().equals(Behaviour.class)) {
            return new SimpleValue(id, name, descr, ValueType.BEHAVIOUR);
        }
        if (field.getType().equals(List.class)) {
            Class<?> type = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
            return new Value<>(id, name, descr, ValueType.LIST, generateValues(type));
        }
        if (field.getType().equals(Node.class)) {
            return new SimpleValue(id, name, descr, ValueType.NODE);
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

    private static List<String> getEnumValues(Class<?> clazz) {
        return Arrays.stream(clazz.getEnumConstants())
                .map(e -> ((Enum<?>) e).name())
                .collect(Collectors.toList());
    }
}
