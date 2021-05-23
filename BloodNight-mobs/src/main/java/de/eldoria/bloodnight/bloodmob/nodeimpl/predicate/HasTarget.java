package de.eldoria.bloodnight.bloodmob.nodeimpl.predicate;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.eldoria.bloodnight.bloodmob.IBloodMob;
import de.eldoria.bloodnight.bloodmob.node.contextcontainer.ContextContainer;
import de.eldoria.bloodnight.bloodmob.node.predicate.PredicateNode;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@JsonSerialize
@NoArgsConstructor
public class HasTarget implements PredicateNode {

    @Override
    public boolean test(IBloodMob mob, ContextContainer context) {
        return mob.getBase().getTarget() != null;
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        return null;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) return false;
        return obj.getClass() == getClass();
    }
}
