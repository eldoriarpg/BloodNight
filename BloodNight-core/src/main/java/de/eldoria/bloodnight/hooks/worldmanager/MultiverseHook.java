package de.eldoria.bloodnight.hooks.worldmanager;

import de.eldoria.bloodnight.hooks.AbstractHookService;
import org.bukkit.World;
import org.mvplugins.multiverse.core.MultiverseCore;

public class MultiverseHook extends AbstractHookService<MultiverseCore> implements WorldManager {
    private MultiverseCore plugin;

    public MultiverseHook() {
        super("Multiverse-Core");
    }

    @Override
    public MultiverseCore getHook() throws ClassNotFoundException {
        if (plugin == null) {
            plugin = MultiverseCore.getPlugin(MultiverseCore.class);
        }
        return plugin;
    }

    @Override
    public void setup() {

    }

    @Override
    public void shutdown() {

    }

    @Override
    public String getAlias(World world) {
        try {
            return getHook().getApi().getWorldManager().getWorld(world).get().getAlias();
        } catch (ClassNotFoundException e) {
            return world.getName();
        }
    }
}
