package de.eldoria.bloodnight.bloodmob.serialization.mapper.adapter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.bukkit.Color;

import java.io.IOException;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BukkitColorDeserializer extends StdDeserializer<Color> {
    protected BukkitColorDeserializer(Class<Duration> t) {
        super(t);
    }
    private static final Pattern HEX = Pattern.compile("#(?<r>[0-9]{2})(?<g>[0-9]{2})(?<b>[0-9]{2})");

    public BukkitColorDeserializer() {
        this(null);
    }

    @Override
    public Color deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        Matcher matcher = HEX.matcher(p.getValueAsString());
        if(!matcher.find()) return Color.WHITE;
        return Color.fromRGB(
                Integer.parseInt(matcher.group("r"), 16),
                Integer.parseInt(matcher.group("g"), 16),
                Integer.parseInt(matcher.group("b"), 16));
    }
}
