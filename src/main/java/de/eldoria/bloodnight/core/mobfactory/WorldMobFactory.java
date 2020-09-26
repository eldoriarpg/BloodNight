package de.eldoria.bloodnight.core.mobfactory;

import de.eldoria.bloodnight.config.MobSetting;
import de.eldoria.bloodnight.config.WorldSettings;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.List;
import java.util.Optional;
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

        Optional<MobGroup> mobGroup = getMobGroup(entity);

        if (!mobGroup.isPresent()) return Optional.empty();

        MobGroup group = mobGroup.get();

        List<MobSetting> mobTypes = settings.getMobSettings().getMobTypes();
        List<MobFactory> allowedMobs = group.getFactories().stream()
                .filter(f -> mobTypes.stream()
                        .filter(d -> d.getMobName().equalsIgnoreCase(f.getMobName()))
                        .findFirst()
                        .map(MobSetting::isActive)
                        .orElse(false))
                .collect(Collectors.toList());

        if (allowedMobs.isEmpty()) return Optional.empty();

        return Optional.of(allowedMobs.get(rand.nextInt(allowedMobs.size())));
    }

}
