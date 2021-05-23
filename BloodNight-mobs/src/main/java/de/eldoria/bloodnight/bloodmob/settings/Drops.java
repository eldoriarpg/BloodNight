package de.eldoria.bloodnight.bloodmob.settings;

import de.eldoria.bloodnight.bloodmob.serialization.annotation.NumberProperty;
import de.eldoria.bloodnight.bloodmob.serialization.annotation.Property;
import de.eldoria.bloodnight.config.ConfigCheck;
import de.eldoria.bloodnight.config.ConfigException;
import de.eldoria.bloodnight.bloodmob.drop.Drop;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Drops implements ConfigCheck {

    /**
     * Min amount of drops.
     */
    @Setter
    @NumberProperty(name = "", descr = "", min = -1, max = 64)
    private int minDrops = -1;

    /**
     * Max amount of drops.
     */
    @Setter
    @NumberProperty(name = "", descr = "", min = -1, max = 64)
    private int maxDrops = -1;

    /**
     * If this is true only drops from mobs are choosen and default drops will not drop. if false the drops will be
     * added to default drops.
     */
    @Setter
    @Property(name = "", descr = "")
    private boolean overrideDefaultDrops = false;

    /**
     * The drops of this mob.
     */
    @Setter
    private List<Drop> drops = new ArrayList<>();

    @Override
    public void check(Object data) throws ConfigException {
        ConfigCheck.isInRange(minDrops, -1, 64, "minDrops");
        ConfigCheck.isInRange(maxDrops, minDrops, 64, "maxDrops");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Drops drops1 = (Drops) o;

        if (minDrops != drops1.minDrops) return false;
        if (maxDrops != drops1.maxDrops) return false;
        if (overrideDefaultDrops != drops1.overrideDefaultDrops) return false;
        return drops != null ? drops.equals(drops1.drops) : drops1.drops == null;
    }

    @Override
    public int hashCode() {
        int result = minDrops;
        result = 31 * result + maxDrops;
        result = 31 * result + (overrideDefaultDrops ? 1 : 0);
        result = 31 * result + (drops != null ? drops.hashCode() : 0);
        return result;
    }
}