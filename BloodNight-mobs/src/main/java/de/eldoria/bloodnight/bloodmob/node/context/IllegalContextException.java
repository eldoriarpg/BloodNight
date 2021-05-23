package de.eldoria.bloodnight.bloodmob.node.context;

import de.eldoria.bloodnight.bloodmob.node.contextcontainer.ContextType;

public class IllegalContextException extends RuntimeException {
    public IllegalContextException(ContextType type, IContext context) {
        super("Class " + context.getClass().getSimpleName() + " can not be assigned to type " + type);
    }
}
