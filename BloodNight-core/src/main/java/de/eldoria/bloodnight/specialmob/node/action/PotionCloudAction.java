package de.eldoria.bloodnight.specialmob.node.action;

import de.eldoria.bloodnight.specialmob.ISpecialMob;
import de.eldoria.bloodnight.specialmob.node.Node;
import de.eldoria.bloodnight.specialmob.node.context.ContextContainer;
import de.eldoria.bloodnight.specialmob.node.context.ILocationContext;
import de.eldoria.bloodnight.specialmobs.effects.PotionCloud;
import org.bukkit.Color;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class PotionCloudAction implements Node {
    private Color color;
    private int duration;
    private int radius;
    private int growth;
    private PotionType potionType;
    boolean extended;
    private boolean upgraded;

    @Override
    public void handle(ISpecialMob mob, ContextContainer context) {
        if (context instanceof ILocationContext) {
            PotionCloud.builder(((ILocationContext) context).getLocation().subtract(0, 1, 0))
                    .fromSource(mob.getBase())
                    .setDuration(duration)
                    .withRadius(4)
                    .setRadiusPerTick(growth / 100f)
                    .ofColor(color)
                    .setPotionType(new PotionData(potionType, extended, upgraded));
        }
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        return null;
    }
}
