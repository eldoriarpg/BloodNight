package de.eldoria.bloodnight.core.mobfactory;

import de.eldoria.bloodnight.specialmobs.SpecialMob;
import de.eldoria.bloodnight.specialmobs.SpecialMobUtil;
import lombok.Getter;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.function.Function;

@Getter
public final class MobFactory {
    private final Function<LivingEntity, SpecialMob> factory;
    private final String displayName;
    private final String mobName;
    private final EntityType entityType;

    public MobFactory(EntityType entityType, String displayName, Function<LivingEntity, SpecialMob> factory) {
        this.entityType = entityType;
        this.factory = factory;
        this.displayName = displayName;
        this.mobName = displayName.toLowerCase().replace(" ", "");
    }

    public SpecialMob wrap(LivingEntity entity) {
        SpecialMobUtil.tagEntity(entity);
        return factory.apply(entity);
    }
}
