package de.eldoria.bloodnight.specialmobs.mobs.creeper;

import de.eldoria.bloodnight.specialmobs.SpecialMob;
import org.bukkit.entity.Creeper;

public abstract class AbstractCreeper implements SpecialMob {
    private final Creeper creeper;

    public AbstractCreeper(Creeper creeper) {
        this.creeper = creeper;
    }

    @Override
    public void onEnd() {
        creeper.explode();
    }

    public int getMaxFuseTicks() {
        return creeper.getMaxFuseTicks();
    }

    public void setMaxFuseTicks(int fuse) {
        creeper.setMaxFuseTicks(fuse);
    }

    public int getExplosionRadius() {
        return creeper.getExplosionRadius();
    }

    public void setExplosionRadius(int radius) {
        creeper.setExplosionRadius(radius);
    }

    public void explode() {
        creeper.explode();
    }

    public void ignite() {
        creeper.ignite();
    }

    public boolean isPowered() {
        return creeper.isPowered();
    }

    public void setPowered(boolean value) {
        creeper.setPowered(value);
    }

    public Creeper getCreeper() {
        return creeper;
    }
}
