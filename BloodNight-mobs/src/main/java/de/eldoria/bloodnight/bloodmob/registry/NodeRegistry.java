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
import de.eldoria.bloodnight.bloodmob.nodeimpl.action.ApplyDamage;
import de.eldoria.bloodnight.bloodmob.nodeimpl.action.CancelEvent;
import de.eldoria.bloodnight.bloodmob.nodeimpl.action.LaunchProjectileOnTarget;
import de.eldoria.bloodnight.bloodmob.nodeimpl.action.OtherPotion;
import de.eldoria.bloodnight.bloodmob.nodeimpl.action.PotionCloudAction;
import de.eldoria.bloodnight.bloodmob.nodeimpl.action.RemoveSelfPotion;
import de.eldoria.bloodnight.bloodmob.nodeimpl.action.SearchNearPlayerTarget;
import de.eldoria.bloodnight.bloodmob.nodeimpl.action.SelfPotion;
import de.eldoria.bloodnight.bloodmob.nodeimpl.action.SetEquipment;
import de.eldoria.bloodnight.bloodmob.nodeimpl.action.TeleportToTarget;
import de.eldoria.bloodnight.bloodmob.nodeimpl.effect.DustParticleEffect;
import de.eldoria.bloodnight.bloodmob.nodeimpl.effect.ParticleEffect;
import de.eldoria.bloodnight.bloodmob.nodeimpl.filter.CooldownFilter;
import de.eldoria.bloodnight.bloodmob.nodeimpl.filter.LastDamage;
import de.eldoria.bloodnight.bloodmob.nodeimpl.filter.PredicateFilter;
import de.eldoria.bloodnight.bloodmob.nodeimpl.mapper.MoveToLocation;
import de.eldoria.bloodnight.bloodmob.nodeimpl.predicate.HasTarget;
import de.eldoria.bloodnight.bloodmob.nodeimpl.predicate.IsDamageCause;
import de.eldoria.bloodnight.bloodmob.nodeimpl.predicate.IsEntityType;
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

    static {
        registerNode(ApplyDamage.class);
        registerNode(CancelEvent.class);
        registerNode(LaunchProjectileOnTarget.class);
        registerNode(OtherPotion.class);
        registerNode(PotionCloudAction.class);
        registerNode(RemoveSelfPotion.class);
        registerNode(SearchNearPlayerTarget.class);
        registerNode(SelfPotion.class);
        registerNode(SetEquipment.class);
        registerNode(TeleportToTarget.class);
        registerNode(DustParticleEffect.class);
        registerNode(ParticleEffect.class);
        registerNode(ParticleEffect.class);
        registerNode(CooldownFilter.class);
        registerNode(LastDamage.class);
        registerNode(PredicateFilter.class);
        registerNode(MoveToLocation.class);
        registerNode(HasTarget.class);
        registerNode(IsDamageCause.class);
        registerNode(IsEntityType.class);
    }

    public static void registerNode(Class<?> clazz) {
        if (isEndNode(clazz)) addIfInstance(Node.class, clazz);
        addIfInstance(NodeHolder.class, clazz);
        addIfInstance(EffectNode.class, clazz);
        addIfInstance(FilterNode.class, clazz);
        addIfInstance(MapperNode.class, clazz);
        addIfInstance(PredicateNode.class, clazz);
    }

    public static boolean isEndNode(Class<?> clazz) {
        return !NodeHolder.class.isAssignableFrom(clazz);
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

    public static List<Class<?>> nodes() {
        ArrayList<Class<?>> classes = new ArrayList<>(NODES.get(Node.class));
        classes.addAll(NODES.get(NodeHolder.class));
        return classes;
    }

    public static List<Class<?>> predicates() {
        return NODES.get(PredicateNode.class);
    }

    @NotNull
    private static Set<Class<?>> getMatchingClasses(ContextContainer context) {
        ArrayList<Class<?>> classes = new ArrayList<>(NODES.get(Node.class));
        classes.addAll(NODES.get(NodeHolder.class));


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
        if (ref.isAssignableFrom(clazz)) {
            getClassList(ref).add(clazz);
        }
    }

    private static List<Class<?>> getClassList(Class<?> clazz) {
        return NODES.computeIfAbsent(clazz, k -> new ArrayList<>());
    }
}
