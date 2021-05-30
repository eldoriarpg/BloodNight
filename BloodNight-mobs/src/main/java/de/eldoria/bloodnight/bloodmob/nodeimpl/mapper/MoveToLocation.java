package de.eldoria.bloodnight.bloodmob.nodeimpl.mapper;

import de.eldoria.bloodnight.bloodmob.node.Node;
import de.eldoria.bloodnight.bloodmob.node.contextcontainer.ContextContainer;
import de.eldoria.bloodnight.bloodmob.node.contextcontainer.ContextType;
import de.eldoria.bloodnight.bloodmob.node.mapper.MapperNode;
import de.eldoria.bloodnight.bloodmob.serialization.annotation.Property;
import de.eldoria.eldoutilities.serialization.SerializationUtil;
import de.eldoria.eldoutilities.serialization.TypeResolvingMap;
import de.eldoria.eldoutilities.utils.EnumUtil;
import lombok.NoArgsConstructor;

import java.util.Map;

@NoArgsConstructor
public class MoveToLocation extends MapperNode {
    @Property(name = "", descr = "")
    private LocationSource source = LocationSource.OLD;

    public MoveToLocation(Node node, LocationSource source) {
        super(node);
        this.source = source;
    }

    public MoveToLocation(Map<String, Object> objectMap) {
        super(objectMap);
        TypeResolvingMap map = SerializationUtil.mapOf(objectMap);
        source = map.getValueOrDefault("source", source, s -> EnumUtil.parse(s, LocationSource.class, source));
    }

    @Override
    public void map(ContextContainer context) {
        context.transform(ContextType.MOVE, ContextType.LOCATION, move -> {
            switch (source) {
                case OLD:
                    return move;
                case NEW:
                    return move::getNewLocation;
                default:
                    throw new IllegalStateException("Unexpected value: " + source);
            }
            //TODO
        }, "");
    }

    @Override
    public ContextContainer getTransformedOutput(ContextContainer context) {
        map(context);
        return nextNode() != null ? nextNode().getTransformedOutput(context) : context;
    }

    public enum LocationSource {
        OLD, NEW
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MoveToLocation that = (MoveToLocation) o;

        return source == that.source;
    }

    @Override
    public int hashCode() {
        return source != null ? source.hashCode() : 0;
    }
}
