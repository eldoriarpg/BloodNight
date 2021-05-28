package de.eldoria.bloodnight.bloodmob.settings.mobsettings.setting;

import de.eldoria.bloodnight.bloodmob.serialization.annotation.Property;
import de.eldoria.bloodnight.bloodmob.settings.mobsettings.TypeSetting;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Rabbit;

public class RabbitSettings extends TypeSetting {
    @Property(name = "", descr = "")
    boolean isKiller;

    @Override
    public void apply(Mob mob) {
        if (mob instanceof Rabbit) {
            if (isKiller) ((Rabbit) mob).setRabbitType(Rabbit.Type.THE_KILLER_BUNNY);
        }
    }
}
