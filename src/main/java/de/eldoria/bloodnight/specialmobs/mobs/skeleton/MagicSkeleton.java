package de.eldoria.bloodnight.specialmobs.mobs.skeleton;

import de.eldoria.bloodnight.specialmobs.SpecialMobUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.concurrent.ThreadLocalRandom;

public class MagicSkeleton extends AbstractSkeleton {
    private static final PotionEffect[] EFFECTS = {
            new PotionEffect(PotionEffectType.SLOW, 8 * 20, 1, true, true),
            new PotionEffect(PotionEffectType.SLOW, 6 * 20, 2, true, true),
            new PotionEffect(PotionEffectType.HARM, 2 * 20, 1, true, true),
            new PotionEffect(PotionEffectType.CONFUSION, 5 * 20, 1, true, true),
            new PotionEffect(PotionEffectType.LEVITATION, 5 * 20, 1, true, true),
            new PotionEffect(PotionEffectType.WEAKNESS, 5 * 20, 1, true, true),
            new PotionEffect(PotionEffectType.POISON, 6 * 20, 1, true, true),
            new PotionEffect(PotionEffectType.POISON, 4 * 20, 2, true, true),
    };

    private final ThreadLocalRandom rand = ThreadLocalRandom.current();

    public MagicSkeleton(Skeleton skeleton) {
        super(skeleton);
    }

    @Override
    public void tick() {
        SpecialMobUtil.addPotionEffect(getBaseEntity(), PotionEffectType.NIGHT_VISION, 1, true);
    }

    @Override
    public void onProjectileHit(ProjectileHitEvent event) {
        Entity hitEntity = event.getHitEntity();
        if (hitEntity instanceof LivingEntity) {
            ((LivingEntity) hitEntity).addPotionEffect(getRandomEffect());
        }
    }

    private PotionEffect getRandomEffect() {
        return EFFECTS[rand.nextInt(EFFECTS.length)];
    }
}
