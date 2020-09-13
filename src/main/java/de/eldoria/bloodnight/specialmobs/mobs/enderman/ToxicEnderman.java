package de.eldoria.bloodnight.specialmobs.mobs.enderman;

import de.eldoria.bloodnight.specialmobs.SpecialMobUtil;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Enderman;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ToxicEnderman extends AbstractEnderman {
    public ToxicEnderman(Enderman enderman) {
        super(enderman);
    }

    @Override
    public void tick() {
        super.tick();
        SpecialMobUtil.spawnParticlesAround(getEnderman(), Particle.SNEEZE, 10);
    }

    @Override
    public void onTeleport(EntityTeleportEvent event) {
        Location from = event.getFrom();
        SpecialMobUtil.spawnLingeringPotionAt(from, new PotionEffect(PotionEffectType.POISON, 5, 1, true, true));
    }
}
