package de.eldoria.bloodnight.specialmobs;

import de.eldoria.bloodnight.specialmobs.mobs.creeper.EnderCreeper;
import de.eldoria.bloodnight.specialmobs.mobs.creeper.FlyingCreeper;
import de.eldoria.bloodnight.specialmobs.mobs.creeper.NervousPoweredCreeper;
import de.eldoria.bloodnight.specialmobs.mobs.creeper.SpeedCreeper;
import de.eldoria.bloodnight.specialmobs.mobs.creeper.ToxicCreeper;
import de.eldoria.bloodnight.specialmobs.mobs.creeper.UnstableCreeper;
import de.eldoria.bloodnight.specialmobs.mobs.enderman.FearfullEnderman;
import de.eldoria.bloodnight.specialmobs.mobs.enderman.ToxicEnderman;
import de.eldoria.bloodnight.specialmobs.mobs.phantom.FearfulPhantom;
import de.eldoria.bloodnight.specialmobs.mobs.phantom.FirePhantom;
import de.eldoria.bloodnight.specialmobs.mobs.phantom.PhantomSoul;
import de.eldoria.bloodnight.specialmobs.mobs.rider.BlazeRider;
import de.eldoria.bloodnight.specialmobs.mobs.rider.SpeedSkeletonRider;
import de.eldoria.bloodnight.specialmobs.mobs.rider.WitherSkeletonRider;
import de.eldoria.bloodnight.specialmobs.mobs.skeleton.InvisibleSkeleton;
import de.eldoria.bloodnight.specialmobs.mobs.skeleton.MagicSkeleton;
import de.eldoria.bloodnight.specialmobs.mobs.slime.ToxicSlime;
import de.eldoria.bloodnight.specialmobs.mobs.witch.FireWizard;
import de.eldoria.bloodnight.specialmobs.mobs.witch.ThunderWizard;
import de.eldoria.bloodnight.specialmobs.mobs.witch.WitherWizard;
import de.eldoria.bloodnight.specialmobs.mobs.zombie.ArmoredZombie;
import de.eldoria.bloodnight.specialmobs.mobs.zombie.InvisibleZombie;
import de.eldoria.bloodnight.specialmobs.mobs.zombie.SpeedZombie;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Spider;
import org.bukkit.entity.Witch;
import org.bukkit.entity.Zombie;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum SpecialMobType {
    /**
     * Creeper with speed 4 and faster explosion
     */
    SPEED_CREEPER(EntityType.CREEPER, e -> new SpeedCreeper((Creeper) e)),
    /**
     * Creeper which leaves a poisinous field after explosion
     */
    TOXIC_CREEPER(EntityType.CREEPER, e -> new ToxicCreeper((Creeper) e)),
    /**
     * Powered creeper with faster explosion.
     */
    NERVOUS_POWERED_CREEPER(EntityType.CREEPER, e -> new NervousPoweredCreeper((Creeper) e)),
    /**
     * Explodes when on fire.
     */
    UNSTABLE_CREEPER(EntityType.CREEPER, e -> new UnstableCreeper((Creeper) e)),
    /**
     * Creeper on a invisible bat.
     */
    FLYING_CREEPER(EntityType.CREEPER, e -> new FlyingCreeper((Creeper) e)),
    /**
     * Creeper which can teleport to its target every 10 seconds.
     */
    ENDER_CREEPER(EntityType.CREEPER, e -> new EnderCreeper((Creeper) e)),
    /**
     * A wither skeleton riding a cave spider
     */
    WITHER_SKELETON_RIDER(EntityType.SPIDER, e -> new WitherSkeletonRider((Spider) e)),
    /**
     * A zombie with speed 4
     */
    SPEED_ZOMBIE(EntityType.ZOMBIE, e -> new SpeedZombie((Zombie) e)),
    /**
     * A zombie with a sword and invisbility effect
     */
    INVISIBLE_ZOMBIE(EntityType.ZOMBIE, e -> new InvisibleZombie((Zombie) e)),
    /**
     * A zombie with a full armor.
     */
    ARMORED_ZOMBIE(EntityType.ZOMBIE, e -> new ArmoredZombie((Zombie) e)),
    /**
     * A witch throwing fireballs instead of potions.
     */
    FIRE_WIZARD(EntityType.WITCH, e -> new FireWizard((Witch) e)),
    /**
     * A witch throwing lightnings
     */
    THUNDER_WIZARD(EntityType.WITCH, e -> new ThunderWizard((Witch) e)),
    /**
     * A witch applying wither effect on players.
     */
    WITHER_WIZARD(EntityType.WITCH, e -> new WitherWizard((Witch) e)),
    /**
     * A skeleton with tipped arrows.
     */
    MAGIC_SKELETON(EntityType.SKELETON, e -> new MagicSkeleton((Skeleton) e)),
    /**
     * A skeleton with invisibility
     */
    INVISIBLE_SKELETON(EntityType.SKELETON, e -> new InvisibleSkeleton((Skeleton) e)),
    // Enderman are always angry.
    /**
     * A enderman which leaves a cloud of poision after teleport.
     */
    TOXIC_ENDERMAN(EntityType.ENDERMAN, e -> new ToxicEnderman((Enderman) e)),
    /**
     * Applies blindness effect hit.
     */
    FEARFULL_ENDERMAN(EntityType.ENDERMAN, e -> new FearfullEnderman((Enderman) e)),
    /**
     * A skeleton on a fast spider.
     */
    SPEED_SKELETON_RIDER(EntityType.SPIDER, e -> new SpeedSkeletonRider((Spider) e)),
    /**
     * A slime which explodes in a cloud of poison when killed.
     */
    TOXIC_SLIME(EntityType.SLIME, e -> new ToxicSlime((Slime) e)),
    /**
     * A phantom with invisibility and glowing effect.
     */
    PHANTOM_SOUL(EntityType.PHANTOM, e -> new PhantomSoul((Phantom) e)),
    /**
     * Applies a slowness effect on hit.
     */
    FEARFULL_PHANTOM(EntityType.PHANTOM, e -> new FearfulPhantom((Phantom) e)),
    /**
     * Phantom with a blaze on top.
     */
    FIRE_PHANTOM(EntityType.PHANTOM, e -> new FirePhantom((Phantom) e)),
    /**
     * A spider with a blaze.
     */
    BLAZE_RIDER(EntityType.SPIDER, e -> new BlazeRider((Spider) e));

    private final EntityType baseEntityType;
    private final Function<Entity, SpecialMob> create;

    SpecialMobType(EntityType baseEntityType, Function<Entity, SpecialMob> create) {
        this.baseEntityType = baseEntityType;
        this.create = create;
    }

    /**
     * Creates the requested special mob
     *
     * @param entity base entity for special mob
     * @return SpecialMob
     * @throws ClassCastException when {@link Entity#getType()} is not equals {@link SpecialMobType#getBaseEntityType()} or a child of this type
     */
    public SpecialMob create(Entity entity) throws ClassCastException {
        return create.apply(entity);
    }

    /**
     * Get a matching special mob for a entity.
     *
     * @param entity Entity to get a special mob
     * @return List of matchin entities for this entity. May be empty.
     */
    private static List<SpecialMobType> getMatchingTypes(Entity entity) {
        return Arrays.stream(values()).filter(t -> t.baseEntityType == entity.getType()).collect(Collectors.toList());
    }

    public EntityType getBaseEntityType() {
        return baseEntityType;
    }
}
