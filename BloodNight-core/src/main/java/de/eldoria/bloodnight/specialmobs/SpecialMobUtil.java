package de.eldoria.bloodnight.specialmobs;

import de.eldoria.bloodnight.config.worldsettings.deathactions.subsettings.LightningSettings;
import de.eldoria.bloodnight.config.worldsettings.deathactions.subsettings.ShockwaveSettings;
import de.eldoria.bloodnight.core.BloodNight;
import de.eldoria.bloodnight.specialmobs.effects.ParticleCloud;
import de.eldoria.bloodnight.specialmobs.effects.PotionCloud;
import de.eldoria.bloodnight.util.VectorUtil;
import de.eldoria.eldoutilities.serialization.TypeConversion;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class SpecialMobUtil {
	private static final NamespacedKey IS_SPECIAL_MOB = BloodNight.getNamespacedKey("isSpecialMob");
	private static final NamespacedKey IS_MOB_EXTENSION = BloodNight.getNamespacedKey("isMobExtension");
	private static final NamespacedKey BASE_UUID = BloodNight.getNamespacedKey("baseUUID");
	private static final NamespacedKey MOB_TYPE = BloodNight.getNamespacedKey("mobType");

	private SpecialMobUtil() {
	}

	@Deprecated
	public static void spawnLingeringPotionAt(Location location, PotionEffect potionEffect) {
		spawnPotionAt(location, potionEffect, Material.LINGERING_POTION);
	}

	@Deprecated
	public static void spawnPotionAt(Location location, PotionEffect potionEffect, Material potionType) {
		ItemStack potion = new ItemStack(potionType);
		PotionMeta potionMeta = (PotionMeta) potion.getItemMeta();
		potionMeta.addCustomEffect(potionEffect, false);
		potion.setItemMeta(potionMeta);

		LivingEntity entity = (LivingEntity) location.getWorld().spawnEntity(location.add(0, 1, 0), EntityType.CHICKEN);
		ThrownPotion thrownPotion = entity.launchProjectile(ThrownPotion.class, new Vector(0, -4, 0));
		thrownPotion.setItem(potion);
		entity.remove();
	}

	/**
	 * Adds a simple potion effect to a entitiy.
	 *
	 * @param entity    entity to add potion effect
	 * @param type      type of potion effect
	 * @param amplifier amplifier
	 * @param visible   true if particles should be visible
	 */
	public static void addPotionEffect(LivingEntity entity, PotionEffectType type, int amplifier, boolean visible) {
		entity.addPotionEffect(new PotionEffect(type, 60 * 20, amplifier, visible, visible));
	}

	/**
	 * Spawns particle around a entity.
	 *
	 * @param entity   entity as center
	 * @param particle particle to spawn
	 * @param amount   amount to spawn
	 */
	public static void spawnParticlesAround(Entity entity, Particle particle, int amount) {
		spawnParticlesAround(entity.getLocation(), particle, amount);
	}

	/**
	 * Spawns particle around a location.
	 *
	 * @param location location as center
	 * @param particle particle to spawn
	 * @param amount   amount to spawn
	 */
	public static void spawnParticlesAround(Location location, Particle particle, int amount) {
		spawnParticlesAround(location, particle, null, amount);
	}

	/**
	 * Spawns particle around a location.
	 *
	 * @param location location as center
	 * @param particle particle to spawn
	 * @param data     data which may be required for spawning the particle
	 * @param amount   amount to spawn
	 * @param <T>      type of date
	 */
	public static <T> void spawnParticlesAround(Location location, Particle particle, T data, int amount) {
		World world = location.getWorld();
		assert world != null;
		ThreadLocalRandom rand = ThreadLocalRandom.current();
		for (int i = 0; i < amount; i++) {
			if (data != null) {
				world.spawnParticle(particle,
						location.clone()
								.add(
										rand.nextDouble(-3, 3),
										rand.nextDouble(0, 3),
										rand.nextDouble(-3, 3)),
						1, data);
			} else {
				world.spawnParticle(particle,
						location.clone()
								.add(
										rand.nextDouble(-3, 3),
										rand.nextDouble(0, 3),
										rand.nextDouble(-3, 3)),
						1);
			}
		}
	}

	/**
	 * Launches a projectile on the current target of the entity.
	 *
	 * @param source     source of the projectile.
	 * @param projectile projectile type
	 * @param speed      projectile speed
	 * @param <T>        type of projectile
	 *
	 * @return projectile or null if target is null
	 */
	public static <T extends Projectile> T launchProjectileOnTarget(Mob source, Class<T> projectile, double speed) {
		return launchProjectileOnTarget(source, source.getTarget(), projectile, speed);
	}

	/**
	 * Launches a projectile.
	 *
	 * @param source     source of the projectile.
	 * @param target     target of the projectile.
	 * @param projectile projectile type
	 * @param speed      projectile speed
	 * @param <T>        type of projectile
	 *
	 * @return projectile or null if target is null
	 */
	public static <T extends Projectile> T launchProjectileOnTarget(Mob source, Entity target, Class<T> projectile, double speed) {
		if (target != null) {
			Vector vel = VectorUtil.getDirectionVector(source.getLocation(), target.getLocation())
					.normalize()
					.multiply(speed);
			return source.launchProjectile(projectile, vel);
		}
		return null;
	}

	/**
	 * Spawn and mount a entity as a passenger
	 *
	 * @param passengerType type of passenger
	 * @param carrier       carrier where the passenger should be mounted on
	 * @param <T>           type of carrier
	 *
	 * @return spawned passenger which is already mounted.
	 */
	public static <T extends Entity> T spawnAndMount(Entity carrier, EntityType passengerType) {
		T passenger = spawnAndTagEntity(carrier.getLocation(), passengerType);
		assert passenger == null;
		tagExtension(passenger, carrier);
		carrier.addPassenger(passenger);
		return passenger;
	}

	/**
	 * Spawn and mount a entity on a carrier
	 *
	 * @param carrierType type of carrier
	 * @param rider       rider to mount
	 * @param <T>         type of carrier
	 *
	 * @return spawned carrier with the rider mounted.
	 */
	public static <T extends Entity> T spawnAndMount(EntityType carrierType, Entity rider) {
		T carrier = spawnAndTagEntity(rider.getLocation(), carrierType);
		assert carrier == null;
		tagExtension(carrier, rider);
		carrier.addPassenger(rider);
		return carrier;
	}

	/**
	 * Spawns a new entity and tags it as special mob.
	 *
	 * @param location   location of the new entity
	 * @param entityType type of the entity
	 * @param <T>        type of the entity
	 *
	 * @return spawned entity of type
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Entity> T spawnAndTagEntity(Location location, EntityType entityType) {
		Entity entity = location.getWorld().spawnEntity(location, entityType);
		assert entity == null;
		tagSpecialMob(entity);
		return (T) entity;
	}

	/**
	 * Marks a entity as a special mob extension
	 *
	 * @param entity   entity to mark
	 * @param extended the entity which is extended
	 */
	public static void tagExtension(Entity entity, Entity extended) {
		PersistentDataContainer dataContainer = entity.getPersistentDataContainer();
		dataContainer.set(IS_MOB_EXTENSION, PersistentDataType.BYTE, (byte) 1);
		dataContainer.set(BASE_UUID, PersistentDataType.BYTE_ARRAY,
				TypeConversion.getBytesFromUUID(extended.getUniqueId()));
	}

	/**
	 * Checks if a entity is a extension of a special mob.
	 *
	 * @param entity entity to check
	 *
	 * @return true if the entity is a extension
	 */
	public static boolean isExtension(Entity entity) {
		PersistentDataContainer dataContainer = entity.getPersistentDataContainer();
		if (dataContainer.has(IS_MOB_EXTENSION, PersistentDataType.BYTE)) {
			Byte specialMob = dataContainer.get(IS_MOB_EXTENSION, PersistentDataType.BYTE);
			return specialMob != null && specialMob == (byte) 1;
		}
		return false;
	}

	/**
	 * Get the UUID of the base mob. This will only return a UUID if {@link #isExtension(Entity)} returns true
	 *
	 * @param entity entity to check
	 *
	 * @return returns a uuid if the mob is a extension.
	 */
	public static Optional<UUID> getBaseUUID(Entity entity) {
		PersistentDataContainer dataContainer = entity.getPersistentDataContainer();
		if (dataContainer.has(IS_MOB_EXTENSION, PersistentDataType.BYTE)) {
			byte[] specialMob = dataContainer.get(BASE_UUID, PersistentDataType.BYTE_ARRAY);
			return Optional.of(TypeConversion.getUUIDFromBytes(specialMob));
		}
		return Optional.empty();
	}

	/**
	 * Set the special mob type.
	 *
	 * @param entity entity to set
	 * @param type   type to set
	 */
	public static void setSpecialMobType(Entity entity, String type) {
		PersistentDataContainer dataContainer = entity.getPersistentDataContainer();
		dataContainer.set(MOB_TYPE, PersistentDataType.STRING, type);
	}

	/**
	 * Get the Special Mob type
	 *
	 * @param entity entity to check
	 *
	 * @return optional string with the mob type or a empty optional
	 */
	public static Optional<String> getSpecialMobType(Entity entity) {
		PersistentDataContainer dataContainer = entity.getPersistentDataContainer();
		if (dataContainer.has(MOB_TYPE, PersistentDataType.STRING)) {
			return Optional.ofNullable(dataContainer.get(MOB_TYPE, PersistentDataType.STRING));
		}
		return Optional.empty();
	}

	/**
	 * Tags an entity as special mob.
	 * <p>This also sets {@link Entity#setPersistent(boolean)} to {@code false}
	 * and {@link LivingEntity#setRemoveWhenFarAway(boolean)} to {@code true}
	 *
	 * @param entity entity to tag
	 */
	public static void tagSpecialMob(Entity entity) {
		PersistentDataContainer dataContainer = entity.getPersistentDataContainer();
		dataContainer.set(IS_SPECIAL_MOB, PersistentDataType.BYTE, (byte) 1);
		if (entity instanceof LivingEntity) {
			((LivingEntity) entity).setRemoveWhenFarAway(true);
		}
		entity.setPersistent(false);
	}

	/**
	 * Checks if a mob is a special mob
	 *
	 * @param entity entity to check
	 *
	 * @return true if the mob is a special mob
	 */
	public static boolean isSpecialMob(Entity entity) {
		PersistentDataContainer dataContainer = entity.getPersistentDataContainer();
		if (dataContainer.has(IS_SPECIAL_MOB, PersistentDataType.BYTE)) {
			Byte specialMob = dataContainer.get(IS_SPECIAL_MOB, PersistentDataType.BYTE);
			return specialMob != null && specialMob == (byte) 1;
		}
		return false;
	}

	/**
	 * Handles the damage which was dealt to one entity to the extension or base.
	 *
	 * @param receiver receiver of the damage.
	 * @param other    the other part of the mob.
	 * @param event    the damage event
	 */
	public static void handleExtendedEntityDamage(LivingEntity receiver, LivingEntity other, EntityDamageEvent event) {
		if (event instanceof EntityDamageByEntityEvent) {
			EntityDamageByEntityEvent entityDamage = (EntityDamageByEntityEvent) event;
			if (entityDamage.getDamager().getUniqueId() == other.getUniqueId()) {
				return;
			}
		}

		if (receiver.getHealth() == 0) return;

		double newHealth = Math.max(0, other.getHealth() - event.getFinalDamage());
		if (newHealth == 0) {
			other.damage(event.getFinalDamage(), event.getEntity());
			return;
		}
		other.setHealth(newHealth);
		other.playEffect(EntityEffect.HURT);
	}

	/**
	 * Dispatch a shockwave at a location when probability is given.
	 *
	 * @param settings shockwave settings
	 * @param location location of shockwave
	 */
	public static void dispatchShockwave(ShockwaveSettings settings, Location location) {
		if (settings.getShockwaveProbability() < ThreadLocalRandom.current().nextInt(101)
				|| settings.getShockwaveProbability() == 0) return;

		for (Entity entity : getEntitiesAround(location, settings.getShockwaveRange())) {
			Vector directionVector = VectorUtil.getDirectionVector(location, entity.getLocation());
			double power = settings.getPower(directionVector);
			entity.setVelocity(directionVector.normalize().multiply(power));
			settings.applyEffects(entity);
		}
	}

	public static void dispatchLightning(LightningSettings settings, Location location) {
		if (location.getWorld() == null) return;
		if (settings.isDoLightning() && settings.getLightning() != 0) {
			if (ThreadLocalRandom.current().nextInt(101) <= settings.getLightning()) {
				location.getWorld().strikeLightningEffect(location.clone().add(0, 20, 0));
				return;
			}
		}

		if (settings.isDoThunder() && settings.getThunder() != 0) {
			if (ThreadLocalRandom.current().nextInt(101) <= settings.getThunder()) {
				location.getWorld().getPlayers().forEach(p ->
						p.playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.WEATHER, 1, 1));
			}
		}
	}

	public static Collection<Entity> getEntitiesAround(Location location, double range) {
		if (location.getWorld() == null) return Collections.emptyList();
		return location.getWorld().getNearbyEntities(location, range, range, range);
	}

	/**
	 * Builds a particle cloud and binds it to an entity.
	 * <p>
	 * In the most cases {@link #spawnParticlesAround(Entity, Particle, int)} will result in a better result.
	 *
	 * @param target target to which the particle cloud should be bound.
	 *
	 * @return builder
	 */
	public static ParticleCloud.Builder buildParticleCloud(LivingEntity target) {
		return ParticleCloud.builder(target);
	}

	/**
	 * Build a particle cloud which is bound to a entity
	 *
	 * @param target entity which will be followed by the cloud
	 *
	 * @return builder
	 */
	public static PotionCloud.Builder buildPotionCloud(LivingEntity target) {
		return PotionCloud.builder(target);
	}

	/**
	 * Build a particle cloud at a specific location.
	 *
	 * @param location location where the cloud should be created
	 *
	 * @return builder
	 */
	public static PotionCloud.Builder buildParticleCloud(Location location) {
		return PotionCloud.builder(location);
	}
}
