package de.eldoria.bloodnight.bloodmob.nodeimpl.filter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.eldoria.bloodnight.bloodmob.IBloodMob;
import de.eldoria.bloodnight.bloodmob.node.Node;
import de.eldoria.bloodnight.bloodmob.node.contextcontainer.ContextContainer;
import de.eldoria.bloodnight.bloodmob.node.filter.FilterNode;
import de.eldoria.bloodnight.bloodmob.serialization.annotation.Property;
import de.eldoria.eldoutilities.serialization.SerializationUtil;
import de.eldoria.eldoutilities.serialization.TypeResolvingMap;
import de.eldoria.eldoutilities.utils.EnumUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@Getter
@NoArgsConstructor
public class CooldownFilter extends FilterNode {
    @Property(name = "", descr = "")
    private Duration duration;
    @JsonIgnore
    private Instant last = Instant.now();

    public CooldownFilter(int duration, Node nextNode) {
        super(nextNode);
        this.duration = Duration.of(duration, ChronoUnit.SECONDS);
    }

    public CooldownFilter(Map<String, Object> objectMap) {
        super(objectMap);
        TypeResolvingMap map = SerializationUtil.mapOf(objectMap);
        duration = Duration.of(
                map.getValue("duration"),
                EnumUtil.parse(map.getValueOrDefault("unit", ChronoUnit.SECONDS.name()), ChronoUnit.class));
    }

    @Override
    @NotNull
    public Map<String, Object> serialize() {
        return SerializationUtil.newBuilder(super.serialize())
                .add("duration", duration.getSeconds())
                .add("unit", ChronoUnit.SECONDS.name())
                .build();
    }

    @Override
    public boolean check(IBloodMob mob, ContextContainer context) {
        if (last.plus(duration).isAfter(Instant.now())) {
            last = Instant.now();
            return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CooldownFilter that = (CooldownFilter) o;

        if (duration != null ? !duration.equals(that.duration) : that.duration != null) return false;
        return last != null ? last.equals(that.last) : that.last == null;
    }

    @Override
    public int hashCode() {
        int result = duration != null ? duration.hashCode() : 0;
        result = 31 * result + (last != null ? last.hashCode() : 0);
        return result;
    }
}