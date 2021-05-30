package de.eldoria.bloodnight.serializing;

import be.seeseemelk.mockbukkit.MockBukkit;
import com.fasterxml.jackson.core.JsonProcessingException;
import de.eldoria.bloodnight.bloodmob.drop.Drop;
import de.eldoria.bloodnight.bloodmob.nodeimpl.action.OtherPotion;
import de.eldoria.bloodnight.bloodmob.nodeimpl.filter.CooldownFilter;
import de.eldoria.bloodnight.bloodmob.nodeimpl.filter.PredicateFilter;
import de.eldoria.bloodnight.bloodmob.nodeimpl.mapper.MoveToLocation;
import de.eldoria.bloodnight.bloodmob.nodeimpl.predicate.HasTarget;
import de.eldoria.bloodnight.bloodmob.registry.MobRegistry;
import de.eldoria.bloodnight.bloodmob.registry.items.ItemRegistry;
import de.eldoria.bloodnight.bloodmob.serialization.container.MobEditorPayload;
import de.eldoria.bloodnight.bloodmob.serialization.container.SettingsContainer;
import de.eldoria.bloodnight.bloodmob.serialization.mapper.MobMapper;
import de.eldoria.bloodnight.bloodmob.settings.BehaviourNodeType;
import de.eldoria.bloodnight.bloodmob.settings.MobConfiguration;
import de.eldoria.bloodnight.serialization.DataDescriptionContainer;
import de.eldoria.bloodnight.util.ClassDefintionUtil;
import de.eldoria.eldoutilities.items.ItemStackBuilder;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffectType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

class SettingsContainerTest {

    private static MobRegistry mobRegistry;
    public static MobConfiguration testMob;
    private static ItemRegistry itemRegistry;

    @BeforeAll
    public static void setup() {
        MockBukkit.mock();

        mobRegistry = new MobRegistry();
        testMob = new MobConfiguration("test_mob");
        mobRegistry.register(testMob);
        var otherPotion = new OtherPotion(PotionEffectType.BLINDNESS, 20, 2, true);
        var cooldownFilter = new CooldownFilter(10, otherPotion);
        var predicateFilter = new PredicateFilter(cooldownFilter, false, new HasTarget());
        var moveToLocation = new MoveToLocation(predicateFilter, MoveToLocation.LocationSource.OLD);

        testMob.behaviour().addNode(BehaviourNodeType.TICK, moveToLocation);
        testMob.behaviour().addNode(BehaviourNodeType.ON_KILL, moveToLocation);
        testMob.behaviour().addNode(BehaviourNodeType.ON_DAMAGE_BY_ENTITY, moveToLocation);

        itemRegistry = new ItemRegistry();
        itemRegistry.register(ItemStackBuilder.of(Material.DIAMOND_BOOTS)
                .withEnchant(Enchantment.DURABILITY, 3, true)
                .build());
        itemRegistry.register(ItemStackBuilder.of(Material.DIAMOND_CHESTPLATE)
                .withLore("This is", "a nice", "Lore")
                .build());
    }

    @Test
    public void settingContainerSerialization() throws JsonProcessingException {
        var drops = new ArrayList<Drop>();
        drops.add(new Drop(0, 5, 10));
        drops.add(new Drop(0, 2, 10));
        drops.add(new Drop(2, 12, 80));

        var initContainer = SettingsContainer.from(itemRegistry, mobRegistry, drops);
        var mobEditorPayload = new MobEditorPayload(initContainer);
        var serializedContainer = MobMapper.mapper().writerWithDefaultPrettyPrinter().writeValueAsString(initContainer);
        System.out.println(serializedContainer);

        var settingsContainer = MobMapper.mapper().readValue(serializedContainer, SettingsContainer.class);
        Assertions.assertEquals(initContainer.hashCode(), settingsContainer.hashCode());

        var of = DataDescriptionContainer.of(testMob);
        var s = MobMapper.mapper().writeValueAsString(of);
        System.out.println(s);

        var serMobEditorPayload = MobMapper.mapper().writeValueAsString(mobEditorPayload);
        System.out.println(serMobEditorPayload);
    }

    @Test
    public void behaviourSerialization() throws JsonProcessingException {
        var nodeMap = testMob.behaviour().behaviourMap();

        var definitions = ClassDefintionUtil.getBehaviourDefinitions(testMob.behaviour());

        var container = DataDescriptionContainer.of(nodeMap, definitions);
        var s = MobMapper.mapper().writeValueAsString(container);
        System.out.println(s);
    }

    @Test
    public void itemSerialization() throws JsonProcessingException {
        var s = MobMapper.mapper().writeValueAsString(itemRegistry.getAsSimpleItems());
        System.out.println(s);
    }
}