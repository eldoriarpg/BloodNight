package de.eldoria.bloodnight.bloodmob.settings.mobsettings.setting;

import de.eldoria.bloodnight.bloodmob.serialization.annotation.NumberProperty;
import de.eldoria.bloodnight.bloodmob.serialization.annotation.Property;
import de.eldoria.bloodnight.bloodmob.settings.mobsettings.TypeSetting;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Mob;

public class CreeperSettings extends TypeSetting {
    @Property(name = "", descr = "")
    boolean powered;
    @NumberProperty(name = "", descr = "", max = 64)
    int maxFuseTick;
    @NumberProperty(name = "", descr = "", max = 64)
    int explosionRadius;

    @Override
    public void apply(Mob mob) {
        if (mob instanceof Creeper) {
            Creeper creeper = (Creeper) mob;
            creeper.setPowered(powered);
            creeper.setMaxFuseTicks(maxFuseTick);
            creeper.setExplosionRadius(explosionRadius);
        }
    }
}
