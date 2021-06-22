package de.eldoria.bloodnight.bloodmob.nodeimpl.action;

import de.eldoria.bloodnight.bloodmob.IBloodMob;
import de.eldoria.bloodnight.bloodmob.node.Node;
import de.eldoria.bloodnight.bloodmob.node.contextcontainer.ContextContainer;
import de.eldoria.bloodnight.bloodmob.serialization.annotation.Property;
import de.eldoria.bloodnight.bloodmob.settings.Equipment;
import lombok.NoArgsConstructor;
import org.bukkit.entity.Mob;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@NoArgsConstructor
public class SetEquipment implements Node {
    @Property(name = "", descr = "")
    private boolean extension = false;
    @Property(name = "", descr = "")
    private Equipment equipment;

    public SetEquipment(boolean extension, Equipment equipment) {
        this.extension = extension;
        this.equipment = equipment;
    }

    @Override
    public void handle(IBloodMob mob, ContextContainer context) {
        Mob currMob = extension ? mob.getExtension() : mob.getBase();
        if (currMob == null) return;
        equipment.apply(currMob, context.itemRegistry());
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SetEquipment that = (SetEquipment) o;

        if (extension != that.extension) return false;
        return equipment != null ? equipment.equals(that.equipment) : that.equipment == null;
    }

    @Override
    public int hashCode() {
        int result = (extension ? 1 : 0);
        result = 31 * result + (equipment != null ? equipment.hashCode() : 0);
        return result;
    }
}
