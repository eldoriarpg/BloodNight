package de.eldoria.bloodnight.specialmob.node.context;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class ContextContainer {
    private final Map<Class<? extends IContext>, IContext> contextMap;

    private ContextContainer(Map<Class<? extends IContext>, IContext> contextMap) {
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
     *
     * @param target target context
     * @param result result context
     * @param map    map target to result
     * @param <T>    type of target
     * @param <R>    type of result
     */
    public <T extends IContext, R extends IContext> void transform(ContextType<T> target, ContextType<R> result, Function<T, R> map) {
        get(target).ifPresent(t -> {
            add(result, map.apply(t));
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
    public <T extends IContext> void add(ContextType<T> type, T context) {
        if (context.getClass().isInstance(type.contextClazz())) {
            contextMap.put(type.contextClazz(), context);
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

    /**
     * Get a builder.
     *
     * @return new builder instance;
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Map<Class<? extends IContext>, IContext> contextMap = new HashMap<>();

        /**
         * Add a context to the context Container.
         *
         * @param type    type of context type
         * @param context context matching the {@link ContextType#contextClazz()}
         * @return builder instance;
         * @throws IllegalContextException if the {@link ContextType#contextClazz()} does not match the {@link IContext}
         */
        public <T extends IContext> Builder add(ContextType<?> type, T context) {
            if (context.getClass().isInstance(type.contextClazz())) {
                contextMap.put(type.contextClazz(), context);
                return this;
            }
            throw new IllegalContextException(type, context);
        }

        public ContextContainer build() {
            return new ContextContainer(contextMap);
        }
    }
}
