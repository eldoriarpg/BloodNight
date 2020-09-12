package de.eldoria.bloodnight.specialmobs.mobs.creeper;

import org.bukkit.entity.Creeper;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class NervousPoweredCreeper extends AbstractCreeper {
    public NervousPoweredCreeper(Creeper creeper) {
        super(creeper);
        setPowered(true);
        setMaxFuseTicks(0);
    }

    @Override
    public void tick() {
        getCreeper().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 2, false, false));
    }

    @Override
    public void onEnd() {
        setMaxFuseTicks(0);
        ignite();
    }
}
