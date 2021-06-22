package de.eldoria.bloodnight.bloodmob;

import de.eldoria.bloodnight.bloodmob.registry.items.ItemRegistry;
import de.eldoria.bloodnight.bloodmob.settings.*;
import de.eldoria.bloodnight.bloodmob.settings.mobsettings.BloodMobType;
import de.eldoria.bloodnight.bloodmob.settings.mobsettings.TypeSetting;
import de.eldoria.bloodnight.bloodmob.utils.BloodMobUtil;
import de.eldoria.bloodnight.core.ABloodNight;
import de.eldoria.eldoutilities.core.EldoUtilities;
import de.eldoria.eldoutilities.localization.ILocalizer;
import de.eldoria.eldoutilities.utils.AttributeUtil;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.event.entity.*;

import java.time.Instant;
import java.util.Optional;

public class BloodMob implements IBloodMob {
    private final Mob base;
    private final Mob extension;
    private final MobConfiguration mobConfiguration;
    private final Behaviour behaviour;
    private Instant lastDamage = Instant.now();
    private final ItemRegistry itemRegistry;

    private BloodMob(Mob base, Mob extension, MobConfiguration mobConfiguration, ItemRegistry itemRegistry) {
        this.base = base;
        this.extension = extension;
        this.mobConfiguration = mobConfiguration;
        behaviour = mobConfiguration.behaviour();
        this.itemRegistry = itemRegistry;
    }

    public static BloodMob construct(Mob base, WorldMobSettings mobSettings, MobConfiguration mobConfiguration, ItemRegistry itemRegistry) {
        // tag mob as special mob
        BloodMobUtil.tagSpecialMob(base);
        // set attributes
        AttributeInstance damage = base.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
        AttributeUtil.setAttributeValue(base, damage.getAttribute(), Math.min(mobConfiguration.stats().applyDamage(damage.getValue(), mobSettings.damageMultiplier()), 2048));
        AttributeInstance health = base.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        AttributeUtil.setAttributeValue(base, health.getAttribute(), Math.min(mobConfiguration.stats().applyHealth(health.getValue(), mobSettings.healthModifier()), 2048));
        base.setHealth(health.getValue());
        // flag with special mob type
        BloodMobUtil.setSpecialMobType(base, mobConfiguration.identifier());
        // set display name
        Optional<BloodMobType> bloodMobType = BloodMobType.fromEntityType(base.getType());
        if(!bloodMobType.isPresent()) return null;
        TypeSetting typeSetting = mobConfiguration.wrapTypes().get(bloodMobType.get());
        String name = typeSetting.name();
        base.setCustomName(name);
        base.setCustomNameVisible(mobSettings.isdisplayMobNames());
        Equipment equipment = mobConfiguration.equipment();
        if (equipment != null) {
            equipment.apply(base, itemRegistry);
        }

        // build extension if required
        if (mobConfiguration.isExtended()) {
            Extension extentionSettings = mobConfiguration.extension();
            // spawn and mount extension
            Mob extension = addExtension(base, extentionSettings);
            // sync names
            extension.setCustomName(base.getCustomName());
            extension.setCustomNameVisible(base.isCustomNameVisible());

            // sync attributes
            AttributeUtil.syncAttributeValue(base, extension, Attribute.GENERIC_ATTACK_DAMAGE);
            AttributeUtil.syncAttributeValue(base, extension, Attribute.GENERIC_MAX_HEALTH);

            extension.setInvulnerable(extentionSettings.isInvulnerable());
            extension.setInvisible(extentionSettings.isInvisible());
            if (extentionSettings.isClearEquipment()) {
                EldoUtilities.getDelayedActions().schedule(() -> BloodMobUtil.clearEquipment(extension), 2);
            }
            equipment = extentionSettings.equipment();
            if (equipment != null) {
                equipment.apply(extension, itemRegistry);
            }
            return new BloodMob(base, extension, mobConfiguration, itemRegistry);
        }

        return new BloodMob(base, null, mobConfiguration, itemRegistry);
    }

    private static Mob addExtension(Mob base, Extension ext) {
        switch (ext.extensionRole()) {
            case CARRIER:
                return BloodMobUtil.spawnAndMount(ext.extensionType().entityType(), base);
            case PASSENGER:
                return BloodMobUtil.spawnAndMount(base, ext.extensionType().entityType());
            default:
                throw new IllegalStateException("Unexpected value: " + ext.extensionRole());
        }
    }

    @Override
    public void tick() {

    }

    @Override
    public void onEnd() {
    }

    @Override
    public void onTeleport(EntityTeleportEvent event) {
    }

    @Override
    public void onProjectileShoot(ProjectileLaunchEvent event) {
    }

    @Override
    public void onProjectileHit(ProjectileHitEvent event) {
    }

    @Override
    public void onDeath(EntityDeathEvent event) {
        if (isExtended()) {
            extension.damage(extension.getHealth(), base);
        }
    }

    @Override
    public void onKill(EntityDeathEvent event) {

    }

    @Override
    public void onExplosionPrimeEvent(ExplosionPrimeEvent event) {

    }

    @Override
    public void onExplosionEvent(EntityExplodeEvent event) {

    }

    @Override
    public void onTargetEvent(EntityTargetEvent event) {
        if (isExtended()) {
            if (event.getTarget() == null) {
                extension.setTarget(null);
                return;
            }

            if (event.getTarget() instanceof LivingEntity) {
                extension.setTarget((LivingEntity) event.getTarget());
            }
        }
    }

    @Override
    public void onDamage(EntityDamageEvent event) {
        BloodMobUtil.handleExtendedEntityDamage(getBase(), getExtension(), event);
    }

    @Override
    public void onDamageByEntity(EntityDamageByEntityEvent event) {
        lastDamage = Instant.now();
    }

    @Override
    public void onHit(EntityDamageByEntityEvent event) {

    }

    @Override
    public void remove() {
        if (base.isValid()) {
            base.remove();
        }
        if (isExtended() && extension.isValid()) {
            extension.remove();
        }
    }

    @Override
    public void onExtensionDamage(EntityDamageEvent event) {
        if (isExtended()) {
            BloodMobUtil.handleExtendedEntityDamage(extension, base, event);
        }
    }

    @Override
    public void onExtensionDeath(EntityDeathEvent event) {
        if (isExtended()) {
            getBase().damage(getBase().getHealth(), event.getEntity().getKiller());
        }
    }

    @Override
    public boolean isValid() {
        if (isExtended()) {
            return base.isValid() && extension.isValid() && !base.getPassengers().isEmpty();
        }
        return base.isValid();
    }

    @Override
    public Mob getBase() {
        return base;
    }

    @Override
    public Mob getExtension() {
        return extension;
    }

    @Override
    public boolean isExtended() {
        return extension != null;
    }

    /**
     * Get the last time a special mob received damage.
     *
     * @return last damage time as instant.
     */
    @Override
    public Instant lastDamage() {
        return lastDamage;
    }
}
