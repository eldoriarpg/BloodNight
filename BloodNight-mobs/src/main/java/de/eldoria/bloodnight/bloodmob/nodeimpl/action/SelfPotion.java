package de.eldoria.bloodnight.bloodmob.nodeimpl.action;

import de.eldoria.bloodnight.bloodmob.IBloodMob;
import de.eldoria.bloodnight.bloodmob.node.Node;
import de.eldoria.bloodnight.bloodmob.node.contextcontainer.ContextContainer;
import de.eldoria.bloodnight.bloodmob.serialization.annotation.EnumLikeProperty;
import de.eldoria.bloodnight.bloodmob.serialization.annotation.NumberProperty;
import de.eldoria.bloodnight.bloodmob.serialization.annotation.Property;
import de.eldoria.bloodnight.bloodmob.utils.BloodMobUtil;
import lombok.NoArgsConstructor;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Applies a potion to itself
 */
@NoArgsConstructor
public class SelfPotion implements Node {
    @EnumLikeProperty(name = "", descr = "")
    private PotionEffectType type;
    @NumberProperty(name = "", descr = "")
    private int seconds;
    @NumberProperty(name = "", descr = "")
    private int amplifier;
    @Property(name = "", descr = "")
    private boolean visible;
    @Property(name = "", descr = "")
    private boolean extension;
    @Property(name = "", descr = "")
    private boolean base;

    public SelfPotion(PotionEffectType type, int seconds, int amplifier, boolean visible, boolean extension, boolean base) {
        this.type = type;
        this.seconds = seconds;
        this.amplifier = amplifier;
        this.visible = visible;
        this.extension = extension;
        this.base = base;
    }

    @Override
    public void handle(IBloodMob mob, ContextContainer context) {
        if (base) BloodMobUtil.addPotionEffect(mob.getBase(), type, amplifier, visible);
        if (extension) mob.invokeExtension(m -> BloodMobUtil.addPotionEffect(m, type, seconds, amplifier, visible));
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

        SelfPotion that = (SelfPotion) o;

        if (seconds != that.seconds) return false;
        if (amplifier != that.amplifier) return false;
        if (visible != that.visible) return false;
        if (extension != that.extension) return false;
        if (base != that.base) return false;
        return type != null ? type.equals(that.type) : that.type == null;
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + seconds;
        result = 31 * result + amplifier;
        result = 31 * result + (visible ? 1 : 0);
        result = 31 * result + (extension ? 1 : 0);
        result = 31 * result + (base ? 1 : 0);
        return result;
    }
}
