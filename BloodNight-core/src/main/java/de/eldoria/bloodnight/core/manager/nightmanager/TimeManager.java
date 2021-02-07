package de.eldoria.bloodnight.core.manager.nightmanager;

import de.eldoria.bloodnight.config.Configuration;
import de.eldoria.bloodnight.config.worldsettings.NightSettings;
import de.eldoria.bloodnight.config.worldsettings.WorldSettings;
import de.eldoria.bloodnight.core.manager.nightmanager.util.BloodNightData;
import de.eldoria.bloodnight.core.manager.nightmanager.util.NightUtil;
import de.eldoria.eldoutilities.utils.ObjUtil;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.TimeSkipEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class TimeManager extends BukkitRunnable implements Listener {
    private final Configuration configuration;
    private final NightManager nightManager;
    /**
     * Map contains for every active world a boolean if it is currently night.
     */
    private final Map<String, Boolean> timeState = new HashMap<>();

    private final Map<String, Double> customTimes = new HashMap<>();
    // <--- World consistency ---> //


    public TimeManager(Configuration configuration, NightManager nightManager) {
        this.configuration = configuration;
        this.nightManager = nightManager;
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        calcualteWorldState(event.getWorld());
    }

    // <--- Time consistency ---> //

    /**
     * Recalulate time state for immediate impact
     *
     * @param event time skip event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onTimeSkip(TimeSkipEvent event) {
        customTimes.computeIfPresent(event.getWorld().getName(), (k, v) -> v + event.getSkipAmount());
    }

    @Override
    public void run() {
        for (World observedWorld : Bukkit.getWorlds()) {
            calcualteWorldState(observedWorld);
        }

        refreshTime();
    }

    private void calcualteWorldState(World world) {
        boolean current = NightUtil.isNight(world, configuration.getWorldSettings(world));
        boolean old = timeState.getOrDefault(world.getName(), false);

        if (current == old) {
            return;
        }

        timeState.put(world.getName(), current);

        if (current) {
            // A new night has begun.
            nightManager.startNight(world);
            return;
        }

        if (nightManager.isBloodNightActive(world)) {
            // A blood night has ended.
            nightManager.endNight(world);
        }
    }

    private void refreshTime() {
        for (Map.Entry<World, BloodNightData> entry : nightManager.getBloodWorldsMap().entrySet()) {
            World world = entry.getKey();
            WorldSettings settings = configuration.getWorldSettings(world.getName());
            NightSettings ns = settings.getNightSettings();
            if (ns.isCustomNightDuration()) {
                double calcTicks = NightUtil.getNightTicksPerTick(world, settings);

                double time = customTimes.compute(world.getName(),
                        (key, old) -> (old == null ? world.getFullTime() : old) + calcTicks);

                world.setFullTime(Math.round(time));
            }

            BloodNightData bloodNightData = entry.getValue();
            ObjUtil.nonNull(bloodNightData.getBossBar(), bossBar -> {
                bossBar.setProgress(NightUtil.getNightProgress(world, configuration.getWorldSettings(world)));
            });
        }
    }

    public void removeCustomTime(World world) {
        customTimes.remove(world.getName());
    }

    public void reload() {
        timeState.clear();
    }
}
