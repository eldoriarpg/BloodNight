package de.eldoria.bloodnight.specialmobs.mobs.phantom;

import de.eldoria.bloodnight.specialmobs.SpecialMobUtil;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Phantom;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class FearfulPhantom extends AbstractPhantom {
    protected FearfulPhantom(Phantom phantom) {
        super(phantom);
    }

    @Override
    public void tick() {
        SpecialMobUtil.addPotionEffect(getPhantom(), PotionEffectType.GLOWING, 1, true);
    }

    @Override
    public void onHit(EntityDamageByEntityEvent event) {
        if (event.getEntity().getType() == EntityType.PLAYER) {
            ((LivingEntity) event.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 10, 2, true, true));
        }
    }
}
