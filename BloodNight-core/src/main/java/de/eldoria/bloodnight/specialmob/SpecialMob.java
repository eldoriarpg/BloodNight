package de.eldoria.bloodnight.specialmob;

import de.eldoria.bloodnight.config.worldsettings.mobsettings.MobSettings;
import de.eldoria.bloodnight.core.BloodNight;
import de.eldoria.bloodnight.specialmob.settings.Behaviour;
import de.eldoria.bloodnight.specialmob.settings.Equipment;
import de.eldoria.bloodnight.specialmob.settings.Extension;
import de.eldoria.bloodnight.specialmob.settings.MobConfiguration;
import de.eldoria.bloodnight.specialmobs.SpecialMobUtil;
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

public class SpecialMob implements ISpecialMob {
    private final Mob base;
    private final Mob extension;
    private final MobConfiguration mobConfiguration;
    private final Behaviour behaviour;
    private Instant lastDamage = Instant.now();

    private SpecialMob(Mob base, Mob extension, MobConfiguration mobConfiguration) {
        this.base = base;
        this.extension = extension;
        this.mobConfiguration = mobConfiguration;
        behaviour = mobConfiguration.getBehaviour();
    }

    public static SpecialMob construct(Mob base, MobSettings mobSettings, MobConfiguration mobConfiguration) {
        // tag mob as special mob
        SpecialMobUtil.tagSpecialMob(base);
        // set attributes
        AttributeInstance damage = base.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
        AttributeUtil.setAttributeValue(base, damage.getAttribute(), Math.min(mobConfiguration.getStats().applyDamage(damage.getValue(), mobSettings.getDamageMultiplier()), 2048));
        AttributeInstance health = base.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        AttributeUtil.setAttributeValue(base, health.getAttribute(), Math.min(mobConfiguration.getStats().applyHealth(health.getValue(), mobSettings.getHealthModifier()), 2048));
        base.setHealth(health.getValue());
        // flag with special mob type
        SpecialMobUtil.setSpecialMobType(base, mobConfiguration.getIdentifier());
        // set display name
        Optional<String> optDisplayName = mobConfiguration.getName(base.getType());
        String name = optDisplayName.orElse(ILocalizer.getPluginLocalizer(BloodNight.class).getMessage("mob." + mobConfiguration.getIdentifier() + "." + base.getType()));
        base.setCustomName(name);
        base.setCustomNameVisible(mobSettings.isDisplayMobNames());
        Equipment equipment = mobConfiguration.getEquipment();
        if(equipment != null){
            equipment.apply(base);
        }

        // build extension if required
        if (mobConfiguration.isExtended()) {
            Extension extentionSettings = mobConfiguration.getExtension();
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
                EldoUtilities.getDelayedActions().schedule(() -> SpecialMobUtil.clearEquipment(extension), 2);
            }
            equipment = extentionSettings.getEquipment();
            if(equipment != null){
                equipment.apply(extension);
            }
            return new SpecialMob(base, extension, mobConfiguration);
        }

        return new SpecialMob(base, null, mobConfiguration);
    }

    private static Mob addExtension(Mob base, Extension ext) {
        switch (ext.getExtensionRole()) {
            case CARRIER:
                return SpecialMobUtil.spawnAndMount(ext.getExtensionType(), base);
            case PASSENGER:
                return SpecialMobUtil.spawnAndMount(base, ext.getExtensionType());
            default:
                throw new IllegalStateException("Unexpected value: " + ext.getExtensionRole());
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
        SpecialMobUtil.handleExtendedEntityDamage(getBase(), getExtension(), event);
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
            SpecialMobUtil.handleExtendedEntityDamage(extension, base, event);
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
