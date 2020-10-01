package de.eldoria.bloodnight.specialmobs.mobs.zombie;

import de.eldoria.bloodnight.specialmobs.SpecialMob;
import lombok.Getter;
import org.bukkit.entity.Zombie;

public abstract class AbstractZombie implements SpecialMob {
    @Getter
    private final Zombie zombie;

    protected AbstractZombie(Zombie zombie) {
        this.zombie = zombie;
    }

    @Override
    public void onEnd() {
        zombie.remove();
    }
}
