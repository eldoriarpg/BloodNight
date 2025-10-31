package de.eldoria.bloodnight.hooks.worldmanager;

import de.eldoria.bloodnight.hooks.AbstractHookService;
import org.bukkit.World;
import org.mvplugins.multiverse.core.MultiverseCore;
import org.mvplugins.multiverse.core.MultiverseCoreApi;

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
            getHook();
            return MultiverseCoreApi.get().getWorldManager().getWorld(world).get().getAlias();
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            return world.getName();
        }
    }
}
