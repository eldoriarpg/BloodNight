package de.eldoria.bloodnight.listener;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.eldoria.bloodnight.config.Configuration;
import de.eldoria.bloodnight.config.NightSettings;
import de.eldoria.bloodnight.listener.util.ListenerUtil;
import de.eldoria.bloodnight.listener.util.ProjectileSender;
import org.bukkit.entity.Boss;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.TimeUnit;

public class LootListener implements Listener {
    private final Cache<Integer, Player> lastDamage = CacheBuilder.newBuilder().expireAfterAccess(10L, TimeUnit.MINUTES).build();
    private final NightListener nightListener;
    private final Configuration configuration;

    public LootListener(NightListener nightListener, Configuration configuration) {
        this.nightListener = nightListener;
        this.configuration = configuration;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {

        if (!nightListener.isBloodNightActive(event.getDamager().getWorld())) return;

        Entity damager = event.getDamager();
        Entity entity = event.getEntity();

        // We just care about monsters and boss monsters
        if (!(entity instanceof Monster || entity instanceof Boss)) return;

        int entityId = entity.getEntityId();

        // Register player on entity
        if (damager instanceof Player) {
            lastDamage.put(entityId, (Player) damager);
            return;
        }

        ProjectileSender sender = ListenerUtil.getProjectileSource(damager);
        if (sender.isEntity() && sender.getEntity() instanceof Player) {
            lastDamage.put(entityId, (Player) sender.getEntity());
            return;
        }

        lastDamage.invalidate(entityId);
    }

    @EventHandler
    public void onEntityKill(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Player player = lastDamage.getIfPresent(entity.getEntityId());
        lastDamage.invalidate(entity.getEntityId());

        if (player == null) {
            return;
        }

        if (!(entity instanceof Monster || entity instanceof Boss)) return;


        if (!nightListener.isBloodNightActive(entity.getWorld())) return;

        NightSettings nightSettings = configuration.getWorldSettings(entity.getWorld()).getNightSettings();

        event.setDroppedExp((int) (event.getDroppedExp() * nightSettings.getExperienceMultiplier()));
        for (ItemStack drop : event.getDrops()) {
            drop.setAmount((int) (drop.getAmount() * nightSettings.getDropMultiplier()));
        }
    }
}
