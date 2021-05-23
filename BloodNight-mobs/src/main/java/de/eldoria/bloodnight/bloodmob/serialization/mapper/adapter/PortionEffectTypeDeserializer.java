package de.eldoria.bloodnight.bloodmob.serialization.mapper.adapter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import de.eldoria.bloodnight.bloodmob.serialization.PotionEffectTypeAdapter;
import org.bukkit.potion.PotionEffectType;

import java.io.IOException;

public class PortionEffectTypeDeserializer extends StdDeserializer<PotionEffectType> {
    protected PortionEffectTypeDeserializer(Class<PotionEffectType> t) {
        super(t);
    }

    public PortionEffectTypeDeserializer() {
        this(null);
    }

    @Override
    public PotionEffectType deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        return PotionEffectTypeAdapter.nameToEffect(p.getValueAsString());
    }
}
