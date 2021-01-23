package de.eldoria.bloodnight.specialmobs.mobs.witch;

import de.eldoria.bloodnight.specialmobs.SpecialMobUtil;
import de.eldoria.eldoutilities.utils.ObjUtil;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LargeFireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Witch;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

public class FireWizard extends AbstractWitch {
    public FireWizard(Witch witch) {
        super(witch);
        tick();
    }

    @Override
    public void tick() {
        EntityEquipment equipment = getBaseEntity().getEquipment();
        equipment.setItemInMainHand(new ItemStack(Material.FIRE_CHARGE));
        SpecialMobUtil.spawnParticlesAround(getBaseEntity(), Particle.DRIP_LAVA, 5);
        if (canShoot(5)) {
            SpecialMobUtil.launchProjectileOnTarget(getBaseEntity(), LargeFireball.class, 4);
            shot();
        }
    }

    @Override
    public void onProjectileShoot(ProjectileLaunchEvent event) {
        if (event.getEntity().getType() == EntityType.FIREBALL) return;
        event.setCancelled(true);
    }

    @Override
    public void onProjectileHit(ProjectileHitEvent event) {
        ObjUtil.nonNull(event.getEntity(), e -> {
            if (e.getType() == EntityType.PLAYER) {
                AttributeInstance attribute = getBaseEntity().getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
                ((LivingEntity) event.getHitEntity()).damage(attribute.getValue(), getBaseEntity());
            }
        });
    }
}
