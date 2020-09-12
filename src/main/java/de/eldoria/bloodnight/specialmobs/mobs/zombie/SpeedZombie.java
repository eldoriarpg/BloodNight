package de.eldoria.bloodnight.specialmobs.mobs.zombie;

import de.eldoria.bloodnight.specialmobs.SpecialMobUtil;
import org.bukkit.entity.Zombie;
import org.bukkit.potion.PotionEffectType;

public class SpeedZombie extends AbstractZombie {
    protected SpeedZombie(Zombie zombie) {
        super(zombie);
        tick();
    }

    @Override
    public void tick() {
        SpecialMobUtil.addPotionEffect(getZombie(), PotionEffectType.SPEED, 4, true);
    }
}
