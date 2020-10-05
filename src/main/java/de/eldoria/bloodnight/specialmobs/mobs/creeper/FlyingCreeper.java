package de.eldoria.bloodnight.specialmobs.mobs.creeper;

import de.eldoria.bloodnight.specialmobs.SpecialMobUtil;
import org.bukkit.entity.Bee;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
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
    }

    @Override
    public void tick() {
        SpecialMobUtil.addPotionEffect(bee, PotionEffectType.SPEED, 4, false);
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
        }
    }
}
