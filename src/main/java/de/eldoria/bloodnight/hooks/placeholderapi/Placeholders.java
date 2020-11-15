package de.eldoria.bloodnight.hooks.placeholderapi;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.eldoria.bloodnight.config.Configuration;
import de.eldoria.bloodnight.config.worldsettings.NightSelection;
import de.eldoria.bloodnight.config.worldsettings.WorldSettings;
import de.eldoria.bloodnight.core.BloodNight;
import de.eldoria.bloodnight.core.manager.NightManager;
import de.eldoria.bloodnight.core.manager.nightmanager.NightUtil;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Placeholders extends PlaceholderExpansion {

    private final Pattern probability = Pattern.compile("probability(?:_([0-9]))?");

    private final NightManager nightManager;
    private final Configuration configuration;
    private final Cache<String, String> worldCache = CacheBuilder.newBuilder()
            .expireAfterWrite(2, TimeUnit.SECONDS)
            .build();

    public Placeholders(NightManager nightManager, Configuration configuration) {
        this.nightManager = nightManager;
        this.configuration = configuration;
    }


    @Override
    public @NotNull String getIdentifier() {
        return "bloodnight";
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", BloodNight.getInstance().getDescription().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
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
        World world = player.getWorld();
        WorldSettings worldSettings = configuration.getWorldSettings(world);
        NightSelection sel = worldSettings.getNightSelection();
        try {
            return worldCache.get(world.getName() + "_" + params,
                    () -> {
                        Matcher matcher = probability.matcher(params);
                        if (matcher.matches()) {
                            String group = matcher.group(1);
                            if (group == null) {
                                return String.valueOf(sel.getNextProbability(world, 1));
                            }
                            int i;
                            try {
                                i = Integer.parseInt(group);
                            } catch (NumberFormatException e) {
                                return "0";
                            }
                            return String.valueOf(sel.getNextProbability(world, i));
                        }

                        if ("seconds_left".equalsIgnoreCase(params)) {
                            if (!nightManager.isBloodNightActive(world)) return "0:00";

                            int seconds = NightUtil.getNightSecondsRemaining(world, worldSettings);
                            if (seconds > 3600) {
                                return String.format(
                                        "%d:%02d:%02d",
                                        seconds / 3600,
                                        (seconds % 3600) / 60,
                                        seconds % 60);
                            } else {
                                return String.format(
                                        "%02d:%02d",
                                        (seconds % 3600) / 60,
                                        seconds % 60);
                            }
                        }

                        if ("percent_left".equalsIgnoreCase(params)) {
                            if (!nightManager.isBloodNightActive(world)) return "0";
                            return String.format("%.1f", NightUtil.getNightProgress(world, worldSettings) * 100);
                        }

                        if ("active".equalsIgnoreCase(params)) {
                            return String.valueOf(nightManager.isBloodNightActive(world));
                        }
                        BloodNight.logger().info("Could not calc placeholder settings for " + "bloodnight_" + params);
                        return "";
                    });
        } catch (ExecutionException e) {
            BloodNight.logger().info("Could not calc placeholder settings for " + params);
        }
        return "";
    }
}
