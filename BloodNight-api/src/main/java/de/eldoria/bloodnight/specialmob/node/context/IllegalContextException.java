package de.eldoria.bloodnight.specialmob.node.context;

public class IllegalContextException extends RuntimeException {
    public IllegalContextException(ContextType type, IContext context) {
        super("Class " + context.getClass().getSimpleName() + " can not be assigned to type " + type);
    }
}
