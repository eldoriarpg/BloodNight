package de.eldoria.bloodnight.hooks;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

/**
 * A hook for a plugin.
 *
 * @param <T> type of plugin hook.
 */
public abstract class AbstractHookService<T extends Plugin> {
    private final String name;

    /**
     * True if the plugin of this hook is enabled.
     */
    @Getter
    private final boolean active;

    public AbstractHookService(String name) {
        this.name = name;
        active = Bukkit.getPluginManager().isPluginEnabled(name);
    }

    /**
     * Get the Hook of the Plugin.
     *
     * @return hook instance.
     * @throws ClassNotFoundException when the plugin is not loaded.
     */
    public abstract T getHook() throws ClassNotFoundException;

    /**
     * Initialize the hook
     */
    public abstract void setup();

    /**
     * Shutdown the hook and stop any attached services.
     */
    public abstract void shutdown();
}
