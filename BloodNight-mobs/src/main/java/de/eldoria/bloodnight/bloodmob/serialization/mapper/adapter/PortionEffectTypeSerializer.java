package de.eldoria.bloodnight.bloodmob.serialization.mapper.adapter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import de.eldoria.bloodnight.bloodmob.serialization.PotionEffectTypeAdapter;
import org.bukkit.potion.PotionEffectType;

import java.io.IOException;

public class PortionEffectTypeSerializer extends StdSerializer<PotionEffectType> {
    protected PortionEffectTypeSerializer(Class<PotionEffectType> t) {
        super(t);
    }

    public PortionEffectTypeSerializer() {
        this(null);
    }

    @Override
    public void serialize(PotionEffectType value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeString(PotionEffectTypeAdapter.idToName(value.getId()));
    }
}
