package de.eldoria.bloodnight.core.mobfactory;

import de.eldoria.bloodnight.config.worldsettings.mobsettings.MobSetting;
import de.eldoria.bloodnight.config.worldsettings.mobsettings.MobSettings;
import de.eldoria.bloodnight.core.BloodNight;
import de.eldoria.bloodnight.specialmobs.SpecialMob;
import de.eldoria.bloodnight.specialmobs.SpecialMobUtil;
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
        SpecialMobUtil.tagEntity(entity);
        applySettings(entity, mobSettings, mobSetting);
        return factory.apply(entity);
    }

    private void applySettings(LivingEntity entity, MobSettings mobSettings, MobSetting mobSetting) {
        AttributeInstance damage = entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
        setNewBase(damage, Math.min(mobSetting.applyDamage(damage.getValue(), mobSettings.getMonsterDamageMultiplier()), 2048));
        AttributeInstance health = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        setNewBase(health, Math.min(mobSetting.applyHealth(health.getValue(), mobSettings.getMonsterHealthModifier()), 2048));
        entity.setHealth(health.getValue());
        entity.setCustomName(mobSetting.getDisplayName());
        entity.setCustomNameVisible(mobSettings.isDisplayMobNames());
    }

    private void setNewBase(AttributeInstance attribute, double target) {
        attribute.setBaseValue(target / (attribute.getValue() / attribute.getBaseValue()));
    }
}
