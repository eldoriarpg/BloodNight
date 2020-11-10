package de.eldoria.bloodnight.specialmobs.mobs.creeper;

import de.eldoria.bloodnight.core.BloodNight;
import de.eldoria.bloodnight.specialmobs.SpecialMobUtil;
import de.eldoria.bloodnight.specialmobs.mobs.ExtendedSpecialMob;
import de.eldoria.eldoutilities.container.Triple;
import de.eldoria.eldoutilities.crossversion.ServerVersion;
import org.bukkit.Material;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Vex;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Optional;

public class GhostCreeper extends ExtendedSpecialMob<Vex, Creeper> {

    private boolean legacy = false;

    public GhostCreeper(Creeper creeper) {
        super(EntityType.VEX, creeper);
        Optional<Triple<Integer, Integer, Integer>> optVersion = ServerVersion.extractVersion();
        // Entites can be invisible since 1.16.3. Hotfix for backwards compatibiliy to spigot 1.16.2
        if (optVersion.isPresent()) {
            Triple<Integer, Integer, Integer> version = optVersion.get();
            if (version.second >= 16 && version.third > 2) {
                getBaseEntity().setInvisible(true);
            } else {
                legacy = true;
            }
        } else {
            legacy = true;
        }
        getBaseEntity().setInvulnerable(true);
        new BukkitRunnable() {
            @Override
            public void run() {
                getBaseEntity().getEquipment().setItemInMainHand(null);
                getBaseEntity().getEquipment().setItemInOffHand(null);
            }
        }.runTaskLater(BloodNight.getInstance(), 2);
        //AttributeUtil.setAttributeValue(getBaseEntity(), Attribute.GENERIC_FLYING_SPEED, 100);
    }

    @Override
    public void tick() {
        if (legacy) {
            SpecialMobUtil.addPotionEffect(getBaseEntity(), PotionEffectType.INVISIBILITY, 4, false);
        }
        SpecialMobUtil.addPotionEffect(getBaseEntity(), PotionEffectType.SPEED, 4, false);
        super.tick();
    }

    @Override
    public void onDamage(EntityDamageEvent event) {
        if(event.getCause() == EntityDamageEvent.DamageCause.SUFFOCATION){
            event.setCancelled(true);
            return;
        }
        super.onDamage(event);
    }

    @Override
    public void onExtensionDamage(EntityDamageEvent event) {
        if(event.getCause() == EntityDamageEvent.DamageCause.SUFFOCATION){
            event.setCancelled(true);
            return;
        }
        super.onExtensionDamage(event);
    }

    @Override
    public void onExplosionEvent(EntityExplodeEvent event) {
        getBaseEntity().remove();
    }
}
