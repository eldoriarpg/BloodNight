package de.eldoria.bloodnight.specialmob.settings;

import de.eldoria.bloodnight.config.ConfigCheck;
import de.eldoria.bloodnight.config.ConfigException;
import lombok.Getter;
import org.bukkit.entity.EntityType;

import java.util.Map;
import java.util.Optional;

@Getter
public class MobConfiguration implements ConfigCheck {
    String identifier;
    Map<EntityType, String> names;
    EntityType[] wrapTypes;
    Extension extension;
    Stats stats;
    Equipment equipment;
    Drops drops;
    Behaviour behaviour;

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
}
