package de.eldoria.bloodnight.specialmobs.mobs.witch;

import de.eldoria.bloodnight.specialmobs.SpecialMobUtil;
import org.bukkit.Particle;
import org.bukkit.entity.Witch;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.entity.ProjectileLaunchEvent;

public class WhiterWizard extends AbstractWitch {
    public WhiterWizard(Witch witch) {
        super(witch);
    }

    @Override
    public void tick() {
        SpecialMobUtil.spawnParticlesAround(getWitch(), Particle.SPELL_INSTANT, 15);
    }

    @Override
    public void onProjectileShoot(ProjectileLaunchEvent event) {
        event.setCancelled(true);
        SpecialMobUtil.launchProjectileOnTarget(getWitch(), WitherSkull.class, 2);
    }
}
