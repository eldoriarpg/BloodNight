package de.eldoria.bloodnight.specialmobs.mobs.creeper;

import de.eldoria.bloodnight.specialmobs.effects.ParticleCloud;
import de.eldoria.bloodnight.specialmobs.effects.PotionCloud;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.Creeper;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

public class ToxicCreeper extends AbstractCreeper {

    private final ParticleCloud cloud;

    public ToxicCreeper(Creeper creeper) {
        super(creeper);
        cloud = ParticleCloud.builder(creeper)
                .withParticle(Particle.SPELL_MOB)
                .ofColor(Color.GREEN)
                .build();
    }

    @Override
    public void tick() {
        cloud.tick();
    }

    @Override
    public void onSpawn() {
        setMaxFuseTicks(20);
    }


    @Override
    public void onExplosionEvent(EntityExplodeEvent event) {
        PotionCloud.builder(event.getLocation())
                .fromSource((Creeper) event.getEntity())
                .setDuration(10)
                .setRadiusPerTick(0.01f)
                .ofColor(Color.GREEN)
                .setPotionType(new PotionData(PotionType.POISON, false, true));
    }
}
