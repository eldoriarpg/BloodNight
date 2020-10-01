package de.eldoria.bloodnight.config;

import de.eldoria.bloodnight.core.mobfactory.MobFactory;
import de.eldoria.bloodnight.core.mobfactory.SpecialMobRegistry;
import de.eldoria.eldoutilities.serialization.SerializationUtil;
import de.eldoria.eldoutilities.serialization.TypeResolvingMap;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Getter
@Setter
@SerializableAs("bloodNightMobSettings")
public class MobSettings implements ConfigurationSerializable {

    /**
     * The conversion rate of mobs. Higher numer -> more special mobs.
     */
    private int spawnPercentage = 80;
    /**
     * The general drops during blood night.
     */
    private List<Drop> defaultDrops = new ArrayList<>();

    /**
     * If true drops will be added to vanilla drops.
     * If false vanilla drops will be removed.
     */
    private boolean naturalDrops = true;

    private int dropAmount = 2;
    /**
     * List of mob type settings.
     */
    private List<MobSetting> mobTypes = SpecialMobRegistry.getRegisteredMobs().stream()
            .map(m -> new MobSetting(m.getMobName()))
            .collect(Collectors.toList());

    public MobSettings(Map<String, Object> objectMap) {
        TypeResolvingMap map = SerializationUtil.mapOf(objectMap);
        spawnPercentage = map.getValueOrDefault("spawnPercentage", spawnPercentage);
        defaultDrops = map.getValueOrDefault("drops", defaultDrops);
        naturalDrops = map.getValueOrDefault("naturalDrops", naturalDrops);
        dropAmount = map.getValueOrDefault("dropAmount", dropAmount);
        mobTypes = map.getValueOrDefault("mobTypes", mobTypes);

        // add not present mobs
        for (MobFactory value : SpecialMobRegistry.getRegisteredMobs()) {
            if (mobTypes.contains(new MobSetting(value.getMobName()))) continue;
            mobTypes.add(new MobSetting(value.getMobName()));
        }
    }

    public MobSettings() {
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        return SerializationUtil.newBuilder()
                .add("spawnPercentage", spawnPercentage)
                .add("drops", defaultDrops)
                .add("naturalDrops", naturalDrops)
                .add("dropAmount", dropAmount)
                .add("mobTypes", mobTypes)
                .build();
    }

    public boolean isActive(String mobName) {
        return getMobByName(mobName).map(MobSetting::isActive).orElse(false);
    }

    public List<ItemStack> getDrops(MobSetting mobSetting) {
        List<Drop> totalDrops = new ArrayList<>(mobSetting.getDrops());
        if (!mobSetting.isOverrideDefaultDrops()) {
            totalDrops.addAll(defaultDrops);
        }

        int totalWeight = totalDrops.stream().mapToInt(Drop::getWeight).sum();

        ThreadLocalRandom current = ThreadLocalRandom.current();

        List<ItemStack> result = new ArrayList<>();

        int currentWeight = 0;
        for (int i = 0; i < mobSetting.getOverridenDropAmount(dropAmount); i++) {
            int goal = current.nextInt(totalWeight + 1);
            for (Drop drop : defaultDrops) {
                currentWeight += drop.getWeight();
                if (currentWeight < goal) continue;
                result.add(new ItemStack(drop.getItem().clone()));
                break;
            }
        }
        return result;
    }

    public Optional<MobSetting> getMobByName(String string) {
        for (MobSetting entry : mobTypes) {
            if (string.equalsIgnoreCase(entry.getMobName())) {
                return Optional.of(entry);
            }
        }
        return Optional.empty();
    }
}
