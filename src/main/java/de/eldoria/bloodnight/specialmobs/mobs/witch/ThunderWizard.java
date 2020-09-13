package de.eldoria.bloodnight.specialmobs.mobs.witch;

import de.eldoria.bloodnight.specialmobs.SpecialMobUtil;
import org.bukkit.Particle;
import org.bukkit.entity.Witch;
import org.bukkit.event.entity.ProjectileLaunchEvent;

public class ThunderWizard extends AbstractWitch {
    public ThunderWizard(Witch witch) {
        super(witch);
    }

    @Override
    public void tick() {
        SpecialMobUtil.spawnParticlesAround(getWitch(), Particle.SPELL_INSTANT, 15);
    }

    @Override
    public void onProjectileShoot(ProjectileLaunchEvent event) {
        event.setCancelled(true);
        if (getWitch().getTarget() != null) {
            getWitch().getLocation().getWorld().strikeLightning(getWitch().getTarget().getLocation());
        }
    }
}
