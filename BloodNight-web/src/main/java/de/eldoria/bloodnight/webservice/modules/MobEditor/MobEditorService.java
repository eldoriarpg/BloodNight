package de.eldoria.bloodnight.webservice.modules.MobEditor;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.net.MediaType;
import de.eldoria.bloodnight.bloodmob.node.Node;
import de.eldoria.bloodnight.bloodmob.node.contextcontainer.ContextContainerFactory;
import de.eldoria.bloodnight.bloodmob.registry.NodeRegistry;
import de.eldoria.bloodnight.bloodmob.serialization.container.MobEditorPayload;
import de.eldoria.bloodnight.bloodmob.serialization.mapper.MobMapper;
import de.eldoria.bloodnight.bloodmob.serialization.value.ValueType;
import de.eldoria.bloodnight.bloodmob.settings.BehaviourNodeType;
import de.eldoria.bloodnight.bloodmob.settings.Drops;
import de.eldoria.bloodnight.bloodmob.settings.Equipment;
import de.eldoria.bloodnight.bloodmob.settings.Extension;
import de.eldoria.bloodnight.bloodmob.settings.MobConfiguration;
import de.eldoria.bloodnight.bloodmob.settings.mobsettings.BloodMobType;
import de.eldoria.bloodnight.bloodmob.settings.mobsettings.TypeSetting;
import de.eldoria.bloodnight.serialization.ClassDefinition;
import de.eldoria.bloodnight.serialization.DataDescriptionContainer;
import de.eldoria.bloodnight.util.ClassDefintionUtil;
import de.eldoria.eldoutilities.container.Triple;
import de.eldoria.eldoutilities.utils.EnumUtil;
import org.eclipse.jetty.http.HttpStatus;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import spark.Request;
import spark.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;
import static spark.Spark.halt;

public class MobEditorService extends SessionHolder<MobEditorPayload> {

    private static final Logger log = getLogger(MobEditorService.class);

    public Object submit(Request request, Response response) {
        MobEditorPayload mobEditorPayload;
        try {
            mobEditorPayload = MobMapper.mapper().readValue(request.body(), MobEditorPayload.class);
        } catch (JacksonException e) {
            response.status();
            halt(HttpStatus.BAD_REQUEST_400, "Invalid payload");
            return null;
        }
        var session = openSession(mobEditorPayload);
        response.body(session.accessToken());
        response.status(HttpStatus.CREATED_201);
        return response.body();
    }

    public Object close(Request request, Response response) {
        var session = getSession(request, response);
        session = closeSession(session);
        response.status(HttpStatus.OK_200);
        response.type(MediaType.PLAIN_TEXT_UTF_8.type());
        response.body(session.retrievaltoken());
        return response.body();
    }

    public Object retrieve(Request request, Response response) {
        var closedSession = getClosedSession(request, response);
        try {
            response.body(MobMapper.mapper().writeValueAsString(closedSession.sessionData()));
            response.status(HttpStatus.OK_200);
            response.type(MediaType.JSON_UTF_8.type());
        } catch (JsonProcessingException e) {
            log.error("Could not serialize session data", e);
            halt(HttpStatus.INTERNAL_SERVER_ERROR_500);
            return null;
        }
        return response.body();
    }

    public Object getTypes(Request request, Response response) {
        Map<String, ClassDefinition> classDefinition = new HashMap<>();
        for (var value : ValueType.values()) {
            classDefinition.put(value.name(), value.clazz() == null ? null : ClassDefinition.of(value.clazz()));
        }
        response.status(HttpStatus.OK_200);
        response.type(MediaType.JSON_UTF_8.type());
        try {
            response.body(MobMapper.mapper().writeValueAsString(classDefinition));
        } catch (JsonProcessingException e) {
            log.error("could not serialize types.", e);
            halt(HttpStatus.INTERNAL_SERVER_ERROR_500);
            return null;
        }
        return response.body();
    }

    public Object getType(Request request, Response response) {
        var parse = EnumUtil.parse(request.params(":type"), ValueType.class);
        if (parse == null) {
            halt(HttpStatus.BAD_REQUEST_400, "Invalid type");
        }

        if (parse.clazz() != null) {
            response.status(HttpStatus.OK_200);
            try {
                response.body(MobMapper.mapper().writeValueAsString(ClassDefinition.of(parse.clazz())));
            } catch (JsonProcessingException e) {
                log.error("could not serialize types.", e);
                halt(HttpStatus.INTERNAL_SERVER_ERROR_500);
                return null;
            }
            response.type(MediaType.JSON_UTF_8.type());
        } else {
            response.status(HttpStatus.NO_CONTENT_204);
        }
        return response.body();
    }

    public Object mobList(Request request, Response response) {
        var session = getSession(request, response);
        var identifier = session.readData(data -> data.settingsContainer().mobIdentifier());

        try {
            return MobMapper.mapper().writeValueAsString(identifier);
        } catch (JsonProcessingException e) {
            log.error("Could not write mob identifier", e);
            halt(HttpStatus.SERVICE_UNAVAILABLE_503);
        }
        return null;
    }

    public Object getMobSettings(Request request, Response response) {
        var configuration = getSessionMobSettings(request, response);
        try {
            response.body(MobMapper.mapper().writeValueAsString(DataDescriptionContainer.of(configuration)));
        } catch (JsonProcessingException e) {
            log.error("Could not serialize mob settings", e);
            halt(HttpStatus.INTERNAL_SERVER_ERROR_500);
        }
        response.status(HttpStatus.OK_200);
        return response.body();
    }

    public Object createMobSettings(Request request, Response response) {
        var session = getSession(request, response);
        var identifier = request.params(":identifier");
        var mobEditorPayload = session.sessionData();
        if (mobEditorPayload.settingsContainer().mobExists(identifier)) {
            halt(HttpStatus.CONFLICT_409, "Identifier already in use.");
        }

        mobEditorPayload.settingsContainer().createMob(identifier);
        response.status(HttpStatus.CREATED_201);
        return response.body();
    }

    public Object deleteMobSettings(Request request, Response response) {
        var session = getSession(request, response);
        var identifier = request.params(":identifier");
        var mobEditorPayload = session.sessionData();

        var removed = mobEditorPayload.settingsContainer().removeMob(identifier);
        if (!removed) {
            halt(HttpStatus.NOT_MODIFIED_304, "Identifier not found.");
        }
        response.status(HttpStatus.ACCEPTED_202);
        return response.body();
    }

    public Object getaAvailableTypes(Request request, Response response) {
        Map<BloodMobType, ClassDefinition> definitions = new HashMap<>();

        var mobSettings = getSessionMobSettings(request, response);

        for (var value : BloodMobType.values()) {
            if (mobSettings.wrapTypes().containsKey(value)) continue;
            definitions.put(value, ClassDefinition.of(value.typeSettingClazz()));
        }
        try {
            response.body(MobMapper.mapper().writeValueAsString(definitions));
            response.type(MediaType.JSON_UTF_8.type());
            response.status(HttpStatus.OK_200);
        } catch (JsonProcessingException e) {
            log.error("Could not write class definitions", e);
            halt(HttpStatus.INTERNAL_SERVER_ERROR_500);
        }
        return response.body();
    }

    public Object getSetWrapTypes(Request request, Response response) {
        Map<BloodMobType, DataDescriptionContainer<?>> definitions = new HashMap<>();

        var mobSettings = getSessionMobSettings(request, response);

        for (var entry : mobSettings.wrapTypes().entrySet()) {
            var container = DataDescriptionContainer.of(entry.getValue());
            definitions.put(entry.getKey(), container);
        }

        try {
            response.body(MobMapper.mapper().writeValueAsString(definitions));
            response.type(MediaType.JSON_UTF_8.type());
            response.status(HttpStatus.OK_200);
        } catch (JsonProcessingException e) {
            log.error("Could not write class definitions", e);
            halt(HttpStatus.INTERNAL_SERVER_ERROR_500);
        }
        return response.body();
    }

    public Object removeWrapType(Request request, Response response) {
        var mobSettings = getSessionMobSettings(request, response);

        var params = request.params(":type");

        var type = EnumUtil.parse(params, BloodMobType.class);
        if (type == null) {
            halt(HttpStatus.BAD_REQUEST_400, "Unknown BloodMobType");
        }

        var remove = mobSettings.wrapTypes().remove(type) != null;
        response.status(remove ? HttpStatus.ACCEPTED_202 : HttpStatus.NOT_MODIFIED_304);
        return response.body();
    }

    public Object addWrapType(Request request, Response response) {
        var mobSettings = getSessionMobSettings(request, response);
        var params = request.params(":type");
        var type = EnumUtil.parse(params, BloodMobType.class);
        if (type == null) {
            halt(HttpStatus.BAD_REQUEST_400, "Unknown BloodMobType");
        }

        TypeSetting typeSetting;
        try {
            typeSetting = MobMapper.mapper().readValue(response.body(), type.typeSettingClazz());
        } catch (JsonProcessingException e) {
            halt(HttpStatus.BAD_REQUEST_400);
            return null;
        }

        mobSettings.wrapTypes().put(type, typeSetting);

        response.status(HttpStatus.OK_200);
        return response.body();
    }

    public Object getSetting(Request request, Response response) {
        var mobSettings = getSessionMobSettings(request, response);
        var params = request.params(":setting");

        DataDescriptionContainer<?> container;

        switch (params.toLowerCase(Locale.ROOT)) {
            case "extension" -> container = DataDescriptionContainer.of(mobSettings.extension(), Extension.class);
            case "extension.equipment" -> container = DataDescriptionContainer.of(mobSettings.extension(), Equipment.class);
            case "equipment" -> container = DataDescriptionContainer.of(mobSettings.equipment(), Equipment.class);
            case "drops" -> container = DataDescriptionContainer.of(mobSettings.drops(), Drops.class);
            case "behaviour" -> {
                var configuration = getSessionMobSettings(request, response);
                var nodeMap = configuration.behaviour().behaviourMap();
                var definitions = ClassDefintionUtil.getBehaviourDefinitions(configuration.behaviour());
                container = DataDescriptionContainer.of(nodeMap, definitions);
            }
            default -> {
                halt(HttpStatus.BAD_REQUEST_400);
                return null;
            }
        }

        try {
            response.body(MobMapper.mapper().writeValueAsString(container));
        } catch (JsonProcessingException e) {
            log.error("Could not serialize mob behaviour.", e);
            halt(HttpStatus.INTERNAL_SERVER_ERROR_500);
            return null;
        }
        response.status(HttpStatus.OK_200);
        return response.status();
    }

    public Object setSetting(Request request, Response response) {
        var mobSettings = getSessionMobSettings(request, response);
        var params = request.params(":setting");

        var body = request.body();

        try {

            switch (params.toLowerCase(Locale.ROOT)) {
                case "extension" -> mobSettings.extension(MobMapper.mapper().readValue(body, Extension.class));
                case "extension.equipment" -> mobSettings.extension().equipment(MobMapper.mapper().readValue(body, Equipment.class));
                case "equipment" -> mobSettings.equipment(MobMapper.mapper().readValue(body, Equipment.class));
                case "drops" -> mobSettings.drops(MobMapper.mapper().readValue(body, Drops.class));
                default -> {
                    halt(HttpStatus.BAD_REQUEST_400);
                    return null;
                }
            }
        } catch (JacksonException e) {
            log.error("Could not deserialize setting.", e);
            halt(HttpStatus.BAD_REQUEST_400);
        }

        return response.status();
    }

    public Object addNode(Request request, Response response) {
        var nodes = getNodes(request, response);

        Node node;
        try {
            node = MobMapper.mapper().readValue(request.body(), Node.class);
        } catch (JsonProcessingException e) {
            halt(HttpStatus.BAD_REQUEST_400, "Invalid Node Format.");
            return null;
        }

        nodes.second.get(nodes.first).addNode(node);
        response.status(HttpStatus.OK_200);
        response.type(MediaType.PLAIN_TEXT_UTF_8.type());
        return response.status();
    }

    public Object createNode(Request request, Response response) {
        var mobSettings = getSessionMobSettings(request, response);
        var type = request.params(":type");
        var nodeType = EnumUtil.parse(type, BehaviourNodeType.class);
        if (nodeType == null) {
            halt(HttpStatus.BAD_REQUEST_400, "Unknown node type");
            return null;
        }

        Node node;
        try {
            node = MobMapper.mapper().readValue(response.body(), Node.class);
        } catch (JsonProcessingException e) {
            halt(HttpStatus.BAD_REQUEST_400, "Invalid Node Format.");
            return null;
        }
        response.body(String.valueOf(mobSettings.behaviour().addNode(nodeType, node)));
        response.status(HttpStatus.CREATED_201);
        response.type(MediaType.PLAIN_TEXT_UTF_8.type());
        return response.body();
    }

    public Object removeLastNode(Request request, Response response) {
        var nodes = getNodes(request, response);
        var node = nodes.second.get(nodes.first);

        if (node.isLast()) {
            halt(HttpStatus.BAD_REQUEST_400, "The last node cant be removed. Use delete instead.");
        }

        node.removeLast();

        response.status(HttpStatus.OK_200);
        response.type(MediaType.JSON_UTF_8.type());
        return response.body();
    }

    public Object deleteNode(Request request, Response response) {
        var nodes = getNodes(request, response);

        nodes.second.remove(nodes.first);

        response.status(HttpStatus.OK_200);
        response.type(MediaType.PLAIN_TEXT_UTF_8.type());
        return response.body();
    }

    public Object nextNodes(Request request, Response response) {
        var nodes = getNodes(request, response);
        var node = nodes.second.get(nodes.first);
        var availableNodes = NodeRegistry.getAvailableNodes(node, ContextContainerFactory.mock(nodes.third));
        var definitions = availableNodes.stream().map(ClassDefinition::of).collect(Collectors.toList());
        try {
            response.body(MobMapper.mapper().writeValueAsString(definitions));
        } catch (JsonProcessingException e) {
            log.error("Could not build node definitions.", e);
            halt(HttpStatus.INTERNAL_SERVER_ERROR_500);
        }
        response.type(MediaType.JSON_UTF_8.type());
        response.status(HttpStatus.OK_200);
        return response.body();
    }

    public Object getItems(Request request, Response response) {
        var session = getSession(request, response);
        var simpleItems = session.readData(data -> data.settingsContainer().items());
        try {
            response.body(MobMapper.mapper().writeValueAsString(simpleItems));
        } catch (JsonProcessingException e) {
            halt(HttpStatus.INTERNAL_SERVER_ERROR_500);
        }
        response.status(HttpStatus.OK_200);
        return response.body();
    }

    public Object deleteItem(Request request, Response response) {
        var session = getSession(request, response);
        var params = request.params(":id");
        int id;
        try {
            id = Integer.parseInt(params);
        } catch (NumberFormatException e) {
            halt(HttpStatus.BAD_REQUEST_400);
            return null;
        }
        var removed = session.readData(mobEditorPayload -> mobEditorPayload.settingsContainer().items()
                .removeIf(item -> item.id() == id));
        response.status(removed ? HttpStatus.OK_200 : HttpStatus.NOT_MODIFIED_304);
        return response.status();
    }

    private MobConfiguration getSessionMobSettings(Request request, Response response) {
        var session = getSession(request, response);
        var identifier = request.params(":identifier");
        var mobConfiguration = session.readData(data -> data.settingsContainer().getMobConfig(identifier));
        if (mobConfiguration.isEmpty()) halt(HttpStatus.BAD_REQUEST_400, "Mob does not exist.");
        return mobConfiguration.get();
    }

    @NotNull
    private Triple<Integer, List<Node>, BehaviourNodeType> getNodes(Request request, Response response) {
        var mobSettings = getSessionMobSettings(request, response);
        var type = request.params(":type");
        var idParam = request.params(":id");

        var id = 0;
        try {
            id = Integer.parseInt(idParam);
        } catch (NumberFormatException e) {
            halt(HttpStatus.BAD_REQUEST_400, "Invalid id");
        }

        var nodeType = EnumUtil.parse(type, BehaviourNodeType.class);
        if (nodeType == null) {
            halt(HttpStatus.BAD_REQUEST_400, "Unknown node type");
        }
        var nodes = mobSettings.behaviour().getNodes(nodeType);
        if (id >= nodes.size() || id < 0) {
            halt(HttpStatus.BAD_REQUEST_400, "Invalid id");
        }
        return Triple.of(id, nodes, nodeType);
    }

    public Object nextTypeNodes(Request request, Response response) {
        var type = request.params(":type");
        var nodeType = EnumUtil.parse(type, BehaviourNodeType.class);
        if (nodeType == null) {
            halt(HttpStatus.BAD_REQUEST_400, "Unknown node type");
        }

        var availableNodes = NodeRegistry.getAvailableNodes(nodeType);

        var definitions = availableNodes.stream().map(ClassDefinition::of).collect(Collectors.toList());
        try {
            response.body(MobMapper.mapper().writeValueAsString(definitions));
        } catch (JsonProcessingException e) {
            log.error("Could not build node definitions.", e);
            halt(HttpStatus.INTERNAL_SERVER_ERROR_500);
        }
        response.type(MediaType.JSON_UTF_8.type());
        response.status(HttpStatus.OK_200);
        return response.body();
    }
}
