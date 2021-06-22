package de.eldoria.bloodnight.specialmobs.mobs.phantom;

import de.eldoria.bloodnight.bloodmob.utils.BloodMobUtil;
import de.eldoria.eldoutilities.utils.AttributeUtil;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Phantom;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;

public class FirePhantom extends AbstractPhantom {

    private final Blaze blaze;

    public FirePhantom(Phantom phantom) {
        super(phantom);
        blaze = BloodMobUtil.spawnAndMount(getBaseEntity(), EntityType.BLAZE);
        AttributeUtil.syncAttributeValue(phantom, blaze, Attribute.GENERIC_ATTACK_DAMAGE);
        AttributeUtil.syncAttributeValue(phantom, blaze, Attribute.GENERIC_MAX_HEALTH);
    }

    @Override
    public void onEnd() {
        blaze.remove();
    }

    @Override
    public void onDamageByEntity(EntityDamageByEntityEvent event) {
    }

    @Override
    public void onDamage(EntityDamageEvent event) {
        BloodMobUtil.handleExtendedEntityDamage(getBaseEntity(), blaze, event);
    }

    @Override
    public void onExtensionDamage(EntityDamageEvent event) {
        BloodMobUtil.handleExtendedEntityDamage(blaze, getBaseEntity(), event);
    }

    @Override
    public void onTargetEvent(EntityTargetEvent event) {
        blaze.setTarget(event.getTarget() == null ? null : (LivingEntity) event.getTarget());
    }

    @Override
    public void onDeath(EntityDeathEvent event) {
        blaze.damage(blaze.getHealth(), getBaseEntity());
    }
}
