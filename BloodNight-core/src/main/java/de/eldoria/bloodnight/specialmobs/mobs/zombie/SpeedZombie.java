package de.eldoria.bloodnight.specialmobs.mobs.zombie;

import de.eldoria.bloodnight.bloodmob.utils.BloodMobUtil;
import org.bukkit.entity.Zombie;
import org.bukkit.potion.PotionEffectType;

public class SpeedZombie extends AbstractZombie {
    public SpeedZombie(Zombie zombie) {
        super(zombie);
    }

    @Override
    public void tick() {
        BloodMobUtil.addPotionEffect(getBaseEntity(), PotionEffectType.SPEED, 4, true);
    }
}
