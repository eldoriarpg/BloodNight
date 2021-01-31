package de.eldoria.bloodnight.specialmobs.mobs.creeper;

import de.eldoria.bloodnight.specialmobs.SpecialMob;
import org.bukkit.entity.Creeper;

public abstract class AbstractCreeper extends SpecialMob<Creeper> {
    public AbstractCreeper(Creeper creeper) {
        super(creeper);
    }

    @Override
    public void onEnd() {
    }

    public int getMaxFuseTicks() {
        return getBaseEntity().getMaxFuseTicks();
    }

    public void setMaxFuseTicks(int fuse) {
        getBaseEntity().setMaxFuseTicks(fuse);
    }

    public int getExplosionRadius() {
        return getBaseEntity().getExplosionRadius();
    }

    public void setExplosionRadius(int radius) {
        getBaseEntity().setExplosionRadius(radius);
    }

    public void explode() {
        getBaseEntity().explode();
    }

    public void ignite() {
        getBaseEntity().ignite();
    }

    public boolean isPowered() {
        return getBaseEntity().isPowered();
    }

    public void setPowered(boolean value) {
        getBaseEntity().setPowered(value);
    }
}
