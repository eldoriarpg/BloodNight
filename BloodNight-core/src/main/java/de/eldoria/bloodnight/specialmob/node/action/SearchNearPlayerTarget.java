package de.eldoria.bloodnight.specialmob.node.action;

import de.eldoria.bloodnight.specialmob.ISpecialMob;
import de.eldoria.bloodnight.specialmob.node.Node;
import de.eldoria.bloodnight.specialmob.node.context.ContextContainer;
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

public class SearchNearPlayerTarget implements Node {
    private int range;

    @Override
    public void handle(ISpecialMob mob, ContextContainer context) {
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
}
