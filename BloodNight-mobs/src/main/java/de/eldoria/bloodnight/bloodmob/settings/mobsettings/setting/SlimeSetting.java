package de.eldoria.bloodnight.bloodmob.settings.mobsettings.setting;

import de.eldoria.bloodnight.bloodmob.serialization.annotation.NumberProperty;
import de.eldoria.bloodnight.bloodmob.settings.mobsettings.TypeSetting;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Slime;

public class SlimeSetting extends TypeSetting {
    @NumberProperty(name = "", descr = "", max = 16)
    int size;

    @Override
    public void apply(Mob mob) {
        if (mob instanceof Slime) {
            Slime slime = (Slime) mob;
            slime.setSize(size);
        }
    }
}
