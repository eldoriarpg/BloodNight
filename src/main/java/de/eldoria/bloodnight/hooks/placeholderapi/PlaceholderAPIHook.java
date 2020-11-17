package de.eldoria.bloodnight.hooks.placeholderapi;

import de.eldoria.bloodnight.config.Configuration;
import de.eldoria.bloodnight.core.manager.NightManager;
import de.eldoria.bloodnight.hooks.AbstractHookService;
import me.clip.placeholderapi.PlaceholderAPIPlugin;

public class PlaceholderAPIHook extends AbstractHookService<PlaceholderAPIPlugin> {
	private final Configuration configuration;
	private final NightManager nightManager;
	private Placeholders placeholders;

	public PlaceholderAPIHook(Configuration configuration, NightManager nightManager) {
		super("PlaceholderAPI");
		this.configuration = configuration;
		this.nightManager = nightManager;
	}

	@Override
	public PlaceholderAPIPlugin getHook() throws ClassNotFoundException {
		return PlaceholderAPIPlugin.getInstance();
	}

	@Override
	public void setup() {
		placeholders = new Placeholders(nightManager, configuration);
		placeholders.register();
	}

	@Override
	public void shutdown() {
		placeholders.unregister();
	}
}
