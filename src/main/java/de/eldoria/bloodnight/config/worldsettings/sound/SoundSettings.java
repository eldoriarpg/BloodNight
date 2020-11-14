package de.eldoria.bloodnight.config.worldsettings.sound;

import de.eldoria.bloodnight.core.BloodNight;
import de.eldoria.bloodnight.util.Sounds;
import de.eldoria.eldoutilities.serialization.SerializationUtil;
import de.eldoria.eldoutilities.serialization.TypeResolvingMap;
import de.eldoria.eldoutilities.utils.EnumUtil;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class SoundSettings implements ConfigurationSerializable {
    private int minInterval = 10;
    private int maxInterval = 40;
    @Getter
    private SoundCategory channel = SoundCategory.AMBIENT;
    private List<SoundEntry> startSounds = new ArrayList<SoundEntry>() {{
        for (Sound sound : Sounds.START) {
            add(new SoundEntry(sound, new float[]{0.8f, 1}, new float[]{1}));
        }
    }};
    private List<SoundEntry> endSounds = new ArrayList<SoundEntry>() {{
        for (Sound sound : Sounds.START) {
            add(new SoundEntry(sound, new float[]{0.8f, 1}, new float[]{1}));
        }
    }};
    private List<SoundEntry> randomSounds = new ArrayList<SoundEntry>() {{
        for (Sound sound : Sounds.SPOOKY) {
            add(new SoundEntry(sound, new float[]{0.4f, 0.6f, 0.8f, 1, 1.2f, 1.4f, 1.6f}, new float[]{0.2f, 0.4f, 0.6f, 0.8f, 1}));
        }
    }};
    private List<SoundEntry> loopSounds = new ArrayList<SoundEntry>() {{
        for (Sound sound : Sounds.LOOPS) {
            add(new SoundEntry(sound, new float[]{0.8f, 1}, new float[]{1}));
        }
    }};

    public SoundSettings(Map<String, Object> objectMap) {
        TypeResolvingMap map = SerializationUtil.mapOf(objectMap);
        minInterval = map.getValueOrDefault("minInterval", minInterval);
        maxInterval = map.getValueOrDefault("maxInterval", maxInterval);
        String channel = map.getValueOrDefault("channel", this.channel.name());
        this.channel = EnumUtil.parse(channel, SoundCategory.class);
        if (this.channel == null) {
            this.channel = SoundCategory.AMBIENT;
            BloodNight.logger().warning("Channel " + channel + " is invalid. Changed to AMBIENT.");
        }
        startSounds = map.getValueOrDefault("startSounds", startSounds);
        endSounds = map.getValueOrDefault("endSounds", endSounds);
        randomSounds = map.getValueOrDefault("randomSounds", randomSounds);
        loopSounds = map.getValueOrDefault("loopSounds", loopSounds);
    }

    public void playRandomSound(Player player, Location location) {
        if (randomSounds.isEmpty()) return;
        randomSounds.get(ThreadLocalRandom.current().nextInt(randomSounds.size())).play(player, location, channel);
    }

    public void playStartSound(Player player) {
        for (SoundEntry endSound : endSounds) {
            endSound.play(player, player.getLocation(), channel);
        }
    }

    public void playEndsound(Player player){
        for (SoundEntry endSound : endSounds) {
            endSound.play(player, player.getLocation(), channel);
        }
    }

    public int getWaitSeconds() {
        return ThreadLocalRandom.current().nextInt(minInterval, maxInterval + 1);
    }

    public SoundSettings() {
    }


    @Override
    public @NotNull Map<String, Object> serialize() {
        return SerializationUtil.newBuilder()
                .add("minInterval", minInterval)
                .add("maxInterval", maxInterval)
                .add("channel", channel.name())
                .add("startSounds", startSounds)
                .add("endSounds", endSounds)
                .add("randomSounds", randomSounds)
                .add("loopSounds", loopSounds)
                .build();
    }
}
