package de.eldoria.bloodnight.serializing;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Slime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.stream.Collectors;

public class MobCheck {
    @Test
    public void findMobs() {
        var collect = Arrays.stream(EntityType.values())
                .filter(e -> e.getEntityClass() != null)
                .filter(e -> Mob.class.isAssignableFrom(e.getEntityClass()))
                .map(e -> e.name() + "(EntityType."+ e.name() +", TypeSetting.class)")
                .sorted()
                .collect(Collectors.joining(",\n"));
        System.out.println(collect);
    }

    @Test
    public void testMob() {
        Assertions.assertTrue(Mob.class.isAssignableFrom(Slime.class));
    }
}
