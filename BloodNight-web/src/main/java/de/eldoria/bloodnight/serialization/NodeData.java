package de.eldoria.bloodnight.serialization;

import de.eldoria.bloodnight.bloodmob.node.Node;
import de.eldoria.bloodnight.bloodmob.serialization.ClassDefinition;
import de.eldoria.bloodnight.bloodmob.serialization.value.SimpleValue;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NodeData {
    Map<String, List<SimpleValue>> definitions;
    Node startNode;

    public NodeData(Node startNode, Map<String, List<SimpleValue>> definitions) {
        this.startNode = startNode;
        this.definitions = definitions;
    }

    public NodeData(){

    }


    public static NodeData of(Node node) {
        Map<String, List<SimpleValue>> definitions = node.getClasses(new HashSet<>()).stream()
                .map(ClassDefinition::of)
                .collect(Collectors.toMap(ClassDefinition::clazz, ClassDefinition::values));
        return new NodeData(node, definitions);
    }

    public Map<String, List<SimpleValue>> definitions() {
        return definitions;
    }

    public Node startNode() {
        return startNode;
    }
}
