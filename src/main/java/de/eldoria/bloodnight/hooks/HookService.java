package de.eldoria.bloodnight.hooks;

import com.onarandombox.MultiverseCore.MultiverseCore;
import de.eldoria.bloodnight.config.Configuration;
import de.eldoria.bloodnight.core.BloodNight;
import de.eldoria.bloodnight.core.manager.NightManager;
import de.eldoria.bloodnight.hooks.mythicmobs.MythicMobsHook;
import de.eldoria.bloodnight.hooks.placeholderapi.PlaceholderAPIHook;
import de.eldoria.bloodnight.hooks.worldmanager.HyperverseHook;
import de.eldoria.bloodnight.hooks.worldmanager.MultiverseHook;
import de.eldoria.bloodnight.hooks.worldmanager.WorldManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import se.hyperver.hyperverse.Hyperverse;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Level;

@Getter
public class HookService {
	private final Map<Class<?>, AbstractHookService<?>> hooks = new HashMap<>();
	private final Plugin plugin;
	private final Configuration configuration;
	private final NightManager nightManager;

	public HookService(Plugin plugin, Configuration configuration, NightManager nightManager) {
		this.plugin = plugin;
		this.configuration = configuration;
		this.nightManager = nightManager;
	}

	public void setup() {
		add("MythicMobs", MythicMobsHook::new);
		add("PlaceholderAPI", () -> new PlaceholderAPIHook(configuration, nightManager));
		add("Multiverse-Core", MultiverseHook::new);
		add("Hyperverse", HyperverseHook::new);
	}

	public void add(String name, Callable<AbstractHookService<?>> hook) {
		if (!Bukkit.getPluginManager().isPluginEnabled(name)) {
			plugin.getLogger().info("Hook into " + name + " failed. Plugin is not enabled.");
			return;
		}
		AbstractHookService<?> call;
		try {
			call = hook.call();
		} catch (Exception e) {
			plugin.getLogger().log(Level.WARNING, "Failed to create hook for " + name + ". Is the plugin up to date?");
			return;
		}
		if (call.isActive()) {
			call.setup();
			hooks.put(call.getClass(), call);
		}
		plugin.getLogger().info("Hook into " + name + " successful.");
	}

	public void shutdown() {
		BloodNight.logger().info("Hooks shutting down.");
		hooks.forEach((k, v) -> v.shutdown());
		hooks.clear();
	}

	public WorldManager getWorldManager() {
		if (hooks.containsKey(HyperverseHook.class)) {
			return ((WorldManager) hooks.get(HyperverseHook.class));
		}
		if (hooks.containsKey(MultiverseHook.class)) {
			return ((WorldManager) hooks.get(MultiverseHook.class));
		}
		return WorldManager.DEFAULT;
	}
}