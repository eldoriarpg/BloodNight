package de.eldoria.bloodnight.config;

import de.eldoria.bloodnight.core.BloodNight;
import de.eldoria.eldoutilities.serialization.SerializationUtil;
import de.eldoria.eldoutilities.serialization.TypeResolvingMap;
import de.eldoria.eldoutilities.utils.Parser;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Getter
@SerializableAs("bloodNightMobSetting")
public class MobSetting implements ConfigurationSerializable {
    @Setter
    private boolean active;
    @Setter
    private int dropAmount;
    private final boolean blockNaturalDrops;
    private final List<Drop> drops = new ArrayList<>();
    private final int totalWeight;

    public MobSetting(Map<String, Object> objectMap) {
        TypeResolvingMap map = SerializationUtil.mapOf(objectMap);
        active = map.getValueOrDefault("active", true);
        dropAmount = map.getValueOrDefault("dropAmount", 0);
        blockNaturalDrops = map.getValueOrDefault("blockNaturalDrops", false);
        List<String> drops = (List<String>) map.getOrDefault("drops", Collections.emptyList());
        for (String drop : drops) {
            String[] split = drop.split(",");
            Material material;
            int amount = 1;
            int weight = 1;
            if (split.length == 0) continue;

            material = Material.getMaterial(split[0].toUpperCase());
            if (material == null) {
                BloodNight.logger().warning("Could not parse drop " + drop + ". Invalid material.");
                continue;
            }

            if (split.length > 1) {
                OptionalInt optionalInt = Parser.parseInt(split[1]);
                if (!optionalInt.isPresent()) {
                    BloodNight.logger().warning("Could not parse drop " + drop + ". Invalid amount.");
                    continue;
                }
                amount = optionalInt.getAsInt();
            }

            if (split.length > 2) {
                OptionalInt optionalInt = Parser.parseInt(split[2]);
                if (!optionalInt.isPresent()) {
                    BloodNight.logger().warning("Could not parse drop " + drop + ". Invalid weight.");
                    continue;
                }
                weight = optionalInt.getAsInt();
            }

            this.drops.add(new Drop(material, amount, weight));
        }
        totalWeight = this.drops.stream().mapToInt(Drop::getWeight).sum();
    }

    public MobSetting() {
        active = true;
        dropAmount = 0;
        blockNaturalDrops = false;
        totalWeight = 0;
    }

    public List<ItemStack> getDrops() {
        List<ItemStack> result = new ArrayList<>();
        ThreadLocalRandom current = ThreadLocalRandom.current();
        int currentWeight = 0;
        for (int i = 0; i < dropAmount; i++) {
            int goal = current.nextInt(totalWeight + 1);
            for (Drop drop : drops) {
                currentWeight += drop.getWeight();
                if (currentWeight < goal) continue;
                result.add(new ItemStack(drop.getMaterial(), drop.getAmount()));
                break;
            }
        }
        return result;
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        return SerializationUtil.newBuilder()
                .add("active", active)
                .add("dropAmount", dropAmount)
                .add("drops", drops.stream()
                        .map(d -> d.material + "," + d.amount + "," + d.weight)
                        .collect(Collectors.toList()))
                .build();
    }


    @Getter
    private static class Drop {
        private final Material material;
        private final int amount;
        private final int weight;

        public Drop(Material material, int amount, int weight) {
            this.material = material;
            this.amount = amount;
            this.weight = weight;
        }
    }
}
