package de.eldoria.bloodnight.specialmobs.mobs.creeper;

import de.eldoria.bloodnight.specialmobs.SpecialMobUtil;
import de.eldoria.eldoutilities.container.Triple;
import de.eldoria.eldoutilities.crossversion.ServerVersion;
import de.eldoria.eldoutilities.utils.AttributeUtil;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Vex;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.util.Optional;

public class FlyingCreeper extends AbstractCreeper {

    private final Vex vex;
    private boolean legacy = false;

    public FlyingCreeper(Creeper creeper) {
        super(creeper);
        vex = SpecialMobUtil.spawnAndMount(EntityType.VEX, getBaseEntity());
        vex.setInvulnerable(true);
        Optional<Triple<Integer, Integer, Integer>> optVersion = ServerVersion.extractVersion();
        // Entites can be invisible since 1.16.3. Hotfix for backwards compatibiliy to spigot 1.16.2
        if (optVersion.isPresent()) {
            Triple<Integer, Integer, Integer> version = optVersion.get();
            if (version.second >= 16 && version.third > 2) {
                vex.setInvisible(true);
            } else {
                legacy = true;
            }
        } else {
            legacy = true;
        }
        vex.setCollidable(false);
        vex.setInvulnerable(true);
        vex.getEquipment().setItemInMainHand(new ItemStack(Material.AIR));
        //AttributeUtil.setAttributeValue(vex, Attribute.GENERIC_FLYING_SPEED, 100);
    }

    @Override
    public void tick() {
        if (legacy) {
            SpecialMobUtil.addPotionEffect(vex, PotionEffectType.INVISIBILITY, 4, false);
        }
        SpecialMobUtil.addPotionEffect(vex, PotionEffectType.SPEED, 4, false);
        vex.setTarget(getBaseEntity().getTarget());
    }

    @Override
    public void onDeath(EntityDeathEvent event) {
        vex.remove();
    }

    @Override
    public void onExplosionEvent(EntityExplodeEvent event) {
        vex.remove();
    }

    @Override
    public void onEnd() {
        vex.remove();
        super.onEnd();
    }

    @Override
    public void remove() {
        vex.remove();
        super.remove();
    }

    @Override
    public void onTargetEvent(EntityTargetEvent event) {
        if (event.getTarget() instanceof LivingEntity) {
            vex.setTarget((LivingEntity) event.getTarget());
            vex.setCharging(true);
        }
    }

    @Override
    public void onExtensionDamage(EntityDamageEvent event) {
        event.setDamage(0);
    }
}
