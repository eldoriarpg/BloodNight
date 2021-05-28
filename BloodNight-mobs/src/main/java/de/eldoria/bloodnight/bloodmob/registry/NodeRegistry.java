package de.eldoria.bloodnight.bloodmob.registry;

import de.eldoria.bloodnight.bloodmob.node.Node;
import de.eldoria.bloodnight.bloodmob.node.NodeHolder;
import de.eldoria.bloodnight.bloodmob.node.annotations.RequiresContext;
import de.eldoria.bloodnight.bloodmob.node.context.IContext;
import de.eldoria.bloodnight.bloodmob.node.contextcontainer.ContextContainer;
import de.eldoria.bloodnight.bloodmob.node.contextcontainer.ContextContainerFactory;
import de.eldoria.bloodnight.bloodmob.node.effect.EffectNode;
import de.eldoria.bloodnight.bloodmob.node.filter.FilterNode;
import de.eldoria.bloodnight.bloodmob.node.mapper.MapperNode;
import de.eldoria.bloodnight.bloodmob.node.predicate.PredicateNode;
import de.eldoria.bloodnight.bloodmob.settings.BehaviourNodeType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class NodeRegistry {
    private static final Map<Class<?>, List<Class<?>>> NODES = new HashMap<>();

    public static void registerNode(Class<?> clazz) {
        if (!isEndNode(clazz)) addIfInstance(Node.class, clazz);
        addIfInstance(NodeHolder.class, clazz);
        addIfInstance(EffectNode.class, clazz);
        addIfInstance(FilterNode.class, clazz);
        addIfInstance(MapperNode.class, clazz);
        addIfInstance(PredicateNode.class, clazz);
    }

    public static Map<Class<?>, List<Class<?>>> nodes() {
        return Collections.unmodifiableMap(NODES);
    }

    public static boolean isEndNode(Class<?> clazz) {
        return !clazz.isInstance(NodeHolder.class);
    }

    public static Set<Class<?>> nodeSet() {
        return NODES.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
    }

    public static Set<Class<?>> getAvailableNodes(Node node, ContextContainer container) {
        Node lastNode = node.getLast();
        if (isEndNode(lastNode.getClass())) {
            return Collections.emptySet();
        }

        return getMatchingClasses(node.getTransformedOutput(container));
    }

    public static Set<Class<?>> getAvailableNodes(BehaviourNodeType type) {
        ContextContainer context = ContextContainerFactory.mock(type);

        return getMatchingClasses(context);
    }

    @NotNull
    private static Set<Class<?>> getMatchingClasses(ContextContainer context) {
        ArrayList<Class<?>> classes = new ArrayList<>(nodes().get(Node.class));
        classes.addAll(nodes().get(NodeHolder.class));


        return classes.stream().filter(clazz -> {
            if (!clazz.isAnnotationPresent(RequiresContext.class)) {
                return true;
            }
            RequiresContext annotation = clazz.getAnnotation(RequiresContext.class);
            for (Class<? extends IContext> aClazz : annotation.value()) {
                if (!context.contains(aClazz)) {
                    return false;
                }
            }
            return true;
        }).collect(Collectors.toSet());
    }

    private static void addIfInstance(Class<?> ref, Class<?> clazz) {
        if (clazz.isInstance(ref)) getClassList(ref).add(clazz);
    }

    private static List<Class<?>> getClassList(Class<?> clazz) {
        return NODES.computeIfAbsent(clazz, k -> new ArrayList<>());
    }
}
