package de.eldoria.bloodnight.core.mobfactory;

import de.eldoria.bloodnight.config.worldsettings.mobsettings.MobSetting;
import de.eldoria.bloodnight.config.worldsettings.mobsettings.MobSettings;
import de.eldoria.bloodnight.core.BloodNight;
import de.eldoria.bloodnight.specialmobs.SpecialMob;
import de.eldoria.bloodnight.specialmobs.SpecialMobUtil;
import de.eldoria.eldoutilities.localization.ILocalizer;
import de.eldoria.eldoutilities.utils.AttributeUtil;
import lombok.Getter;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.function.Function;

@Getter
public final class MobFactory {
    private final Function<LivingEntity, SpecialMob<?>> factory;
    private final String mobName;
    private final EntityType entityType;

    public MobFactory(EntityType entityType, Class<? extends SpecialMob<?>> clazz, Function<LivingEntity, SpecialMob<?>> factory) {
        this.entityType = entityType;
        this.factory = factory;
        this.mobName = clazz.getSimpleName();
    }

    public SpecialMob<?> wrap(LivingEntity entity, MobSettings mobSettings, MobSetting mobSetting) {
        SpecialMobUtil.tagSpecialMob(entity);
        applySettings(entity, mobSettings, mobSetting);
        return factory.apply(entity);
    }

    private void applySettings(LivingEntity entity, MobSettings mobSettings, MobSetting mobSetting) {
        AttributeInstance damage = entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
        AttributeUtil.setAttributeValue(entity, damage.getAttribute(), Math.min(mobSetting.applyDamage(damage.getValue(), mobSettings.getDamageMultiplier()), 2048));
        AttributeInstance health = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        AttributeUtil.setAttributeValue(entity, health.getAttribute(), Math.min(mobSetting.applyHealth(health.getValue(), mobSettings.getHealthModifier()), 2048));
        SpecialMobUtil.setSpecialMobType(entity, mobSetting.getMobName());
        entity.setHealth(health.getValue());
        String displayName = mobSetting.getDisplayName();
        if (displayName.trim().isEmpty()) {
            displayName = ILocalizer.getPluginLocalizer(BloodNight.class).getMessage("mob." + mobSetting.getMobName());
        }
        entity.setCustomName(displayName);
        entity.setCustomNameVisible(mobSettings.isDisplayMobNames());
    }
}
