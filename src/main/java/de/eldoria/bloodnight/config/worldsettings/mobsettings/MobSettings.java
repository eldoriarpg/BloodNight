package de.eldoria.bloodnight.config.worldsettings.mobsettings;

import de.eldoria.bloodnight.core.BloodNight;
import de.eldoria.bloodnight.core.mobfactory.MobFactory;
import de.eldoria.bloodnight.core.mobfactory.MobGroup;
import de.eldoria.bloodnight.core.mobfactory.SpecialMobRegistry;
import de.eldoria.eldoutilities.container.Pair;
import de.eldoria.eldoutilities.serialization.SerializationUtil;
import de.eldoria.eldoutilities.serialization.TypeResolvingMap;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Getter
@Setter
@SerializableAs("bloodNightMobSettings")
public class MobSettings implements ConfigurationSerializable {
    private VanillaMobSettings vanillaMobSettings = new VanillaMobSettings();
    /**
     * Enabled or disables mob names for special mobs.
     */
    private boolean displayMobNames = true;

    /**
     * The modifier which will be multiplied with monster damage when dealing damage to players.
     */
    private double damageMultiplier = 2;

    /**
     * The modifier which will be applied to special Mobs health on spawn.
     */
    private double healthModifier = 2;

    /**
     * The modifier which will be muliplied with the dropped exp of a monster.
     */
    private double experienceMultiplier = 4;


    /**
     * Sleep time will be set for every player when the nights starts and will be reset to earlier value when the night
     * ends
     */
    private boolean forcePhantoms = true;

    /**
     * The conversion rate of mobs. Higher numer -> more special mobs.
     */
    private int spawnPercentage = 80;

    /**
     * The general drops during blood night.
     */
    private List<Drop> defaultDrops = new ArrayList<>();

    /**
     * If true drops will be added to vanilla drops. If false vanilla drops will be removed.
     */
    private boolean naturalDrops = true;

    /**
     * Max Amount of custom drops which can be dropped on death.
     */
    private int dropAmount = 3;

    /**
     * List of mob type settings.
     */
    private MobTypes mobTypes = new MobTypes();

    public MobSettings(Map<String, Object> objectMap) {
        TypeResolvingMap map = SerializationUtil.mapOf(objectMap);
        vanillaMobSettings = map.getValueOrDefault("vanillaMobSettings", vanillaMobSettings);
        displayMobNames = map.getValueOrDefault("displayMobNames", displayMobNames);
        damageMultiplier = map.getValueOrDefault("damageMultiplier", damageMultiplier);
        healthModifier = map.getValueOrDefault("healthMultiplier", healthModifier);
        experienceMultiplier = map.getValueOrDefault("experienceMultiplier", experienceMultiplier);
        forcePhantoms = map.getValueOrDefault("forcePhantoms", forcePhantoms);
        spawnPercentage = map.getValueOrDefault("spawnPercentage", spawnPercentage);
        defaultDrops = map.getValueOrDefault("drops", defaultDrops);
        naturalDrops = map.getValueOrDefault("naturalDrops", naturalDrops);
        dropAmount = map.getValueOrDefault("dropAmount", dropAmount);
        mobTypes = map.getValueOrDefault("mobTypes", mobTypes);
    }

    public MobSettings() {
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        return SerializationUtil.newBuilder()
                .add("displayMobNames", displayMobNames)
                .add("monsterDamageMultiplier", damageMultiplier)
                .add("playerDamageMultiplier", healthModifier)
                .add("experienceMultiplier", experienceMultiplier)
                .add("forcePhantoms", forcePhantoms)
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

        return getDrops(totalDrops, 1, mobSetting.getOverridenDropAmount(dropAmount));
    }

    /**
     * Get the amount of drops from the default drops
     *
     * @param dropAmount max amount of drops
     *
     * @return list of length between 0 and drop amount
     */
    public List<ItemStack> getDrops(int dropAmount) {
        return getDrops(defaultDrops, 0, dropAmount);
    }

    /**
     * Get the amount of drops from a list of weighted drops
     *
     * @param totalDrops list of drops
     * @param dropAmount max amount of drops
     *
     * @return item stack list of length between 1 and drop amount
     */
    public List<ItemStack> getDrops(List<Drop> totalDrops, int minDrops, int dropAmount) {
        if (dropAmount == 0) return new ArrayList<>();

        int totalWeight = totalDrops.stream().mapToInt(Drop::getWeight).sum();

        ThreadLocalRandom current = ThreadLocalRandom.current();
        int nextInt = current.nextInt(minDrops, dropAmount + 1);
        List<ItemStack> result = new ArrayList<>();

        int currentWeight = 0;
        for (int i = 0; i < nextInt; i++) {
            int goal = current.nextInt(totalWeight + 1);
            for (Drop drop : totalDrops) {
                currentWeight += drop.getWeight();
                if (currentWeight < goal) continue;
                result.add(new ItemStack(drop.getItem().clone()));
                break;
            }
        }
        return result;
    }

    public Optional<MobSetting> getMobByName(String string) {
        Optional<MobFactory> mobFactoryByName = SpecialMobRegistry.getMobFactoryByName(string);
        if (!mobFactoryByName.isPresent()) return Optional.empty();
        String name = mobFactoryByName.get().getEntityType().getEntityClass().getSimpleName();
        for (MobSetting entry : mobTypes.mobSettings.getOrDefault(name, Collections.emptySet())) {
            if (string.equalsIgnoreCase(entry.getMobName())) {
                return Optional.of(entry);
            }
        }
        return Optional.empty();
    }

    @SerializableAs("bloodNightMobTypes")
    public static class MobTypes implements ConfigurationSerializable {

        /**
         * List of mob type settings.
         */
        private Map<String, Set<MobSetting>> mobSettings = new HashMap<>();

        public MobTypes() {
            this.mobSettings = SpecialMobRegistry.getMobGroups().entrySet().stream()
                    .map(m -> {
                        Pair<String, Set<MobSetting>> pair = new Pair<>(m.getKey().getSimpleName(), new HashSet<>());
                        m.getValue().getFactories().forEach(v -> pair.second.add(new MobSetting(v.getMobName())));
                        return pair;
                    })
                    .collect(Collectors.toMap(p -> p.first, p -> p.second));
        }

        public MobTypes(Map<String, Object> objectMap) {
            TypeResolvingMap map = SerializationUtil.mapOf(objectMap);

            for (Map.Entry<Class<? extends Entity>, MobGroup> entry : SpecialMobRegistry.getMobGroups().entrySet()) {
                Set<MobSetting> mobSettings = this.mobSettings.computeIfAbsent(entry.getKey().getSimpleName(), k -> new HashSet<>());
                List<MobSetting> valueOrDefault = map.getValueOrDefault(entry.getKey().getSimpleName(), new ArrayList<>());

                // only load settings for valid mobs
                for (MobFactory factory : entry.getValue().getFactories()) {
                    for (MobSetting mobSetting : valueOrDefault) {
                        // check if a setting is already registered
                        if (mobSetting.getMobName().equalsIgnoreCase(factory.getMobName())) {
                            mobSettings.add(mobSetting);
                            break;
                        }
                    }
                    // create default settings
                    mobSettings.add(new MobSetting(factory.getMobName()));
                    BloodNight.logger().info(String.format("No settings for {} found. Creating default settings.", factory.getMobName()));
                }
            }
        }

        @Override
        public @NotNull Map<String, Object> serialize() {
            SerializationUtil.Builder builder = SerializationUtil.newBuilder();
            for (Map.Entry<String, Set<MobSetting>> entry : mobSettings.entrySet()) {
                builder.add(entry.getKey(), new ArrayList<>(entry.getValue()));
            }
            return builder.build();
        }

        /**
         * Returns a optional of a mob group.
         *
         * @param groupName name of group
         *
         * @return optional result set. Key represents the mob group and value a set of mob settings.
         */
        public Optional<Map.Entry<String, Set<MobSetting>>> getGroup(String groupName) {
            return mobSettings.entrySet().stream()
                    .filter(e -> e.getKey().equalsIgnoreCase(groupName))
                    .findFirst();
        }

        public Set<MobSetting> getSettings() {
            Set<MobSetting> settings = new HashSet<>();
            mobSettings.values().forEach(settings::addAll);
            return settings;
        }
    }
}
