package de.eldoria.bloodnight.bloodmob.serialization.mapper.adapter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.time.Duration;

public class DurationSerializer extends StdSerializer<Duration> {
    protected DurationSerializer(Class<Duration> t) {
        super(t);
    }

    public DurationSerializer() {
        this(null);
    }

    @Override
    public void serialize(Duration value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeNumber(value.getSeconds());
    }
}
