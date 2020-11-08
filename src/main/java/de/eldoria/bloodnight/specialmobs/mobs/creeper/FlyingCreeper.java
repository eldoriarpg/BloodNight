package de.eldoria.bloodnight.specialmobs.mobs.creeper;

import de.eldoria.bloodnight.specialmobs.SpecialMobUtil;
import de.eldoria.eldoutilities.container.Triple;
import de.eldoria.eldoutilities.crossversion.ServerVersion;
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

import java.util.Optional;

public class FlyingCreeper extends AbstractCreeper {

    private final Bee bee;
    private boolean legacy = false;

    public FlyingCreeper(Creeper creeper) {
        super(creeper);
        bee = SpecialMobUtil.spawnAndMount(EntityType.BEE, getBaseEntity());
        bee.setInvulnerable(true);
        Optional<Triple<Integer, Integer, Integer>> optVersion = ServerVersion.extractVersion();
        // Bees can be invisible since 1.16.3. Hotfix for backwards compatibiliy to spigot 1.16.2
        if (optVersion.isPresent()) {
            Triple<Integer, Integer, Integer> version = optVersion.get();
            if (version.second >= 16 && version.third > 2) {
                bee.setInvisible(true);
            } else {
                legacy = true;
            }
        } else {
            legacy = true;
        }
        bee.setCollidable(true);
        AttributeUtil.setAttributeValue(bee, Attribute.GENERIC_FLYING_SPEED, 150);
    }

    @Override
    public void tick() {
        if (legacy) {
            SpecialMobUtil.addPotionEffect(bee, PotionEffectType.INVISIBILITY, 4, false);
        }
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
