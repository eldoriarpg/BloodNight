package de.eldoria.bloodnight.core.manager.nightmanager;

import de.eldoria.bloodnight.config.Configuration;
import de.eldoria.bloodnight.core.manager.nightmanager.util.BloodNightData;
import org.bukkit.scheduler.BukkitRunnable;

public class SoundManager extends BukkitRunnable {
    private final NightManager nightManager;
    private final Configuration configuration;

    public SoundManager(NightManager nightManager, Configuration configuration) {
        this.nightManager = nightManager;
        this.configuration = configuration;
    }

    @Override
    public void run() {
        playRandomSound();
    }

    private void playRandomSound() {
        for (BloodNightData data : nightManager.getBloodWorldsMap().values()) {
            data.playRandomSound(configuration.getWorldSettings(data.getWorld()).getSoundSettings());
        }
    }
}
