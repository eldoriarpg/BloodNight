package de.eldoria.bloodnight.bloodmob.serialization;

import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class PotionEffectTypeAdapter {
    private static final Map<Integer, String> byId = new HashMap<>();
    private static final Map<String, PotionEffectType> byName = new HashMap<>();

    static {
        for (Field field : PotionEffectType.class.getDeclaredFields()) {
            if (field.getType().equals(PotionEffectType.class)) {
                PotionEffectType potionEffectType;
                try {
                    potionEffectType = (PotionEffectType) field.get(null);
                } catch (IllegalAccessException e) {
                    continue;
                }
                byId.put(potionEffectType.getId(), field.getName());
                byName.put(field.getName(), potionEffectType);
            }
        }
    }

    public static String idToName(int id) {
        return byId.get(id);
    }

    public static PotionEffectType nameToEffect(String id) {
        return byName.get(id);
    }
}
