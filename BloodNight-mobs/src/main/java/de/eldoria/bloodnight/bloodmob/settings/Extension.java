package de.eldoria.bloodnight.bloodmob.settings;

import de.eldoria.bloodnight.bloodmob.serialization.annotation.Property;
import de.eldoria.bloodnight.config.ConfigCheck;
import de.eldoria.bloodnight.config.ConfigException;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;

import java.util.Objects;

public class Extension implements ConfigCheck {
    @Property(name = "", descr = "")
    EntityType extensionType = null;
    @Property(name = "", descr = "")
    ExtensionRole extensionRole = null;
    @Property(name = "", descr = "")
    Equipment equipment = null;
    @Property(name = "", descr = "")
    boolean invisible = true;
    @Property(name = "", descr = "")
    boolean invulnerable = false;
    @Property(name = "", descr = "")
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

    public EntityType extensionType() {
        return extensionType;
    }

    public void extensionType(EntityType extensionType) {
        this.extensionType = extensionType;
    }

    public ExtensionRole extensionRole() {
        return extensionRole;
    }

    public void extensionRole(ExtensionRole extensionRole) {
        this.extensionRole = extensionRole;
    }

    public Equipment equipment() {
        return equipment;
    }

    public void equipment(Equipment equipment) {
        this.equipment = equipment;
    }

    public boolean isInvisible() {
        return invisible;
    }

    public void invisible(boolean invisible) {
        this.invisible = invisible;
    }

    public boolean isInvulnerable() {
        return invulnerable;
    }

    public void invulnerable(boolean invulnerable) {
        this.invulnerable = invulnerable;
    }

    public boolean isClearEquipment() {
        return clearEquipment;
    }

    public void clearEquipment(boolean clearEquipment) {
        this.clearEquipment = clearEquipment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Extension extension = (Extension) o;

        if (invisible != extension.invisible) return false;
        if (invulnerable != extension.invulnerable) return false;
        if (clearEquipment != extension.clearEquipment) return false;
        if (extensionType != extension.extensionType) return false;
        if (extensionRole != extension.extensionRole) return false;
        return Objects.equals(equipment, extension.equipment);
    }

    @Override
    public int hashCode() {
        int result = extensionType != null ? extensionType.hashCode() : 0;
        result = 31 * result + (extensionRole != null ? extensionRole.hashCode() : 0);
        result = 31 * result + (equipment != null ? equipment.hashCode() : 0);
        result = 31 * result + (invisible ? 1 : 0);
        result = 31 * result + (invulnerable ? 1 : 0);
        result = 31 * result + (clearEquipment ? 1 : 0);
        return result;
    }
}
