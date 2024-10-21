package de.eldoria.bloodnight.core.mobfactory;

import de.eldoria.bloodnight.specialmobs.SpecialMob;
import de.eldoria.bloodnight.specialmobs.mobs.creeper.EnderCreeper;
import de.eldoria.bloodnight.specialmobs.mobs.creeper.GhostCreeper;
import de.eldoria.bloodnight.specialmobs.mobs.creeper.NervousPoweredCreeper;
import de.eldoria.bloodnight.specialmobs.mobs.creeper.SpeedCreeper;
import de.eldoria.bloodnight.specialmobs.mobs.creeper.ToxicCreeper;
import de.eldoria.bloodnight.specialmobs.mobs.creeper.UnstableCreeper;
import de.eldoria.bloodnight.specialmobs.mobs.enderman.FearfulEnderman;
import de.eldoria.bloodnight.specialmobs.mobs.enderman.ToxicEnderman;
import de.eldoria.bloodnight.specialmobs.mobs.phantom.FearfulPhantom;
import de.eldoria.bloodnight.specialmobs.mobs.phantom.FirePhantom;
import de.eldoria.bloodnight.specialmobs.mobs.phantom.PhantomSoul;
import de.eldoria.bloodnight.specialmobs.mobs.skeleton.InvisibleSkeleton;
import de.eldoria.bloodnight.specialmobs.mobs.skeleton.MagicSkeleton;
import de.eldoria.bloodnight.specialmobs.mobs.slime.ToxicSlime;
import de.eldoria.bloodnight.specialmobs.mobs.spider.BlazeRider;
import de.eldoria.bloodnight.specialmobs.mobs.spider.SpeedSkeletonRider;
import de.eldoria.bloodnight.specialmobs.mobs.spider.WitherSkeletonRider;
import de.eldoria.bloodnight.specialmobs.mobs.witch.FireWizard;
import de.eldoria.bloodnight.specialmobs.mobs.witch.ThunderWizard;
import de.eldoria.bloodnight.specialmobs.mobs.witch.WitherWizard;
import de.eldoria.bloodnight.specialmobs.mobs.zombie.ArmoredZombie;
import de.eldoria.bloodnight.specialmobs.mobs.zombie.InvisibleZombie;
import de.eldoria.bloodnight.specialmobs.mobs.zombie.KnightZombie;
import de.eldoria.bloodnight.specialmobs.mobs.zombie.SpeedZombie;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Spider;
import org.bukkit.entity.Witch;
import org.bukkit.entity.Zombie;

import java.util.Collections;
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

    static {
        /*
        Initialize default mobs.
         */
        registerMob(EntityType.CREEPER, EnderCreeper.class, e -> new EnderCreeper((Creeper) e));
        registerMob(EntityType.CREEPER, GhostCreeper.class, e -> new GhostCreeper((Creeper) e));
        registerMob(EntityType.CREEPER, NervousPoweredCreeper.class, e -> new NervousPoweredCreeper((Creeper) e));
        registerMob(EntityType.CREEPER, SpeedCreeper.class, e -> new SpeedCreeper((Creeper) e));
        registerMob(EntityType.CREEPER, ToxicCreeper.class, e -> new ToxicCreeper((Creeper) e));
        registerMob(EntityType.CREEPER, UnstableCreeper.class, e -> new UnstableCreeper((Creeper) e));

        // Enderman
        registerMob(EntityType.ENDERMAN, FearfulEnderman.class, e -> new FearfulEnderman((Enderman) e));
        registerMob(EntityType.ENDERMAN, ToxicEnderman.class, e -> new ToxicEnderman((Enderman) e));

        // Phantom
        registerMob(EntityType.PHANTOM, FearfulPhantom.class, e -> new FearfulPhantom((Phantom) e));
        registerMob(EntityType.PHANTOM, FirePhantom.class, e -> new FirePhantom((Phantom) e));
        registerMob(EntityType.PHANTOM, PhantomSoul.class, e -> new PhantomSoul((Phantom) e));

        // Rider
        registerMob(EntityType.SPIDER, BlazeRider.class, e -> new BlazeRider((Spider) e));
        registerMob(EntityType.SPIDER, SpeedSkeletonRider.class, e -> new SpeedSkeletonRider((Spider) e));
        registerMob(EntityType.SPIDER, WitherSkeletonRider.class, e -> new WitherSkeletonRider((Spider) e));

        // Skeleton
        registerMob(EntityType.SKELETON, InvisibleSkeleton.class, e -> new InvisibleSkeleton((Skeleton) e));
        registerMob(EntityType.SKELETON, MagicSkeleton.class, e -> new MagicSkeleton((Skeleton) e));

        // Slime
        registerMob(EntityType.SLIME, ToxicSlime.class, e -> new ToxicSlime((Slime) e));

        // Witch
        registerMob(EntityType.WITCH, FireWizard.class, e -> new FireWizard((Witch) e));
        registerMob(EntityType.WITCH, ThunderWizard.class, e -> new ThunderWizard((Witch) e));
        registerMob(EntityType.WITCH, WitherWizard.class, e -> new WitherWizard((Witch) e));

        // Zombie
        registerMob(EntityType.ZOMBIE, ArmoredZombie.class, e -> new ArmoredZombie((Zombie) e));
        registerMob(EntityType.ZOMBIE, InvisibleZombie.class, e -> new InvisibleZombie((Zombie) e));
        registerMob(EntityType.ZOMBIE, SpeedZombie.class, e -> new SpeedZombie((Zombie) e));
        registerMob(EntityType.ZOMBIE, KnightZombie.class, e -> new KnightZombie((Zombie) e));

    }

    private SpecialMobRegistry() {
    }

    public static <T extends SpecialMob<?>> void registerMob(EntityType entityType, Class<T> clazz, Function<LivingEntity, SpecialMob<?>> factory) {
        Class<? extends Entity> entityClazz = entityType.getEntityClass();

        MOB_GROUPS.computeIfAbsent(entityClazz, k -> new MobGroup(entityType)).registerFactory(clazz, factory);
    }

    public static Optional<Class<? extends Entity>> getMobBaseClass(Entity entity) {
        if (entity.getType() == EntityType.UNKNOWN) return Optional.empty();

        Class<? extends Entity> mob = ENTITY_MAPPINGS.computeIfAbsent(entity.getType().getEntityClass(), k -> {
            for (Class<? extends Entity> clazz : MOB_GROUPS.keySet()) {
                if (clazz.isAssignableFrom(k)) {
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

    public static Optional<MobGroup> getMobGroup(String name) {
        return MOB_GROUPS.entrySet().stream()
                .filter(e -> e.getKey().getSimpleName().equalsIgnoreCase(name))
                .map(Map.Entry::getValue)
                .findFirst();
    }

    public static Set<MobFactory> getRegisteredMobs() {
        Set<MobFactory> wrappers = new HashSet<>();
        for (MobGroup value : MOB_GROUPS.values()) {
            wrappers.addAll(value.getFactories());
        }
        return wrappers;
    }

    /**
     * Get a read only map of registered mob groups
     *
     * @return registered mob groups
     */
    public static Map<Class<? extends Entity>, MobGroup> getMobGroups() {
        return Collections.unmodifiableMap(MOB_GROUPS);
    }

    public static Optional<MobFactory> getMobFactoryByName(String arg) {
        return getRegisteredMobs().stream().filter(f -> f.getMobName().equalsIgnoreCase(arg)).findFirst();
    }
}
