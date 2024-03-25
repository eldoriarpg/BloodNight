package de.eldoria.bloodnight.core.manager.mobmanager;

import de.eldoria.bloodnight.specialmobs.SpecialMob;
import org.bukkit.entity.Entity;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;
import java.util.function.Consumer;

class WorldMobs {
    private final Map<UUID, SpecialMob<?>> mobs = new HashMap<>();
    private final Queue<SpecialMob<?>> tickQueue = new ArrayDeque<>();

    private double entityTick;

    public void invokeIfPresent(Entity entity, Consumer<SpecialMob<?>> invoke) {
        invokeIfPresent(entity.getUniqueId(), invoke);
    }

    public void invokeIfPresent(UUID uuid, Consumer<SpecialMob<?>> invoke) {
        SpecialMob<?> specialMob = mobs.get(uuid);
        if (specialMob != null) {
            invoke.accept(specialMob);
        }
    }

    public void invokeAll(Consumer<SpecialMob<?>> invoke) {
        mobs.values().forEach(invoke);
    }

    public void tick(int tickDelay) {
        if (tickQueue.isEmpty()) return;
        entityTick += tickQueue.size() / (double) tickDelay;
        while (entityTick > 0) {
            if (tickQueue.isEmpty()) return;
            SpecialMob<?> poll = tickQueue.poll();
            if (!poll.getBaseEntity().isValid()) {
                remove(poll.getBaseEntity().getUniqueId());
                poll.remove();
            } else {
                poll.tick();
                tickQueue.add(poll);
            }
            entityTick--;
        }
    }

    public boolean isEmpty() {
        return mobs.isEmpty();
    }

    public void put(UUID key, SpecialMob<?> value) {
        mobs.put(key, value);
        tickQueue.add(value);
    }

    /**
     * Attemts to remove an entity from world mobs and the world.
     *
     * @param key uid of entity
     * @return special mob if present.
     */
    public Optional<SpecialMob<?>> remove(UUID key) {
        if (!mobs.containsKey(key)) return Optional.empty();
        SpecialMob<?> removed = mobs.remove(key);
        tickQueue.remove(removed);
        removed.remove();
        return Optional.of(removed);
    }

    public void clear() {
        mobs.clear();
        tickQueue.clear();
    }
}
