package de.eldoria.bloodnight.specialmobs.mobs.creeper;

import de.eldoria.bloodnight.bloodmob.utils.BloodMobUtil;
import de.eldoria.bloodnight.bloodmob.utils.PotionCloud;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.Creeper;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

public class ToxicCreeper extends AbstractCreeper {

    public ToxicCreeper(Creeper creeper) {
        super(creeper);
        setMaxFuseTicks(20);
    }

    @Override
    public void tick() {
        BloodMobUtil.spawnParticlesAround(getBaseEntity().getLocation(), Particle.REDSTONE, new Particle.DustOptions(Color.GREEN, 2), 5);
    }

    @Override
    public void onExplosionEvent(EntityExplodeEvent event) {
        PotionCloud.builder(event.getLocation().subtract(0, 1, 0))
                .fromSource((Creeper) event.getEntity())
                .setDuration(10)
                .setRadiusPerTick(0.01f)
                .ofColor(Color.GREEN)
                .setPotionType(new PotionData(PotionType.POISON, false, true));
    }
}
