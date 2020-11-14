package de.eldoria.bloodnight.config.worldsettings.sound;

import de.eldoria.bloodnight.core.BloodNight;
import de.eldoria.eldoutilities.serialization.SerializationUtil;
import de.eldoria.eldoutilities.serialization.TypeResolvingMap;
import de.eldoria.eldoutilities.utils.EMath;
import de.eldoria.eldoutilities.utils.EnumUtil;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class SoundEntry implements ConfigurationSerializable {
    private Sound sound = Sound.UI_BUTTON_CLICK;
    private float[] pitch = {1};
    private float[] volume = {1};

    public SoundEntry(Map<String, Object> objectMap) {
        TypeResolvingMap map = SerializationUtil.mapOf(objectMap);
        String name = map.getValueOrDefault("sound", this.sound.name());
        this.sound = EnumUtil.parse(name, Sound.class);
        if (sound == null) {
            sound = Sound.UI_BUTTON_CLICK;
            BloodNight.logger().warning("§4Sound " + name + " is not a valid sound. Changed to " + sound.name());
        }
        pitch = map.getValueOrDefault("pitch", pitch);
        clampArray(pitch, 0.01f, 2);
        volume = map.getValueOrDefault("volume", volume);
        clampArray(volume, 0.01f, 1);
    }

    public SoundEntry(Sound sound, float[] pitch, float[] volume) {
        this.sound = sound;
        this.pitch = pitch;
        this.volume = volume;
    }

    private void clampArray(float[] values, float min, float max) {
        for (int i = 0; i < values.length; i++) {
            values[i] = EMath.clamp(min, max, values[i]);
        }
    }

    public void play(Player player, Location location, SoundCategory channel){
        player.playSound(location, sound, channel, getPitch(), getVolume());
    }

    private float getPitch() {
        if(pitch.length == 0) return 1;
        return pitch[ThreadLocalRandom.current().nextInt(pitch.length)];
    }
    private float getVolume() {
        if(volume.length == 0) return 1;
        return volume[ThreadLocalRandom.current().nextInt(volume.length)];
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
