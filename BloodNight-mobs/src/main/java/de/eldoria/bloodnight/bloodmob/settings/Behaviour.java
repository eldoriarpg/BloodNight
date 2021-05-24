package de.eldoria.bloodnight.bloodmob.settings;

import de.eldoria.bloodnight.bloodmob.node.Node;
import de.eldoria.bloodnight.config.ConfigCheck;
import de.eldoria.bloodnight.config.ConfigException;
import de.eldoria.eldoutilities.serialization.SerializationUtil;
import de.eldoria.eldoutilities.serialization.TypeResolvingMap;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Behaviour implements ConfigCheck<MobConfiguration>, ConfigurationSerializable {
    private Map<BehaviourNodeType, List<Node>> behaviourMap = new HashMap<>();

    public Behaviour() {
        Arrays.stream(BehaviourNodeType.values()).forEachOrdered(this::getBehaviour);
    }

    public List<Node> getNodes(BehaviourNodeType node) {
        return getBehaviour(node);
    }

    public int addNode(BehaviourNodeType type, Node node) {
        List<Node> behaviour = getBehaviour(type);
        behaviour.add(node);
        return behaviour.size() -1;
    }

    public Map<BehaviourNodeType, List<Node>> behaviourMap() {
        return Collections.unmodifiableMap(behaviourMap);
    }

    private List<Node> getBehaviour(BehaviourNodeType type) {
        return behaviourMap.computeIfAbsent(type, k -> new ArrayList<>());
    }


    @Override
    public void check(MobConfiguration data) throws ConfigException {
    }

    public Behaviour(Map<String, Object> objectMap) {
        TypeResolvingMap map = SerializationUtil.mapOf(objectMap);
        for (BehaviourNodeType value : BehaviourNodeType.values()) {
            behaviourMap.put(value, map.getValueOrDefault(value.name(), new ArrayList<>()));
        }
    }

    @Override
    @NotNull
    public Map<String, Object> serialize() {
        SerializationUtil.Builder builder = SerializationUtil.newBuilder();
        for (BehaviourNodeType value : BehaviourNodeType.values()) {
            builder.add(value.name(), getBehaviour(value));
        }
        return builder.build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Behaviour behaviour = (Behaviour) o;

        return Objects.equals(behaviourMap, behaviour.behaviourMap);
    }

    @Override
    public int hashCode() {
        return behaviourMap != null ? behaviourMap.hashCode() : 0;
    }
}
