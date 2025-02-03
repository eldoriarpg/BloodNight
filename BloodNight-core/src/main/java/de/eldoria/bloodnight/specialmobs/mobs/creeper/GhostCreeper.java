package de.eldoria.bloodnight.specialmobs.mobs.creeper;

import de.eldoria.bloodnight.core.BloodNight;
import de.eldoria.bloodnight.specialmobs.SpecialMobUtil;
import de.eldoria.bloodnight.specialmobs.mobs.ExtendedSpecialMob;
import de.eldoria.eldoutilities.crossversion.ServerVersion;
import de.eldoria.eldoutilities.utils.Version;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Vex;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class GhostCreeper extends ExtendedSpecialMob<Vex, Creeper> {

    public GhostCreeper(Creeper creeper) {
        super(EntityType.VEX, creeper);
        Version optVersion = ServerVersion.getVersion();
        getBaseEntity().setInvisible(true);
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
        SpecialMobUtil.addPotionEffect(getBaseEntity(), PotionEffectType.SPEED, 4, false);
        super.tick();
    }

    @Override
    public void onDamage(EntityDamageEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.SUFFOCATION) {
            event.setCancelled(true);
            return;
        }
        super.onDamage(event);
    }

    @Override
    public void onExtensionDamage(EntityDamageEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.SUFFOCATION) {
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
