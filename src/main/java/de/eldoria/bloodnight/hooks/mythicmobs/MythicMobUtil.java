package de.eldoria.bloodnight.hooks.mythicmobs;

import de.eldoria.bloodnight.core.BloodNight;
import lombok.experimental.UtilityClass;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

@UtilityClass
public class MythicMobUtil {
    public final NamespacedKey MYTHIC_MOB_TAG = BloodNight.getNamespacedKey("mythicMob");

    public void tagMob(Entity entity) {
        PersistentDataContainer container = entity.getPersistentDataContainer();
        container.set(MYTHIC_MOB_TAG, PersistentDataType.BYTE, (byte) 1);
    }

    public boolean isMythicMob(Entity entity){
        PersistentDataContainer container = entity.getPersistentDataContainer();
        return container.has(MYTHIC_MOB_TAG, PersistentDataType.BYTE);
    }
}
