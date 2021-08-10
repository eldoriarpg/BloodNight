package de.eldoria.bloodnight.hooks.placeholderapi;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.eldoria.bloodnight.core.BloodNight;
import de.eldoria.eldoutilities.utils.Parser;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Placeholders extends PlaceholderExpansion {

    private final Pattern probability = Pattern.compile("probability(?:_(?<offset>[0-9]))?");
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
        if (player == null) return retriveFromWorldCache(params, () -> calcWorldPlaceholder(params));

        World world = player.getWorld();
        return retriveFromWorldCache(world.getName() + "_" + params, () -> calcPlayerPlaceholder(params, world));
    }

    public String retriveFromWorldCache(String key, Callable<String> calc) {
        try {
            return worldCache.get(key, calc);
        } catch (ExecutionException e) {
            BloodNight.logger().log(Level.WARNING, "Could not calc placeholder settings for " + key, e);
        }
        return "";
    }

    @NotNull
    private String calcWorldPlaceholder(@NotNull String params) {
        Matcher matcher = worldActive.matcher(params);
        if (matcher.find()) return worldActiveByString(matcher);
        return "";
    }

    @NotNull
    private String calcPlayerPlaceholder(@NotNull String params, @NotNull World world) {
        Matcher matcher = worldActive.matcher(params);
        if (matcher.find()) return worldActiveByString(matcher);

        matcher = probability.matcher(params);
        if (matcher.matches()) return probability(world, matcher);

        if ("seconds_left".equalsIgnoreCase(params)) return secondsLeft(world);

        if ("percent_left".equalsIgnoreCase(params)) return percentLeft(world);

        if ("active".equalsIgnoreCase(params)) {
            return active(world);
        }
        BloodNight.logger().info("Could not calc placeholder settings for " + "bloodnight_" + params + ". No placeholder exists.");
        return "";
    }

    @NotNull
    private String worldActiveByString(Matcher matcher) {
        String worldName = matcher.group("world");
        World targetWorld = Bukkit.getWorld(worldName);
        if (targetWorld == null) return "Invalid world";
        return active(targetWorld);
    }

    @NotNull
    private String active(@NotNull World world) {
        return String.valueOf(BloodNight.getBloodNightAPI().isBloodNightActive(world));
    }

    @NotNull
    private String probability(World world, Matcher matcher) {
        String offsetGroup = Optional.ofNullable(matcher.group("offset")).orElse("1");
        int offset = Parser.parseInt(offsetGroup).orElse(1);
        return String.valueOf(BloodNight.getBloodNightAPI().nextProbability(world, offset));
    }

    @NotNull
    private String percentLeft(World world) {
        if (!BloodNight.getBloodNightAPI().isBloodNightActive(world)) return "0";
        return String.format("%.1f", BloodNight.getBloodNightAPI().getPercentleft(world));
    }

    @NotNull
    private String secondsLeft(World world) {
        if (!BloodNight.getBloodNightAPI().isBloodNightActive(world)) return "0:00";

        int seconds = BloodNight.getBloodNightAPI().getSecondsLeft(world);
        if (seconds > 3600) {
            return String.format("%d:%02d:%02d", seconds / 3600, (seconds % 3600) / 60, seconds % 60);
        }
        return String.format("%02d:%02d", (seconds % 3600) / 60, seconds % 60);
    }
}
