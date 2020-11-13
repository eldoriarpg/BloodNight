package de.eldoria.bloodnight.core.mobfactory;

import de.eldoria.bloodnight.config.worldsettings.WorldSettings;
import de.eldoria.bloodnight.config.worldsettings.mobsettings.MobSetting;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static de.eldoria.bloodnight.core.mobfactory.SpecialMobRegistry.getMobGroup;

public class WorldMobFactory {
    private final WorldSettings settings;
    private final ThreadLocalRandom rand = ThreadLocalRandom.current();

    public WorldMobFactory(WorldSettings settings) {
        this.settings = settings;
    }

    public Optional<MobFactory> getRandomFactory(Entity entity) {
        if (!(entity instanceof LivingEntity)) return Optional.empty();

        // Get the group of the mob
        Optional<MobGroup> optionalMobGroup = getMobGroup(entity);

        if (!optionalMobGroup.isPresent()) return Optional.empty();

        MobGroup mobGroup = optionalMobGroup.get();

        Set<MobSetting> settings = this.settings.getMobSettings().getMobTypes().getSettings();

        // Search filter for factories with active mobs
        List<MobFactory> allowedFactories = mobGroup.getFactories().stream()
                .filter(factory -> settings.stream()
                        // search for setting for factory
                        .filter(setting -> setting.getMobName().equalsIgnoreCase(factory.getMobName()))
                        // take first
                        .findFirst()
                        // draw active value or false
                        .map(MobSetting::isActive)
                        .orElse(false))
                .collect(Collectors.toList());

        if (allowedFactories.isEmpty()) return Optional.empty();

        return Optional.of(allowedFactories.get(rand.nextInt(allowedFactories.size())));
    }

}
