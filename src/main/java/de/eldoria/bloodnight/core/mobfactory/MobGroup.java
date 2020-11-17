package de.eldoria.bloodnight.core.mobfactory;

import de.eldoria.bloodnight.specialmobs.SpecialMob;
import lombok.Getter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class MobGroup {
	@Getter
	private final EntityType entityType;
	@Getter
	private final List<MobFactory> factories = new ArrayList<>();

	public MobGroup(EntityType entityType) {
		this.entityType = entityType;
	}

	public Class<? extends Entity> getBaseClass() {
		return entityType.getEntityClass();
	}

	void registerFactory(MobFactory factory) {
		factories.add(factory);
	}

	public void registerFactory(Class<? extends SpecialMob<?>> clazz, Function<LivingEntity, SpecialMob<?>> factory) {
		factories.add(new MobFactory(entityType, clazz, factory));
	}
}
