package de.eldoria.bloodnight.specialmobs.mobs.enderman;

import de.eldoria.bloodnight.specialmobs.SpecialMob;
import de.eldoria.bloodnight.specialmobs.SpecialMobUtil;
import lombok.Getter;
import org.bukkit.GameMode;
import org.bukkit.Particle;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;

public abstract class AbstractEnderman implements SpecialMob {
    @Getter
    private final Enderman enderman;

    public AbstractEnderman(Enderman enderman) {
        this.enderman = enderman;
    }

    @Override
    public void tick() {
        if (enderman.getTarget() != null) return;
        Collection<Entity> nearbyPlayers = enderman.getWorld().
                getNearbyEntities(enderman.getLocation(), 16, 16, 16, e -> e.getType() == EntityType.PLAYER
                        && ((Player) e).getGameMode() == GameMode.SURVIVAL);
        if (nearbyPlayers.isEmpty()) {
            return;
        }
        enderman.setTarget((LivingEntity) new ArrayList<>(nearbyPlayers).get(0));
    }

    @Override
    public void onEnd() {
        SpecialMobUtil.spawnParticlesAround(enderman, Particle.DRAGON_BREATH, 10);
        if (enderman.isValid()) {
            enderman.remove();
        }
    }
}
