package de.eldoria.bloodnight.bloodmob.registry;

import de.eldoria.bloodnight.bloodmob.settings.MobConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MobRegistry {
    private final Map<String, MobConfiguration> configurations = new HashMap<>();

    public List<MobConfiguration> getConfigurations() {
        return new ArrayList<>(configurations.values());
    }

    public void register(MobConfiguration mobConfiguration) {
        configurations.put(mobConfiguration.identifier(), mobConfiguration);
    }
}
