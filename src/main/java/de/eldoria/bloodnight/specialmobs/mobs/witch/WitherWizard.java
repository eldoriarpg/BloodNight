package de.eldoria.bloodnight.specialmobs.mobs.witch;

import de.eldoria.bloodnight.specialmobs.SpecialMobUtil;
import org.bukkit.Particle;
import org.bukkit.entity.Witch;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.entity.ProjectileLaunchEvent;

public class WitherWizard extends AbstractWitch {
    public WitherWizard(Witch witch) {
        super(witch);
    }

    @Override
    public void tick() {
        SpecialMobUtil.spawnParticlesAround(getBaseEntity(), Particle.SPELL_INSTANT, 15);
    }

    @Override
    public void onProjectileShoot(ProjectileLaunchEvent event) {
        event.setCancelled(true);
        SpecialMobUtil.launchProjectileOnTarget(getBaseEntity(), WitherSkull.class, 2);
    }
}
