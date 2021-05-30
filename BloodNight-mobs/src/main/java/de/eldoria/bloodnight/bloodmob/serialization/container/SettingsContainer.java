package de.eldoria.bloodnight.bloodmob.serialization.container;

import de.eldoria.bloodnight.bloodmob.drop.Drop;
import de.eldoria.bloodnight.bloodmob.registry.MobRegistry;
import de.eldoria.bloodnight.bloodmob.registry.items.ItemRegistry;
import de.eldoria.bloodnight.bloodmob.registry.items.SimpleItem;
import de.eldoria.bloodnight.bloodmob.settings.MobConfiguration;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class SettingsContainer {
    List<MobConfiguration> configurations;
    List<SimpleItem> items;
    List<Drop> globalDrops;

    public SettingsContainer() {
    }

    private SettingsContainer(List<MobConfiguration> configurations, List<SimpleItem> items, List<Drop> globalDrops) {
        this.configurations = configurations;
        this.items = items;
        this.globalDrops = globalDrops;
    }

    public static SettingsContainer from(ItemRegistry itemRegistry, MobRegistry mobRegistry, List<Drop> globalDrops) {
        List<SimpleItem> asSimpleItems = itemRegistry.getAsSimpleItems();
        List<MobConfiguration> configurations = mobRegistry.getConfigurations();
        return new SettingsContainer(configurations, asSimpleItems, globalDrops);
    }

    public List<MobConfiguration> configurations() {
        return configurations;
    }

    public List<SimpleItem> items() {
        return items;
    }

    public List<Drop> globalDrops() {
        return globalDrops;
    }

    public List<String> mobIdentifier() {
        return configurations.stream().map(MobConfiguration::identifier).collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SettingsContainer that = (SettingsContainer) o;

        if (!Objects.equals(configurations, that.configurations)) return false;
        if (!Objects.equals(items, that.items)) return false;
        return Objects.equals(globalDrops, that.globalDrops);
    }

    @Override
    public int hashCode() {
        int result = configurations != null ? configurations.hashCode() : 0;
        result = 31 * result + (items != null ? items.hashCode() : 0);
        result = 31 * result + (globalDrops != null ? globalDrops.hashCode() : 0);
        return result;
    }

    public Optional<MobConfiguration> getMobConfig(String identifier) {
        return configurations.stream().filter(c -> c.identifier().equals(identifier)).findFirst();
    }

    public boolean mobExists(String identifier) {
        return getMobConfig(identifier).isPresent();
    }

    public void createMob(String identifier) {
        configurations.add(new MobConfiguration(identifier));
    }

    public boolean removeMob(String identifier) {
        return configurations.removeIf(c -> c.identifier().equals(identifier));
    }
}
