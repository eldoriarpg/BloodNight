package de.eldoria.bloodnight.bloodmob.serialization.mapper.adapter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.bukkit.Color;

import java.io.IOException;
import java.time.Duration;

public class BukkitColorSerializer extends StdSerializer<Color> {
    protected BukkitColorSerializer(Class<Color> t) {
        super(t);
    }

    public BukkitColorSerializer() {
        this(null);
    }

    @Override
    public void serialize(Color value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeString(String.format("#%02x%02x%02x", value.getRed(), value.getGreen(), value.getBlue()));
    }
}
