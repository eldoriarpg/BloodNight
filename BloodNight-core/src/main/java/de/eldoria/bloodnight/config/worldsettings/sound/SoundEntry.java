package de.eldoria.bloodnight.config.worldsettings.sound;

import de.eldoria.eldoutilities.serialization.SerializationUtil;
import de.eldoria.eldoutilities.serialization.TypeResolvingMap;
import de.eldoria.eldoutilities.utils.EMath;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

@SerializableAs("bloodNightSoundEntry")
public class SoundEntry implements ConfigurationSerializable {
    private static final Sound DEFAULT_SOUND = Sound.UI_BUTTON_CLICK;
    private Sound sound = DEFAULT_SOUND;
    private List<Double> pitch = new ArrayList<>() {{
        add(1d);
    }};
    private List<Double> volume = new ArrayList<>() {{
        add(1d);
    }};

    public SoundEntry(Map<String, Object> objectMap) {
        TypeResolvingMap map = SerializationUtil.mapOf(objectMap);
        String name = map.getValueOrDefault("sound", Registry.SOUNDS.getKey(this.sound).value());
        this.sound = Objects.requireNonNullElse(Registry.SOUNDS.get(NamespacedKey.minecraft(name)), DEFAULT_SOUND);
        pitch = map.getValueOrDefault("pitch", pitch);
        clampArray(pitch, 0.01f, 2);
        volume = map.getValueOrDefault("volume", volume);
        clampArray(volume, 0.01f, 1);
    }

    public SoundEntry(Sound sound, Double[] pitch, Double[] volume) {
        this.sound = sound;
        this.pitch = Arrays.asList(pitch);
        this.volume = Arrays.asList(volume);
    }

    private void clampArray(List<Double> values, double min, double max) {
        values.replaceAll(value -> EMath.clamp(min, max, value));
    }

    public void play(Player player, Location location, SoundCategory channel) {
        player.playSound(location, sound, channel, (float) getPitch(), (float) getVolume());
    }

    private double getPitch() {
        if (pitch.isEmpty()) return 1;
        return pitch.get(ThreadLocalRandom.current().nextInt(pitch.size()));
    }

    private double getVolume() {
        if (volume.isEmpty()) return 1;
        return volume.get(ThreadLocalRandom.current().nextInt(volume.size()));
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        return SerializationUtil.newBuilder()
                                .add("sound", Registry.SOUNDS.getKey(sound).value())
                                .add("pitch", pitch)
                                .add("volume", volume)
                                .build();
    }
}
