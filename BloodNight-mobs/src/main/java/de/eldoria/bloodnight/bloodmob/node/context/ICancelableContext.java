package de.eldoria.bloodnight.bloodmob.node.context;

import org.bukkit.event.Cancellable;

public interface ICancelableContext extends IContext {
    static ICancelableContext of(Cancellable event) {
        return () -> event;
    }

    Cancellable getCancelable();
}
