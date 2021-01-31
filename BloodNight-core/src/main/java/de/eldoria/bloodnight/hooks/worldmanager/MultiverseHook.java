package de.eldoria.bloodnight.hooks.worldmanager;

import com.onarandombox.MultiverseCore.MultiverseCore;
import de.eldoria.bloodnight.hooks.AbstractHookService;
import org.bukkit.World;

public class MultiverseHook extends AbstractHookService<MultiverseCore> implements WorldManager {
    private MultiverseCore plugin = null;

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
            return getHook().getMVWorldManager().getMVWorld(world).getAlias();
        } catch (ClassNotFoundException e) {
            return world.getName();
        }
    }
}
