package de.eldoria.bloodnight.bloodmob.serialization.mapper.adapter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.time.Duration;

public class DurationDeserializer extends StdDeserializer<Duration> {
    protected DurationDeserializer(Class<Duration> t) {
        super(t);
    }

    public DurationDeserializer() {
        this(null);
    }

    @Override
    public Duration deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        return Duration.ofSeconds(p.getNumberValue().intValue());
    }
}
