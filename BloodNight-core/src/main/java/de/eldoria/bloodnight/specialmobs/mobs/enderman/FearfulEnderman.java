package de.eldoria.bloodnight.specialmobs.mobs.enderman;

import de.eldoria.bloodnight.specialmobs.SpecialMobUtil;
import org.bukkit.Particle;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class FearfulEnderman extends AbstractEnderman {
    public FearfulEnderman(Enderman enderman) {
        super(enderman);
    }

    @Override
    public void tick() {
        super.tick();
        SpecialMobUtil.spawnParticlesAround(getBaseEntity(), Particle.SPELL_WITCH, 10);
    }

    @Override
    public void onHit(EntityDamageByEntityEvent event) {
        if (event.getEntity().getType() == EntityType.PLAYER) {
            ((LivingEntity) event.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 7 * 20, 1, true, true));
        }
    }
}
