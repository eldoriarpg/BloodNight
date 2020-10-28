package de.eldoria.bloodnight.specialmobs.mobs.enderman;

import de.eldoria.bloodnight.specialmobs.SpecialMob;
import de.eldoria.bloodnight.specialmobs.SpecialMobUtil;
import org.bukkit.GameMode;
import org.bukkit.Particle;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;

public abstract class AbstractEnderman extends SpecialMob<Enderman> {

    public AbstractEnderman(Enderman enderman) {
        super(enderman);
    }

    @Override
    public void tick() {
        if (getBaseEntity().getTarget() != null) return;
        Collection<Entity> nearbyPlayers = getBaseEntity().getWorld().
                getNearbyEntities(getBaseEntity().getLocation(), 16, 16, 16, e -> e.getType() == EntityType.PLAYER
                        && ((Player) e).getGameMode() == GameMode.SURVIVAL);
        if (nearbyPlayers.isEmpty()) {
            return;
        }
        getBaseEntity().setTarget((LivingEntity) new ArrayList<>(nearbyPlayers).get(0));
    }

    @Override
    public void onEnd() {
        SpecialMobUtil.spawnParticlesAround(getBaseEntity(), Particle.DRAGON_BREATH, 10);
    }
}
