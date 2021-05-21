package de.eldoria.bloodnight.specialmob.node.context;

import org.bukkit.event.Cancellable;

public interface ICancelableContext extends IContext {
    Cancellable getCancelable();
}
