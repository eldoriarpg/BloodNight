package de.eldoria.bloodnight.specialmob.node.action;

import de.eldoria.bloodnight.specialmob.ISpecialMob;
import de.eldoria.bloodnight.specialmob.node.Node;
import de.eldoria.bloodnight.specialmob.node.context.ContextContainer;
import de.eldoria.bloodnight.specialmob.settings.Equipment;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Mob;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class SetEquipment implements Node {
    private boolean extension = false;
    private Equipment equipment;

    @Override
    public void handle(ISpecialMob mob, ContextContainer context) {
        Mob currMob = extension ? mob.getExtension() : mob.getBase();
        if (currMob == null) return;
        equipment.apply(currMob);
    }

    /**
     * Creates a Map representation of this class.
     * <p>
     * This class must provide a method to restore this class, as defined in
     * the {@link ConfigurationSerializable} interface javadocs.
     *
     * @return Map containing the current state of this class
     */
    @NotNull
    @Override
    public Map<String, Object> serialize() {
        return null;
    }
}
