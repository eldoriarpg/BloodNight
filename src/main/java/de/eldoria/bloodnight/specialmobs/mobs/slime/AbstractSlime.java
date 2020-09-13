package de.eldoria.bloodnight.specialmobs.mobs.slime;

import de.eldoria.bloodnight.specialmobs.SpecialMob;
import lombok.Getter;
import org.bukkit.entity.Slime;

public abstract class AbstractSlime implements SpecialMob {
    @Getter
    private final Slime slime;

    protected AbstractSlime(Slime slime) {
        this.slime = slime;
    }

    @Override
    public void onEnd() {
        slime.remove();
    }
}
