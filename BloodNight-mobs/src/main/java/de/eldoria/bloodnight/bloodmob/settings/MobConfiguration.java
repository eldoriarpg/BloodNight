package de.eldoria.bloodnight.bloodmob.settings;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.eldoria.bloodnight.bloodmob.serialization.annotation.MapProperty;
import de.eldoria.bloodnight.bloodmob.serialization.annotation.MultiListProperty;
import de.eldoria.bloodnight.bloodmob.serialization.annotation.Property;
import de.eldoria.bloodnight.bloodmob.serialization.annotation.StringProperty;
import de.eldoria.bloodnight.bloodmob.serialization.value.ValueType;
import de.eldoria.bloodnight.config.ConfigCheck;
import de.eldoria.bloodnight.config.ConfigException;
import org.bukkit.entity.EntityType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MobConfiguration implements ConfigCheck<Object> {
    @StringProperty(name = "", descr = "", pattern = "^[a-z_]+$")
    String identifier;
    @MapProperty(name = "", descr = "", key = ValueType.LIST, value = ValueType.STRING)
    Map<EntityType, String> names = new HashMap<>();
    @MultiListProperty(name = "", descr = "")
    EntityType[] wrapTypes = new EntityType[0];
    @Property(name = "", descr = "")
    Extension extension = null;
    @Property(name = "", descr = "")
    Stats stats = new Stats();
    @Property(name = "", descr = "")
    Equipment equipment = new Equipment();
    @Property(name = "", descr = "")
    Drops drops = new Drops();
    @Property(name = "", descr = "")
    Behaviour behaviour = new Behaviour();

    public MobConfiguration() {
    }

    public MobConfiguration(String identifier) {
        this.identifier = identifier;
    }

    @JsonIgnore
    public boolean isExtended() {
        return extension != null;
    }

    @Override
    public void check(Object data) throws ConfigException {
        if (identifier == null) throw new ConfigException("Identifier is not set.");
        if (identifier.trim().isEmpty()) throw new ConfigException("Identifier is empty");
        extension.check();
        stats.check();
        drops.check();
        behaviour.check(this);
    }

    public Optional<String> getName(EntityType type) {
        String name = names.get(type);
        if (name == null || name.trim().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(name);
    }

    public String identifier() {
        return identifier;
    }

    public Extension extension() {
        return extension;
    }

    public Stats stats() {
        return stats;
    }

    public Equipment equipment() {
        return equipment;
    }

    public Drops drops() {
        return drops;
    }

    public Behaviour behaviour() {
        return behaviour;
    }

    public Map<EntityType, String> names() {
        return names;
    }

    public EntityType[] wrapTypes() {
        return wrapTypes;
    }

    public void identifier(String identifier) {
        this.identifier = identifier;
    }

    public void names(Map<EntityType, String> names) {
        this.names = names;
    }

    public void wrapTypes(EntityType[] wrapTypes) {
        this.wrapTypes = wrapTypes;
    }

    public void extension(Extension extension) {
        this.extension = extension;
    }

    public void stats(Stats stats) {
        this.stats = stats;
    }

    public void equipment(Equipment equipment) {
        this.equipment = equipment;
    }

    public void drops(Drops drops) {
        this.drops = drops;
    }

    public void behaviour(Behaviour behaviour) {
        this.behaviour = behaviour;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MobConfiguration that = (MobConfiguration) o;

        if (identifier != null ? !identifier.equals(that.identifier) : that.identifier != null) return false;
        if (names != null ? !names.equals(that.names) : that.names != null) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(wrapTypes, that.wrapTypes)) return false;
        if (extension != null ? !extension.equals(that.extension) : that.extension != null) return false;
        if (stats != null ? !stats.equals(that.stats) : that.stats != null) return false;
        if (equipment != null ? !equipment.equals(that.equipment) : that.equipment != null) return false;
        if (drops != null ? !drops.equals(that.drops) : that.drops != null) return false;
        return behaviour != null ? behaviour.equals(that.behaviour) : that.behaviour == null;
    }

    @Override
    public int hashCode() {
        int result = identifier != null ? identifier.hashCode() : 0;
        result = 31 * result + (names != null ? names.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(wrapTypes);
        result = 31 * result + (extension != null ? extension.hashCode() : 0);
        result = 31 * result + (stats != null ? stats.hashCode() : 0);
        result = 31 * result + (equipment != null ? equipment.hashCode() : 0);
        result = 31 * result + (drops != null ? drops.hashCode() : 0);
        result = 31 * result + (behaviour != null ? behaviour.hashCode() : 0);
        return result;
    }
}
