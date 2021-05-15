package de.eldoria.bloodnight.specialmob.node.action;

import de.eldoria.bloodnight.specialmob.ISpecialMob;
import de.eldoria.bloodnight.specialmob.node.Node;
import de.eldoria.bloodnight.specialmob.node.context.IActionContext;
import de.eldoria.bloodnight.specialmob.node.context.ILivingEntityContext;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Applies a potion to itself
 */
public class OtherPotion implements Node {
    private PotionEffectType type;
    private int seconds;
    private int amplifier;
    private boolean visible;

    @Override
    public void handle(ISpecialMob mob, IActionContext context) {
        if (context instanceof ILivingEntityContext) {
            ((ILivingEntityContext) context).getEntity()
                    .addPotionEffect(new PotionEffect(type, seconds * 20, amplifier, false, visible));
        }
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
