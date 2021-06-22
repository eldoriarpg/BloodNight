package de.eldoria.bloodnight.bloodmob.nodeimpl.action;

import de.eldoria.bloodnight.bloodmob.IBloodMob;
import de.eldoria.bloodnight.bloodmob.node.Node;
import de.eldoria.bloodnight.bloodmob.node.annotations.RequiresContext;
import de.eldoria.bloodnight.bloodmob.node.context.ILivingEntityContext;
import de.eldoria.bloodnight.bloodmob.node.contextcontainer.ContextContainer;
import de.eldoria.bloodnight.bloodmob.node.contextcontainer.ContextType;
import de.eldoria.bloodnight.bloodmob.serialization.annotation.EnumLikeProperty;
import de.eldoria.bloodnight.bloodmob.serialization.annotation.NumberProperty;
import de.eldoria.bloodnight.bloodmob.serialization.annotation.NumericProperty;
import de.eldoria.bloodnight.bloodmob.serialization.annotation.Property;
import lombok.NoArgsConstructor;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Applies a potion to itself
 */
@NoArgsConstructor
@RequiresContext(ILivingEntityContext.class)
public class OtherPotion implements Node {
    @EnumLikeProperty(name = "", descr = "")
    private PotionEffectType type;
    @NumberProperty(name = "", descr = "")
    private int seconds;
    @NumericProperty(name = "", descr = "")
    private int amplifier;
    @Property(name = "", descr = "")
    private boolean visible;

    public OtherPotion(PotionEffectType type, int seconds, int amplifier, boolean visible) {
        this.type = type;
        this.seconds = seconds;
        this.amplifier = amplifier;
        this.visible = visible;
    }

    @Override
    public void handle(IBloodMob mob, ContextContainer context) {
        context.get(ContextType.LIVING_ENTITY).ifPresent(entity -> entity.getEntity()
                .addPotionEffect(new PotionEffect(type, seconds * 20, amplifier, false, visible)));
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

        OtherPotion that = (OtherPotion) o;

        if (seconds != that.seconds) return false;
        if (amplifier != that.amplifier) return false;
        if (visible != that.visible) return false;
        return type.equals(that.type);
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + seconds;
        result = 31 * result + amplifier;
        result = 31 * result + (visible ? 1 : 0);
        return result;
    }
}
