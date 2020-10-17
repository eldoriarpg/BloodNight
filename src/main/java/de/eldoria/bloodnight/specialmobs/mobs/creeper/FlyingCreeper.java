package de.eldoria.bloodnight.specialmobs.mobs.creeper;

import de.eldoria.bloodnight.specialmobs.SpecialMobUtil;
import de.eldoria.eldoutilities.utils.AttributeUtil;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Bee;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.potion.PotionEffectType;

public class FlyingCreeper extends AbstractCreeper {

    private final Bee bee;

    public FlyingCreeper(Creeper creeper) {
        super(creeper);
        bee = SpecialMobUtil.spawnAndMount(EntityType.BEE, getBaseEntity());
        bee.setInvulnerable(true);
        bee.setInvisible(true);
        bee.setCollidable(true);
        AttributeUtil.setAttributeValue(bee, Attribute.GENERIC_FLYING_SPEED, 50);
    }

    @Override
    public void tick() {
        SpecialMobUtil.addPotionEffect(bee, PotionEffectType.SPEED, 4, false);
        bee.setTarget(getBaseEntity().getTarget());
    }

    @Override
    public void onDeath(EntityDeathEvent event) {
        bee.remove();
    }

    @Override
    public void onExplosionEvent(EntityExplodeEvent event) {
        bee.remove();
    }

    @Override
    public void onEnd() {
        bee.remove();
        super.onEnd();
    }


    @Override
    public void onTargetEvent(EntityTargetEvent event) {
        if (event.getTarget() instanceof LivingEntity) {
            bee.setTarget((LivingEntity) event.getTarget());
            bee.setAnger(10);
            bee.setHasStung(false);
        }
    }

    @Override
    public void onExtensionDamage(EntityDamageEvent event) {
        event.setDamage(0);
    }
}
