package de.eldoria.bloodnight.specialmob.settings;

import de.eldoria.bloodnight.config.ConfigCheck;
import de.eldoria.bloodnight.config.ConfigException;
import lombok.Getter;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;

@Getter
public class Extension implements ConfigCheck {
    EntityType extensionType = null;
    ExtensionRole extensionRole = null;
    Equipment equipment;
    boolean invisible = true;
    boolean invulnerable = false;
    boolean clearEquipment = false;

    @Override
    public void check(Object data) throws ConfigException {
        if (extensionRole != null && extensionType == null) {
            throw new ConfigException("Extension role is set, but no extension type is present.");
        }
        if (extensionType != null && extensionRole == null) {
            throw new ConfigException("Extension type is set, but no extension role is present.");
        }
        if (extensionType != null && !extensionType.getEntityClass().isInstance(Mob.class)) {
            throw new ConfigException(extensionType.getEntityClass().getSimpleName() + " is not a mob.");
        }
    }
}
