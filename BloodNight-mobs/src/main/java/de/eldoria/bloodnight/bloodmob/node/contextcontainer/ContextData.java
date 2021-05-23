package de.eldoria.bloodnight.bloodmob.node.contextcontainer;

import de.eldoria.bloodnight.bloodmob.node.context.IContext;

public class ContextData {
    IContext context;
    String descr;

    public static ContextData of(IContext context, String descr) {
        return new ContextData(context, descr);
    }

    private ContextData(IContext context, String descr) {
        this.context = context;
        this.descr = descr;
    }

    public IContext context() {
        return context;
    }

    public String descr() {
        return descr;
    }
}
