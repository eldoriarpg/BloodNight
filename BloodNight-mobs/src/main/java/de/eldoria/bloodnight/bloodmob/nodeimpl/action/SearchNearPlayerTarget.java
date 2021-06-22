package de.eldoria.bloodnight.bloodmob.nodeimpl.action;

import de.eldoria.bloodnight.bloodmob.IBloodMob;
import de.eldoria.bloodnight.bloodmob.node.Node;
import de.eldoria.bloodnight.bloodmob.node.contextcontainer.ContextContainer;
import de.eldoria.bloodnight.bloodmob.serialization.annotation.NumberProperty;
import lombok.NoArgsConstructor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@NoArgsConstructor
public class SearchNearPlayerTarget implements Node {
    @NumberProperty(name = "", descr = "", max = 128)
    private int range;

    public SearchNearPlayerTarget(int range) {
        this.range = range;
    }

    @Override
    public void handle(IBloodMob mob, ContextContainer context) {
        Location loc = mob.getBase().getLocation();
        Collection<Entity> nearbyPlayers = loc.getWorld().
                getNearbyEntities(loc, range, range, range, entity -> {
                    if (entity.getType() == EntityType.PLAYER) {
                        Player player = (Player) entity;
                        return !player.isInvisible() && (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.CREATIVE);
                    }
                    return false;
                });
        if (nearbyPlayers.isEmpty()) {
            return;
        }
        mob.getBase().setTarget((LivingEntity) new ArrayList<>(nearbyPlayers).get(0));
        mob.invokeExtension(e -> e.setTarget((LivingEntity) new ArrayList<>(nearbyPlayers).get(0)));
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

        SearchNearPlayerTarget that = (SearchNearPlayerTarget) o;

        return range == that.range;
    }

    @Override
    public int hashCode() {
        return range;
    }
}
