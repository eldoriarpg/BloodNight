package de.eldoria.bloodnight.specialmob.node.mapper;

import de.eldoria.bloodnight.specialmob.node.Node;
import de.eldoria.bloodnight.specialmob.node.context.IActionContext;
import de.eldoria.bloodnight.specialmob.node.context.ILocationContext;
import de.eldoria.bloodnight.specialmob.node.context.IMoveContext;
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
    public IActionContext map(IActionContext context) {
        if (context instanceof IMoveContext) {
            switch (source) {
                case OLD:
                    return context;
                case NEW:
                    return (ILocationContext) ((IMoveContext) context)::getNewLocation;
            }
        }
        return context;
    }

    public static enum LocationSource {
        OLD, NEW
    }
}
