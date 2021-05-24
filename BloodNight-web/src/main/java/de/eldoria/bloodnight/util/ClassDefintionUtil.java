package de.eldoria.bloodnight.util;

import de.eldoria.bloodnight.bloodmob.settings.Behaviour;
import de.eldoria.bloodnight.serialization.ClassDefinition;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ClassDefintionUtil {
    public static List<ClassDefinition> getBehaviourDefinitions(Behaviour behaviour) {
        return behaviour.behaviourMap()
                .values()
                .stream()
                .map(nodeList -> nodeList.stream()
                        .flatMap(node -> node.getClasses(new HashSet<>()).stream())
                        .collect(Collectors.toSet()))
                .flatMap(Set::stream)
                .collect(Collectors.toSet())
                .stream()
                .map(ClassDefinition::of)
                .collect(Collectors.toList());
    }
}
