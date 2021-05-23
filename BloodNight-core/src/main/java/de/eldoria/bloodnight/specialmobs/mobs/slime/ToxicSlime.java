package de.eldoria.bloodnight.specialmobs.mobs.slime;

import de.eldoria.bloodnight.bloodmob.utils.BloodMobUtil;
import org.bukkit.entity.Slime;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ToxicSlime extends AbstractSlime {
    public ToxicSlime(Slime slime) {
        super(slime);
    }

    @Override
    public void onDeath(EntityDeathEvent event) {
        BloodMobUtil.spawnLingeringPotionAt(event.getEntity().getLocation(),
                new PotionEffect(PotionEffectType.POISON, 5 * 20, 2, true, true));
    }
}
