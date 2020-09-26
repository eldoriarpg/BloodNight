package de.eldoria.bloodnight.listener;

import de.eldoria.bloodnight.config.Configuration;
import de.eldoria.bloodnight.config.NightSettings;
import de.eldoria.bloodnight.listener.util.ListenerUtil;
import de.eldoria.bloodnight.listener.util.ProjectileSender;
import org.bukkit.entity.Boss;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class DamageListener implements Listener {

    private final NightManager nightManager;
    private final Configuration configuration;

    public DamageListener(NightManager nightManager, Configuration configuration) {
        this.nightManager = nightManager;
        this.configuration = configuration;
    }


    void onPlayerDamage(EntityDamageByEntityEvent event) {
        // If no blood night is active in this world we dont care at all.
        if (!nightManager.isBloodNightActive(event.getDamager().getWorld())) return;

        // Check if the entity is a projectile.
        ProjectileSender sender = ListenerUtil.getProjectileSource(event.getDamager());

        Entity damager = sender.isEntity() ? sender.getEntity() : event.getDamager();
        Entity oponent = event.getEntity();

        NightSettings settings = configuration.getWorldSettings(oponent.getLocation().getWorld().getName()).getNightSettings();

        // Check if opponent a monster or boss. We want also to recude non player damage.
        if (oponent instanceof Monster || oponent instanceof Boss) {
            // the damager is a player. Multiply damage by player multiplier
            event.setDamage(event.getDamage() * settings.getPlayerDamageMultiplier());
        } else if (oponent.getType() == EntityType.PLAYER
                && (damager instanceof Monster || damager instanceof Boss)) {
            event.setDamage(event.getDamage() * settings.getMonsterDamageMultiplier());
        }
    }
}
