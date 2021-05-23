package de.eldoria.bloodnight.bloodmob.serialization.container;

import be.seeseemelk.mockbukkit.MockBukkit;
import com.fasterxml.jackson.core.JsonProcessingException;
import de.eldoria.bloodnight.bloodmob.nodeimpl.action.OtherPotion;
import de.eldoria.bloodnight.bloodmob.nodeimpl.filter.CooldownFilter;
import de.eldoria.bloodnight.bloodmob.nodeimpl.filter.PredicateFilter;
import de.eldoria.bloodnight.bloodmob.nodeimpl.mapper.MoveToLocation;
import de.eldoria.bloodnight.bloodmob.nodeimpl.predicate.HasTarget;
import de.eldoria.bloodnight.bloodmob.registry.MobRegistry;
import de.eldoria.bloodnight.bloodmob.registry.items.ItemRegistry;
import de.eldoria.bloodnight.bloodmob.serialization.mapper.MobMapper;
import de.eldoria.bloodnight.bloodmob.settings.BehaviourNode;
import de.eldoria.bloodnight.bloodmob.settings.MobConfiguration;
import de.eldoria.eldoutilities.items.ItemStackBuilder;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffectType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SettingsContainerTest {
    @Test
    public void serialize() throws JsonProcessingException {
        MockBukkit.mock();

        MobRegistry mobRegistry = new MobRegistry();
        MobConfiguration testMob = new MobConfiguration("test_mob");
        mobRegistry.register(testMob);
        OtherPotion otherPotion = new OtherPotion(PotionEffectType.BLINDNESS, 20, 2, true);
        CooldownFilter cooldownFilter = new CooldownFilter(10, otherPotion);
        PredicateFilter predicateFilter = new PredicateFilter(cooldownFilter, false, new HasTarget());
        MoveToLocation moveToLocation = new MoveToLocation(predicateFilter, MoveToLocation.LocationSource.OLD);

        testMob.behaviour().addNode(BehaviourNode.TICK, moveToLocation);
        testMob.behaviour().addNode(BehaviourNode.ON_KILL, moveToLocation);
        testMob.behaviour().addNode(BehaviourNode.ON_DAMAGE_BY_ENTITY, moveToLocation);

        ItemRegistry itemRegistry = new ItemRegistry();
        itemRegistry.register(ItemStackBuilder.of(Material.DIAMOND_BOOTS)
                .withEnchant(Enchantment.DURABILITY, 3, true)
                .build());
        itemRegistry.register(ItemStackBuilder.of(Material.DIAMOND_CHESTPLATE)
                .withLore("This is", "a nice", "Lore")
                .build());

        SettingsContainer initContainer = SettingsContainer.from(itemRegistry, mobRegistry);
        String serializedContainer = MobMapper.mapper().writerWithDefaultPrettyPrinter().writeValueAsString(initContainer);
        System.out.println(serializedContainer);

        SettingsContainer settingsContainer = MobMapper.mapper().readValue(serializedContainer, SettingsContainer.class);
        Assertions.assertEquals(initContainer, settingsContainer);
    }
}