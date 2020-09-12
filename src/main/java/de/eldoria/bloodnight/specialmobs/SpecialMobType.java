package de.eldoria.bloodnight.specialmobs;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum SpecialMobType {
    /**
     * Creeper with speed 4 and faster explosion
     */
    SPEED_CREEPER(EntityType.CREEPER),
    /**
     * Creeper which leaves a poisinous field after explosion
     */
    TOXIC_CREEPER(EntityType.CREEPER),
    /**
     * Powered creeper with faster explosion.
     */
    NERVOUS_POWERED_CREEPER(EntityType.CREEPER),
    /**
     * Explodes when on fire.
     */
    UNSTABLE_CREEPER(EntityType.CREEPER),
    /**
     * Creeper on a invisible bat.
     */
    FLYING_CREEPER(EntityType.CREEPER),
    /**
     * Creeper which can teleport to its target every 10 seconds.
     */
    ENDER_CREEPER(EntityType.CREEPER),
    /**
     * A wither skeleton riding a cave spider
     */
    WITHER_SKELETON_RIDER(EntityType.SPIDER),
    /**
     * A zombie with speed 4
     */
    SPEED_ZOMBIE(EntityType.ZOMBIE),
    /**
     * A zombie with a sword and invisbility effect
     */
    INVISIBLE_ZOMBIE(EntityType.ZOMBIE),
    /**
     * A witch throwing fireballs instead of potions.
     */
    FIRE_WIZARD(EntityType.WITCH),
    /**
     * A witch throwing lightnings
     */
    THUNDER_WIZARD(EntityType.WITCH),
    /**
     * A witch applying wither effect on players.
     */
    WITHER_WIZARD(EntityType.WITCH),
    /**
     * A skeleton with tipped arrows.
     */
    MAGIC_SKELETON(EntityType.SKELETON),
    /**
     * A skeleton with invisibility
     */
    INVISIBLE_SKELETON(EntityType.SKELETON),
    // Enderman are always angry.
    /**
     * A enderman which leaves a cloud of poision after teleport.
     */
    TOXIC_ENDERMAN(EntityType.ENDERMAN),
    /**
     * Applies blindness effect hit.
     */
    FEARFULL_ENDERMAN(EntityType.ENDERMAN),
    /**
     * A skeleton on a fast spider.
     */
    SPEED_SKELETON_RIDER(EntityType.SPIDER),
    /**
     * A slime which explodes in a cloud of poison when killed.
     */
    POISINOUS_SLIME(EntityType.SLIME),
    /**
     * A phantom with invisibility and glowing effect.
     */
    PHANTOM_SOUL(EntityType.PHANTOM),
    /**
     * Applies a slowness effect on hit.
     */
    FEARFULL_PHANTOM(EntityType.PHANTOM);

    private final EntityType type;

    SpecialMobType(EntityType type) {
        this.type = type;
    }

    /**
     * Get a matching special mob for a entity.
     *
     * @param entity Entity to get a special mob
     * @return List of matchin entities for this entity. May be empty.
     */
    private static List<SpecialMobType> getMatchingTypes(Entity entity) {
        return Arrays.stream(values()).filter(t -> t.type == entity.getType()).collect(Collectors.toList());
    }

    public EntityType getType() {
        return type;
    }
}
