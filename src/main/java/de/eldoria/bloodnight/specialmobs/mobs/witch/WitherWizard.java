package de.eldoria.bloodnight.specialmobs.mobs.witch;

import de.eldoria.bloodnight.specialmobs.SpecialMobUtil;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LargeFireball;
import org.bukkit.entity.Witch;
import org.bukkit.entity.WitherSkeleton;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class WitherWizard extends AbstractWitch {
    private Instant lastShot = Instant.now();

    public WitherWizard(Witch witch) {
        super(witch);
    }

    @Override
    public void tick() {
        EntityEquipment equipment = getBaseEntity().getEquipment();
        equipment.setItemInMainHand(new ItemStack(Material.WITHER_SKELETON_SKULL));
        SpecialMobUtil.spawnParticlesAround(getBaseEntity(), Particle.SPELL_INSTANT, 15);
        if (canShoot(5)) {
            SpecialMobUtil.launchProjectileOnTarget(getBaseEntity(), WitherSkull.class, 4);
            shot();
        }
    }

    @Override
    public void onProjectileShoot(ProjectileLaunchEvent event) {
        if (event.getEntity().getType() == EntityType.WITHER_SKULL) return;
        event.setCancelled(true);
    }
}
