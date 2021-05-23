package de.eldoria.bloodnight.bloodmob.node;

import java.util.Set;

public interface ClassesProvider {
    default Set<Class<?>> getClasses(Set<Class<?>> set){
        set.add(getClass());
        return set;
    }
}
