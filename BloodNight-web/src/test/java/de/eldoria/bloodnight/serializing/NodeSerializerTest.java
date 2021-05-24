package de.eldoria.bloodnight.serializing;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.eldoria.bloodnight.bloodmob.nodeimpl.action.OtherPotion;
import de.eldoria.bloodnight.bloodmob.nodeimpl.filter.CooldownFilter;
import de.eldoria.bloodnight.bloodmob.nodeimpl.filter.PredicateFilter;
import de.eldoria.bloodnight.bloodmob.nodeimpl.mapper.MoveToLocation;
import de.eldoria.bloodnight.bloodmob.nodeimpl.predicate.HasTarget;
import de.eldoria.bloodnight.bloodmob.serialization.mapper.MobMapper;
import de.eldoria.bloodnight.serialization.NodeData;
import org.bukkit.potion.PotionEffectType;
import org.junit.jupiter.api.Test;

public class NodeSerializerTest {
    @Test
    public void test() throws JsonProcessingException {
        var otherPotion = new OtherPotion(PotionEffectType.BLINDNESS, 20, 2, true);
        var cooldownFilter = new CooldownFilter(10, otherPotion);
        var predicateFilter = new PredicateFilter(cooldownFilter, false, new HasTarget());
        var moveToLocation = new MoveToLocation(predicateFilter, MoveToLocation.LocationSource.OLD);

        var data = NodeData.of(moveToLocation);
        var serialize = MobMapper.mapper()
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(data);

        System.out.println(serialize);
    }
}
