package de.eldoria.bloodnight.specialmob.node.mapper;

import de.eldoria.bloodnight.specialmob.node.Node;
import de.eldoria.bloodnight.specialmob.node.context.ContextContainer;
import de.eldoria.bloodnight.specialmob.node.context.ContextType;
import de.eldoria.eldoutilities.serialization.SerializationUtil;
import de.eldoria.eldoutilities.serialization.TypeResolvingMap;
import de.eldoria.eldoutilities.utils.EnumUtil;

import java.util.Map;

public class MoveToLocation extends MapperNode {
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
        });
    }

    public static enum LocationSource {
        OLD, NEW
    }
}
