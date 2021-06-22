package de.eldoria.bloodnight.bloodmob.serialization.mapper;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import de.eldoria.bloodnight.bloodmob.serialization.mapper.adapter.*;
import org.bukkit.Color;
import org.bukkit.potion.PotionEffectType;

import java.time.Duration;

public class MobMapper {
    private static final ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper()
                .setDefaultPrettyPrinter(new DefaultPrettyPrinter())
                .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
                .setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE)
                .setVisibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.NONE);
        SimpleModule module = new SimpleModule();
        module.addSerializer(PotionEffectType.class, new PortionEffectTypeSerializer());
        module.addDeserializer(PotionEffectType.class, new PortionEffectTypeDeserializer());
        module.addSerializer(Duration.class, new DurationSerializer());
        module.addDeserializer(Duration.class, new DurationDeserializer());
        module.addSerializer(Color.class, new BukkitColorSerializer());
        module.addDeserializer(Color.class, new BukkitColorDeserializer());
        objectMapper.registerModule(module);
    }

    public static ObjectMapper mapper() {
        return objectMapper;
    }
}
