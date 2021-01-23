package de.eldoria.bloodnight.hooks.mythicmobs;

import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobSpawnEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MythicMobTagger implements Listener {

    /**
     * Mark mobs spawned by MithicMobs since this plugin dont provide a way to identify...
     *
     * @param event spawn event
     */
    @EventHandler
    public void onSpawn(MythicMobSpawnEvent event) {
        MythicMobUtil.tagMob(event.getEntity());
    }
}
