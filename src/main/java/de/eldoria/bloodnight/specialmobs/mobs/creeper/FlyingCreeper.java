package de.eldoria.bloodnight.specialmobs.mobs.creeper;

import org.bukkit.entity.Bat;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class FlyingCreeper extends AbstractCreeper {

    private Bat bat;

    public FlyingCreeper(Creeper creeper) {
        super(creeper);
        bat = (Bat) creeper.getLocation().getWorld().spawnEntity(creeper.getLocation(), EntityType.BAT);
        bat.addPassenger(creeper);
        bat.setInvulnerable(true);
        tick();
    }

    @Override
    public void tick() {
        bat.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 60, 1, false, false));
        bat.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 4, false, false));
    }

    @Override
    public void onDeath(EntityDeathEvent event) {
        bat.remove();
    }

    @Override
    public void onExplosionEvent(EntityExplodeEvent event) {
        bat.remove();
    }

    @Override
    public void onEnd() {
        bat.remove();
        super.onEnd();
    }

    @Override
    public void onTargetEvent(EntityTargetEvent event) {
        if (event.getTarget() instanceof LivingEntity) {
            bat.setTarget((LivingEntity) event.getTarget());
        }
    }
}
