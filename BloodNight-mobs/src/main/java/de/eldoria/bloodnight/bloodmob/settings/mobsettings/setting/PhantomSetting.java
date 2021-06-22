package de.eldoria.bloodnight.bloodmob.settings.mobsettings.setting;

import de.eldoria.bloodnight.bloodmob.serialization.annotation.NumberProperty;
import de.eldoria.bloodnight.bloodmob.settings.mobsettings.TypeSetting;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Phantom;

public class PhantomSetting extends TypeSetting {
    @NumberProperty(name = "", descr = "", max = 64)
    private int size;

    @Override
    public void apply(Mob mob) {
        if(mob instanceof Phantom){
            ((Phantom) mob).setSize(size);
        }
    }
}
