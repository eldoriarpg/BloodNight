package de.eldoria.bloodnight.bloodmob.settings.mobsettings;

import de.eldoria.bloodnight.bloodmob.serialization.annotation.Property;
import org.bukkit.entity.Mob;

public class TypeSetting {
    @Property(name = "", descr = "")
    String name;

    public String name() {
        return name;
    }

    public void apply(Mob mob) {
    }
}
