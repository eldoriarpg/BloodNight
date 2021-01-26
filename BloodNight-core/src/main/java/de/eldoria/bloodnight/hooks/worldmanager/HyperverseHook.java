package de.eldoria.bloodnight.hooks.worldmanager;

import de.eldoria.bloodnight.hooks.AbstractHookService;
import org.bukkit.World;
import se.hyperver.hyperverse.Hyperverse;
import se.hyperver.hyperverse.HyperverseAPI;

public class HyperverseHook extends AbstractHookService<HyperverseAPI> implements WorldManager {
    public HyperverseHook() {
        super("Hyperverse");
    }

    @Override
    public HyperverseAPI getHook() throws ClassNotFoundException {
        return Hyperverse.getApi();
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
            return getHook().getWorldManager().getWorld(world).getDisplayName();
        } catch (ClassNotFoundException e) {
            return world.getName();
        }
    }
}
