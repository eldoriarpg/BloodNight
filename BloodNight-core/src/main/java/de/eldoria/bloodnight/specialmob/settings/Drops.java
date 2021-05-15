package de.eldoria.bloodnight.specialmob.settings;

import de.eldoria.bloodnight.config.ConfigCheck;
import de.eldoria.bloodnight.config.ConfigException;
import de.eldoria.bloodnight.config.worldsettings.mobsettings.Drop;
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
    private int minDrops = -1;

    /**
     * Max amount of drops.
     */
    @Setter
    private int maxDrops = -1;

    /**
     * If this is true only drops from mobs are choosen and default drops will not drop. if false the drops will be
     * added to default drops.
     */
    @Setter
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
}