package de.eldoria.bloodnight.hooks.placeholderapi;

import de.eldoria.bloodnight.hooks.AbstractHookService;
import me.clip.placeholderapi.PlaceholderAPIPlugin;

public class PlaceholderAPIHook extends AbstractHookService<PlaceholderAPIPlugin> {
	private Placeholders placeholders;

	public PlaceholderAPIHook() {
		super("PlaceholderAPI");
	}

	@Override
	public PlaceholderAPIPlugin getHook() throws ClassNotFoundException {
		return PlaceholderAPIPlugin.getInstance();
	}

	@Override
	public void setup() {
		placeholders = new Placeholders();
		placeholders.register();
	}

	@Override
	public void shutdown() {
		placeholders.unregister();
	}
}
