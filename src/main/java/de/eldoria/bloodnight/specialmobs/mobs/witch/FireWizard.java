package de.eldoria.bloodnight.specialmobs.mobs.witch;

import de.eldoria.bloodnight.specialmobs.SpecialMobUtil;
import org.bukkit.Particle;
import org.bukkit.entity.LargeFireball;
import org.bukkit.entity.Witch;
import org.bukkit.event.entity.ProjectileLaunchEvent;

public class FireWizard extends AbstractWitch {
    public FireWizard(Witch witch) {
        super(witch);
        tick();
    }

    @Override
    public void tick() {
        SpecialMobUtil.spawnParticlesAround(getBaseEntity(), Particle.LAVA, 15);
    }

    @Override
    public void onProjectileShoot(ProjectileLaunchEvent event) {
        event.setCancelled(true);
        SpecialMobUtil.launchProjectileOnTarget(getBaseEntity(), LargeFireball.class, 1.5);
    }
}
