package de.eldoria.bloodnight.hooks.placeholderapi;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.eldoria.bloodnight.core.BloodNight;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Placeholders extends PlaceholderExpansion {

    private final Pattern probability = Pattern.compile("probability(?:_([0-9]))?");
    private final Pattern worldActive = Pattern.compile("active_(?<world>.+)");

    private final Cache<String, String> worldCache = CacheBuilder.newBuilder()
            .expireAfterWrite(500, TimeUnit.MILLISECONDS)
            .build();

    @Override
    @NotNull
    public String getIdentifier() {
        return "bloodnight";
    }

    @Override
    @NotNull
    public String getAuthor() {
        return String.join(", ", BloodNight.getInstance().getDescription().getAuthors());
    }

    @Override
    @NotNull
    public String getVersion() {
        return BloodNight.getInstance().getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) {
            try {
                return worldCache.get(params, () -> {
                    Matcher matcher = worldActive.matcher(params);
                    if(matcher.find()){
                        String worldName = matcher.group("world");
                        World world = Bukkit.getWorld(worldName);
                        if(world == null) return "Invalid world";
                        return String.valueOf(BloodNight.getBloodNightAPI().isBloodNightActive(world));
                    }
                    return "";
                });
            } catch (ExecutionException e) {
                BloodNight.logger().log(Level.WARNING, "Could not calc placeholder settings for " + params, e);
                return "";
            }
        }

        World world = player.getWorld();
        try {
            return worldCache.get(world.getName() + "_" + params,
                    () -> {
                        Matcher matcher = worldActive.matcher(params);
                        if(matcher.find()){
                            String worldName = matcher.group("world");
                            World targetWorld = Bukkit.getWorld(worldName);
                            if(targetWorld == null) return "Invalid world";
                            return String.valueOf(BloodNight.getBloodNightAPI().isBloodNightActive(targetWorld));
                        }
                        matcher = probability.matcher(params);
                        if (matcher.matches()) {
                            String group = matcher.group(1);
                            if (group == null) {
                                return String.valueOf(BloodNight.getBloodNightAPI().nextProbability(world, 1));
                            }
                            int offset;
                            try {
                                offset = Integer.parseInt(group);
                            } catch (NumberFormatException e) {
                                return "0";
                            }
                            return String.valueOf(BloodNight.getBloodNightAPI().nextProbability(world, offset));
                        }

                        if ("seconds_left".equalsIgnoreCase(params)) {
                            if (!BloodNight.getBloodNightAPI().isBloodNightActive(world)) return "0:00";

                            int seconds = BloodNight.getBloodNightAPI().getSecondsLeft(world);
                            if (seconds > 3600) {
                                return String.format(
                                        "%d:%02d:%02d",
                                        seconds / 3600,
                                        (seconds % 3600) / 60,
                                        seconds % 60);
                            }
                            return String.format(
                                    "%02d:%02d",
                                    (seconds % 3600) / 60,
                                    seconds % 60);
                        }

                        if ("percent_left".equalsIgnoreCase(params)) {
                            if (!BloodNight.getBloodNightAPI().isBloodNightActive(world)) return "0";
                            return String.format("%.1f", BloodNight.getBloodNightAPI().getPercentleft(world));
                        }

                        if ("active".equalsIgnoreCase(params)) {
                            return String.valueOf(BloodNight.getBloodNightAPI().isBloodNightActive(world));
                        }
                        BloodNight.logger().info("Could not calc placeholder settings for " + "bloodnight_" + params + ". No placeholder exists.");
                        return "";
                    });
        } catch (ExecutionException e) {
            BloodNight.logger().log(Level.WARNING, "Could not calc placeholder settings for " + params, e);
        }
        return "";
    }
}
