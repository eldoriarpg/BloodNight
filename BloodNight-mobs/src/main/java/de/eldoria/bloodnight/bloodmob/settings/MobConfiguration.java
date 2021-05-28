package de.eldoria.bloodnight.bloodmob.settings;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.eldoria.bloodnight.bloodmob.serialization.annotation.MapProperty;
import de.eldoria.bloodnight.bloodmob.serialization.annotation.Property;
import de.eldoria.bloodnight.bloodmob.serialization.annotation.StringProperty;
import de.eldoria.bloodnight.bloodmob.serialization.value.ValueType;
import de.eldoria.bloodnight.bloodmob.settings.mobsettings.BloodMobType;
import de.eldoria.bloodnight.bloodmob.settings.mobsettings.TypeSetting;
import de.eldoria.bloodnight.config.ConfigCheck;
import de.eldoria.bloodnight.config.ConfigException;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class MobConfiguration implements ConfigCheck<Object> {
    @StringProperty(name = "", descr = "", pattern = "^[a-z_]+$")
    String identifier;
    @MapProperty(name = "", descr = "", key = ValueType.LIST, value = ValueType.STRING)
    Map<BloodMobType, TypeSetting> wrapTypes = new HashMap<>();
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
        this.identifier = identifier.toLowerCase(Locale.ROOT);
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


    public void identifier(String identifier) {
        this.identifier = identifier;
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

    public Map<BloodMobType, TypeSetting> wrapTypes() {
        return wrapTypes;
    }

    public void wrapTypes(Map<BloodMobType, TypeSetting> wrapTypes) {
        this.wrapTypes = wrapTypes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MobConfiguration that = (MobConfiguration) o;

        if (!Objects.equals(identifier, that.identifier)) return false;
        if (!Objects.equals(wrapTypes, that.wrapTypes)) return false;
        if (!Objects.equals(extension, that.extension)) return false;
        if (!Objects.equals(stats, that.stats)) return false;
        if (!Objects.equals(equipment, that.equipment)) return false;
        if (!Objects.equals(drops, that.drops)) return false;
        return Objects.equals(behaviour, that.behaviour);
    }

    @Override
    public int hashCode() {
        int result = identifier != null ? identifier.hashCode() : 0;
        result = 31 * result + (wrapTypes != null ? wrapTypes.hashCode() : 0);
        result = 31 * result + (extension != null ? extension.hashCode() : 0);
        result = 31 * result + (stats != null ? stats.hashCode() : 0);
        result = 31 * result + (equipment != null ? equipment.hashCode() : 0);
        result = 31 * result + (drops != null ? drops.hashCode() : 0);
        result = 31 * result + (behaviour != null ? behaviour.hashCode() : 0);
        return result;
    }
}
