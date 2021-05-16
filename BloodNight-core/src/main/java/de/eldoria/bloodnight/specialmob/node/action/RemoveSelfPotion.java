package de.eldoria.bloodnight.specialmob.node.action;

import de.eldoria.bloodnight.specialmob.ISpecialMob;
import de.eldoria.bloodnight.specialmob.node.Node;
import de.eldoria.bloodnight.specialmob.node.context.IActionContext;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Applies a potion to itself
 */
public class RemoveSelfPotion implements Node {
    private PotionEffectType type;
    private boolean base;
    private boolean extension;

    @Override
    public void handle(ISpecialMob mob, IActionContext context) {
        if (base) mob.getBase().removePotionEffect(type);
        if (extension) mob.invokeExtension(m -> m.removePotionEffect(type));
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        return null;
    }
}
