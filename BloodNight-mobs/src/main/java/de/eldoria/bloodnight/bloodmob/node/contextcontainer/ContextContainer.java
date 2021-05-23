package de.eldoria.bloodnight.bloodmob.node.contextcontainer;

import de.eldoria.bloodnight.bloodmob.node.context.IContext;
import de.eldoria.bloodnight.bloodmob.node.context.IllegalContextException;
import de.eldoria.bloodnight.bloodmob.registry.items.ItemRegistry;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ContextContainer {
    private ItemRegistry itemRegistry;
    private final Map<Class<? extends IContext>, ContextData> contextMap;

    private ContextContainer(Map<Class<? extends IContext>, ContextData> contextMap) {
        this.contextMap = contextMap;
    }

    /**
     * Get the context of the requested type.
     *
     * @param type context type
     * @param <T>  type of context
     * @return optional holding the context is present
     */
    @SuppressWarnings("unchecked")
    public <T extends IContext> Optional<T> get(ContextType<T> type) {
        return Optional.ofNullable((T) contextMap.get(type.contextClazz()));
    }

    /**
     * Checks if the context has this type of context
     *
     * @param type type of context
     * @return true if context is present
     */
    public boolean has(ContextType<?> type) {
        return contextMap.containsKey(type.contextClazz());
    }

    /**
     * Transform the target context to another context.
     * <p>
     * The target context will be removed from the contexts and the result context will be added.
     * <p>
     * The method will have no effect if {@link #has(ContextType)} returns false for target.
     *  @param <T>    type of target
     * @param <R>    type of result
     * @param target target context
     * @param result result context
     * @param map    map target to result
     * @param descr
     */
    public <T extends IContext, R extends IContext> void transform(ContextType<T> target, ContextType<R> result, Function<T, R> map, String descr) {
        get(target).ifPresent(t -> {
            add(result, map.apply(t), descr);
            remove(target);
        });
    }

    /**
     * Add a context to the context Container.
     *
     * @param type    type of context type
     * @param context context matching the {@link ContextType#contextClazz()}
     * @throws IllegalContextException if the {@link ContextType#contextClazz()} does not match the {@link IContext}
     */
    public <T extends IContext> void add(ContextType<T> type, T context, String descr) {
        if (context.getClass().isInstance(type.contextClazz())) {
            contextMap.put(type.contextClazz(), ContextData.of(context, descr));
            return;
        }
        throw new IllegalContextException(type, context);
    }

    /**
     * Remove a context from the container
     *
     * @param type context type
     */
    public void remove(ContextType<?> type) {
        contextMap.remove(type.contextClazz());
    }

    public Collection<ContextType<?>> getTypes() {
        return contextMap.keySet().stream().map(ContextType::getType).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toSet());
    }

    /**
     * Get a builder.
     *
     * @return new builder instance;
     */
    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(Map<Class<? extends IContext>, ContextData> contextMap) {
        return new Builder(contextMap);
    }

    public static Builder builder(ContextContainer context) {
        return builder(new HashMap<>(context.contextMap));
    }

    public static <T extends IContext> ContextContainer of(ContextType<?> type, T context, String descr) {
        return builder().add(type, context, descr).build();
    }

    public static <T extends IContext> Builder builder(ContextType<?> type, T context, String descr) {
        return builder().add(type, context, descr);
    }

    public ItemRegistry itemRegistry() {
        return itemRegistry;
    }

    public static class Builder {
        private final Map<Class<? extends IContext>, ContextData> contextMap;

        public Builder(Map<Class<? extends IContext>, ContextData> contextMap) {
            this.contextMap = contextMap;
        }

        public Builder() {
            contextMap = new HashMap<>();
        }

        /**
         * Add a context to the context Container.
         *
         * @param type    type of context type
         * @param context context matching the {@link ContextType#contextClazz()}
         * @return builder instance;
         * @throws IllegalContextException if the {@link ContextType#contextClazz()} does not match the {@link IContext}
         */
        public <T extends IContext> Builder add(ContextType<?> type, T context, String descr) {
            if (context.getClass().isInstance(type.contextClazz())) {
                contextMap.put(type.contextClazz(), ContextData.of(context, descr));
                return this;
            }
            throw new IllegalContextException(type, context);
        }

        public ContextContainer build() {
            return new ContextContainer(contextMap);
        }
    }

    /**
     * Get a new instance of this object with the same values
     *
     * @return new instance with same values
     */
    public ContextContainer copy() {
        return new ContextContainer(new HashMap<>(contextMap));
    }

}
