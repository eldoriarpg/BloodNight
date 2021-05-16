package de.eldoria.bloodnight.specialmob.node.action;

import de.eldoria.bloodnight.specialmob.ISpecialMob;
import de.eldoria.bloodnight.specialmob.node.Node;
import de.eldoria.bloodnight.specialmob.node.context.IActionContext;
import de.eldoria.bloodnight.specialmob.node.context.ILivingEntityContext;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
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

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        return null;
    }
}
