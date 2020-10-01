package de.eldoria.bloodnight.core.mobfactory;

import de.eldoria.bloodnight.core.BloodNight;
import de.eldoria.bloodnight.specialmobs.SpecialMob;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

public final class SpecialMobRegistry {

    private static final Map<Class<? extends Entity>, MobGroup> MOB_GROUPS = new HashMap<>();
    private static final Map<Class<? extends Entity>, Class<? extends Entity>> ENTITY_MAPPINGS = new HashMap<>();
    private static final ThreadLocalRandom RAND = ThreadLocalRandom.current();

    private SpecialMobRegistry() {
    }

    public static void registerMob(EntityType entityType, String displayName, Function<LivingEntity, SpecialMob> factory) {
        MOB_GROUPS.computeIfAbsent(entityType.getEntityClass(), k -> new MobGroup(entityType)).registerFactory(displayName, factory);
    }

    public static Optional<Class<? extends Entity>> getMobBaseClass(Entity entity) {
        if (entity.getType() == EntityType.UNKNOWN) return Optional.empty();

        Class<? extends Entity> mob = ENTITY_MAPPINGS.computeIfAbsent(entity.getType().getEntityClass(), k -> {
            for (Class<? extends Entity> clazz : MOB_GROUPS.keySet()) {
                if (clazz.isAssignableFrom(k)) {
                    BloodNight.logger().info(clazz.getSimpleName() + " is assignable to " + k.getSimpleName());
                    return clazz;
                }
            }
            return null;
        });
        return Optional.ofNullable(mob);
    }

    public static Optional<MobGroup> getMobGroup(Entity entity) {
        return getMobBaseClass(entity).map(MOB_GROUPS::get);
    }

    public static Set<MobFactory> getRegisteredMobs() {
        Set<MobFactory> wrappers = new HashSet<>();
        for (MobGroup value : MOB_GROUPS.values()) {
            wrappers.addAll(value.getFactories());
        }
        return wrappers;
    }

    public static Optional<MobFactory> getMobFactoryByName(String arg) {
        return getRegisteredMobs().stream().filter(f -> f.getMobName().equalsIgnoreCase(arg)).findFirst();
    }
}
