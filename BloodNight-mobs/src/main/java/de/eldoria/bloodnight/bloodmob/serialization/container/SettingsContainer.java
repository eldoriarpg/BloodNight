package de.eldoria.bloodnight.bloodmob.serialization.container;

import de.eldoria.bloodnight.bloodmob.registry.MobRegistry;
import de.eldoria.bloodnight.bloodmob.registry.items.ItemRegistry;
import de.eldoria.bloodnight.bloodmob.registry.items.SimpleItem;
import de.eldoria.bloodnight.bloodmob.settings.MobConfiguration;

import java.util.List;

public class SettingsContainer {
    List<MobConfiguration> configurations;
    List<SimpleItem> items;

    public SettingsContainer() {
    }

    private SettingsContainer(List<MobConfiguration> configurations, List<SimpleItem> items) {
        this.configurations = configurations;
        this.items = items;
    }

    public static SettingsContainer from(ItemRegistry itemRegistry, MobRegistry mobRegistry) {
        List<SimpleItem> asSimpleItems = itemRegistry.getAsSimpleItems();
        List<MobConfiguration> configurations = mobRegistry.getConfigurations();
        return new SettingsContainer(configurations, asSimpleItems);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SettingsContainer that = (SettingsContainer) o;

        if (!configurations.equals(that.configurations)) return false;
        return items.equals(that.items);
    }

    @Override
    public int hashCode() {
        int result = configurations.hashCode();
        result = 31 * result + items.hashCode();
        return result;
    }
}
