package de.eldoria.bloodnight.bloodmob.settings.mobsettings;

import de.eldoria.bloodnight.bloodmob.settings.mobsettings.setting.CreeperSettings;
import de.eldoria.bloodnight.bloodmob.settings.mobsettings.setting.PhantomSetting;
import de.eldoria.bloodnight.bloodmob.settings.mobsettings.setting.RabbitSettings;
import de.eldoria.bloodnight.bloodmob.settings.mobsettings.setting.SlimeSetting;
import org.bukkit.entity.EntityType;

public enum BloodMobType {
    BAT(EntityType.BAT, TypeSetting.class),
    BLAZE(EntityType.BLAZE, TypeSetting.class),
    CAVE_SPIDER(EntityType.CAVE_SPIDER, TypeSetting.class),
    CREEPER(EntityType.CREEPER, CreeperSettings.class),
    DROWNED(EntityType.DROWNED, TypeSetting.class),
    ELDER_GUARDIAN(EntityType.ELDER_GUARDIAN, TypeSetting.class),
    ENDERMAN(EntityType.ENDERMAN, TypeSetting.class),
    ENDERMITE(EntityType.ENDERMITE, TypeSetting.class),
    EVOKER(EntityType.EVOKER, TypeSetting.class),
    GHAST(EntityType.GHAST, TypeSetting.class),
    GIANT(EntityType.GIANT, TypeSetting.class),
    GUARDIAN(EntityType.GUARDIAN, TypeSetting.class),
    HOGLIN(EntityType.HOGLIN, TypeSetting.class),
    HUSK(EntityType.HUSK, TypeSetting.class),
    MAGMA_CUBE(EntityType.MAGMA_CUBE, SlimeSetting.class),
    PHANTOM(EntityType.PHANTOM, PhantomSetting.class),
    PIGLIN(EntityType.PIGLIN, TypeSetting.class),
    PIGLIN_BRUTE(EntityType.PIGLIN_BRUTE, TypeSetting.class),
    RABBIT(EntityType.RABBIT, RabbitSettings.class),
    SILVERFISH(EntityType.SILVERFISH, TypeSetting.class),
    SKELETON(EntityType.SKELETON, TypeSetting.class),
    SLIME(EntityType.SLIME, SlimeSetting.class),
    SPIDER(EntityType.SPIDER, TypeSetting.class),
    STRAY(EntityType.STRAY, TypeSetting.class),
    STRIDER(EntityType.STRIDER, TypeSetting.class),
    VEX(EntityType.VEX, TypeSetting.class),
    WITCH(EntityType.WITCH, TypeSetting.class),
    WITHER(EntityType.WITHER, TypeSetting.class),
    WITHER_SKELETON(EntityType.WITHER_SKELETON, TypeSetting.class),
    WOLF(EntityType.WOLF, TypeSetting.class),
    ZOGLIN(EntityType.ZOGLIN, TypeSetting.class),
    ZOMBIE(EntityType.ZOMBIE, TypeSetting.class),
    ZOMBIE_VILLAGER(EntityType.ZOMBIE_VILLAGER, TypeSetting.class),
    ZOMBIFIED_PIGLIN(EntityType.ZOMBIFIED_PIGLIN, TypeSetting.class);

    private final EntityType entityType;

    private final Class<? extends TypeSetting> typeSettingClazz;

    BloodMobType(EntityType entityType, Class<? extends TypeSetting> typeSettingClazz) {
        this.entityType = entityType;
        this.typeSettingClazz = typeSettingClazz;
    }

    public EntityType entityType() {
        return entityType;
    }

    public Class<? extends TypeSetting> typeSettingClazz() {
        return typeSettingClazz;
    }
}
