package de.eldoria.bloodnight.core.api;

import de.eldoria.bloodnight.api.IBloodNightAPI;
import de.eldoria.bloodnight.config.Configuration;
import de.eldoria.bloodnight.config.worldsettings.NightSelection;
import de.eldoria.bloodnight.core.manager.NightManager;
import de.eldoria.bloodnight.core.manager.nightmanager.NightUtil;
import org.bukkit.World;

import java.util.Set;

/**
 * Provides safe to use methods to interact with Blood Night.
 *
 * @since 0.8
 */
public class BloodNightAPI implements IBloodNightAPI {
    private final NightManager nightManager;
    private final Configuration configuration;

    public BloodNightAPI(NightManager nightManager, Configuration configuration) {
        this.nightManager = nightManager;
        this.configuration = configuration;
    }

    @Override
    public boolean isBloodNightActive(World world) {
        return nightManager.isBloodNightActive(world);
    }

    @Override
    public void forceNight(World world) {
        nightManager.forceNight(world);
    }

    @Override
    public void cancelNight(World world) {
        nightManager.cancelNight(world);
    }

    @Override
    public Set<World> getBloodWorlds() {
        return nightManager.getBloodWorldsSet();
    }

    @Override
    public int getSecondsLeft(World world) {
        if (!isBloodNightActive(world)) return 0;
        return NightUtil.getSecondsRemaining(world, configuration.getWorldSettings(world));
    }

    @Override
    public double getPercentleft(World world) {
        if (!isBloodNightActive(world)) return 0;
        return NightUtil.getNightProgress(world, configuration.getWorldSettings(world)) * 100;
    }

    @Override
    public int nextProbability(World world, int offset) {
        //if (!isBloodNightActive(world)) return 0;
        NightSelection ns = configuration.getWorldSettings(world).getNightSelection();
        return ns.getNextProbability(world, offset);
    }
}
