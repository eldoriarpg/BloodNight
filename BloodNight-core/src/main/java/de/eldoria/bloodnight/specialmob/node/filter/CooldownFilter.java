package de.eldoria.bloodnight.specialmob.node.filter;

import de.eldoria.bloodnight.specialmob.ISpecialMob;
import de.eldoria.bloodnight.specialmob.node.context.IActionContext;
import de.eldoria.eldoutilities.serialization.SerializationUtil;
import de.eldoria.eldoutilities.serialization.TypeResolvingMap;
import de.eldoria.eldoutilities.utils.EnumUtil;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@Getter
public class CooldownFilter extends FilterNode {
    private Duration duration;
    private Instant last = Instant.now();


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
    public boolean check(ISpecialMob mob, IActionContext context) {
        if (last.plus(duration).isAfter(Instant.now())) {
            last = Instant.now();
            return true;
        }
        return false;
    }
}