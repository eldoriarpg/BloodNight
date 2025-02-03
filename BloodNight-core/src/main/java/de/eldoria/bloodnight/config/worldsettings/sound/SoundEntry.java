package de.eldoria.bloodnight.config.worldsettings.sound;

import de.eldoria.eldoutilities.serialization.SerializationUtil;
import de.eldoria.eldoutilities.serialization.TypeResolvingMap;
import de.eldoria.eldoutilities.utils.EMath;
import de.eldoria.eldoutilities.utils.EnumUtil;
import org.bukkit.Location;
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
import java.util.concurrent.ThreadLocalRandom;

@SerializableAs("bloodNightSoundEntry")
public class SoundEntry implements ConfigurationSerializable {
    private Sound sound = Sound.UI_BUTTON_CLICK;
    private List<Double> pitch = new ArrayList<>() {{
        add(1d);
    }};
    private List<Double> volume = new ArrayList<>() {{
        add(1d);
    }};

    public SoundEntry(Map<String, Object> objectMap) {
        TypeResolvingMap map = SerializationUtil.mapOf(objectMap);
        String name = map.getValueOrDefault("sound", this.sound.name());
        this.sound = EnumUtil.parse(name, Sound.class).orElse(Sound.UI_BUTTON_CLICK);
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
                .add("sound", sound.name())
                .add("pitch", pitch)
                .add("volume", volume)
                .build();
    }
}
