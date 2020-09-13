package de.eldoria.bloodnight.specialmobs.mobs.phantom;

import de.eldoria.bloodnight.specialmobs.SpecialMobUtil;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Phantom;
import org.bukkit.event.entity.EntityDeathEvent;

public class FirePhantom extends AbstractPhantom {

    private final Blaze blaze;

    public FirePhantom(Phantom phantom) {
        super(phantom);
        blaze = SpecialMobUtil.spawnAndMount(getPhantom(), EntityType.BLAZE);
    }

    @Override
    public void onEnd() {
        super.onEnd();
        blaze.remove();
    }

    @Override
    public void onDeath(EntityDeathEvent event) {
        blaze.damage(blaze.getHealth());
    }
}
