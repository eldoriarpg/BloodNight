package de.eldoria.bloodnight.bloodmob.nodeimpl.action;

import de.eldoria.bloodnight.bloodmob.IBloodMob;
import de.eldoria.bloodnight.bloodmob.node.Node;
import de.eldoria.bloodnight.bloodmob.node.contextcontainer.ContextContainer;
import de.eldoria.bloodnight.bloodmob.node.contextcontainer.ContextType;
import de.eldoria.bloodnight.bloodmob.serialization.annotation.NumberProperty;
import de.eldoria.bloodnight.bloodmob.serialization.annotation.Property;
import de.eldoria.bloodnight.bloodmob.utils.PotionCloud;
import lombok.NoArgsConstructor;
import org.bukkit.Color;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@NoArgsConstructor
public class PotionCloudAction implements Node {
    @Property(name = "", descr = "")
    boolean extended;
    @Property(name = "", descr = "")
    private Color color;
    @NumberProperty(name = "", descr = "", max = 60)
    private int duration;
    @NumberProperty(name = "", descr = "", max = 60)
    private int radius;
    @NumberProperty(name = "", descr = "", max = 50)
    private int growth;
    @Property(name = "", descr = "")
    private PotionType potionType;
    @Property(name = "", descr = "")
    private boolean upgraded;

    public PotionCloudAction(boolean extended, Color color, int duration, int radius, int growth, PotionType potionType, boolean upgraded) {
        this.extended = extended;
        this.color = color;
        this.duration = duration;
        this.radius = radius;
        this.growth = growth;
        this.potionType = potionType;
        this.upgraded = upgraded;
    }

    @Override
    public void handle(IBloodMob mob, ContextContainer context) {
        context.get(ContextType.LOCATION).ifPresent(location -> {
            PotionCloud.builder(location.getLocation().subtract(0, 1, 0))
                    .fromSource(mob.getBase())
                    .setDuration(duration)
                    .withRadius(4)
                    .setRadiusPerTick(growth / 100f)
                    .ofColor(color)
                    .setPotionType(new PotionData(potionType, extended, upgraded));
        });
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

        PotionCloudAction that = (PotionCloudAction) o;

        if (extended != that.extended) return false;
        if (duration != that.duration) return false;
        if (radius != that.radius) return false;
        if (growth != that.growth) return false;
        if (upgraded != that.upgraded) return false;
        if (!color.equals(that.color)) return false;
        return potionType == that.potionType;
    }

    @Override
    public int hashCode() {
        int result = (extended ? 1 : 0);
        result = 31 * result + color.hashCode();
        result = 31 * result + duration;
        result = 31 * result + radius;
        result = 31 * result + growth;
        result = 31 * result + potionType.hashCode();
        result = 31 * result + (upgraded ? 1 : 0);
        return result;
    }
}
