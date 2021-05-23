package de.eldoria.bloodnight.bloodmob.nodeimpl.action;

import de.eldoria.bloodnight.bloodmob.IBloodMob;
import de.eldoria.bloodnight.bloodmob.node.Node;
import de.eldoria.bloodnight.bloodmob.node.contextcontainer.ContextContainer;
import de.eldoria.bloodnight.bloodmob.serialization.annotation.EnumLikeProperty;
import de.eldoria.bloodnight.bloodmob.serialization.annotation.Property;
import lombok.NoArgsConstructor;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Applies a potion to itself
 */
@NoArgsConstructor
public class RemoveSelfPotion implements Node {
    @EnumLikeProperty(name = "", descr = "")
    private PotionEffectType type;
    @Property(name = "", descr = "")
    private boolean base;
    @Property(name = "", descr = "")
    private boolean extension;

    public RemoveSelfPotion(PotionEffectType type, boolean base, boolean extension) {
        this.type = type;
        this.base = base;
        this.extension = extension;
    }

    @Override
    public void handle(IBloodMob mob, ContextContainer context) {
        if (base) mob.getBase().removePotionEffect(type);
        if (extension) mob.invokeExtension(m -> m.removePotionEffect(type));
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

        RemoveSelfPotion that = (RemoveSelfPotion) o;

        if (base != that.base) return false;
        if (extension != that.extension) return false;
        return type != null ? type.equals(that.type) : that.type == null;
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (base ? 1 : 0);
        result = 31 * result + (extension ? 1 : 0);
        return result;
    }
}
