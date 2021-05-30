package de.eldoria.bloodnight.webservice.modules.mobeditor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.net.MediaType;
import de.eldoria.bloodnight.bloodmob.drop.Drop;
import de.eldoria.bloodnight.bloodmob.node.Node;
import de.eldoria.bloodnight.bloodmob.node.NodeHolder;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
        var mobEditorPayload = readInput(request, MobEditorPayload.class);
        var session = openSession(mobEditorPayload);
        return respondStatus(response, HttpStatus.CREATED_201, session.accessToken());
    }

    public Object close(Request request, Response response) {
        var session = getSession(request, response);
        session = closeSession(session);
        return respondStatus(response, HttpStatus.OK_200, session.retrievaltoken());
    }

    public Object retrieve(Request request, Response response) {
        var closedSession = getClosedSession(request, response);
        return writeJsonResponse(response, closedSession.sessionData());
    }

    public Object getTypes(Request request, Response response) {
        Map<String, ClassDefinition> classDefinition = new HashMap<>();
        for (var value : ValueType.values()) {
            classDefinition.put(value.name(), value.clazz() == null ? null : ClassDefinition.of(value.clazz()));
        }
        return writeJsonResponse(response, classDefinition);
    }

    public Object getType(Request request, Response response) {
        var parse = EnumUtil.parse(request.params(":type"), ValueType.class);
        if (parse == null) {
            halt(HttpStatus.BAD_REQUEST_400, "Invalid type");
        }

        if (parse.clazz() != null) {
            return writeJsonResponse(response, ClassDefinition.of(parse.clazz()));
        }
        return respondStatus(response, HttpStatus.NO_CONTENT_204);
    }

    public Object mobList(Request request, Response response) {
        var session = getSession(request, response);
        var identifier = session.readData(data -> data.settingsContainer().mobIdentifier());

        return writeJsonResponse(response, identifier);
    }

    public Object getMobSettings(Request request, Response response) {
        var configuration = getSessionMobSettings(request, response);
        return writeJsonResponse(response, DataDescriptionContainer.of(configuration));
    }

    public Object createMobSettings(Request request, Response response) {
        var session = getSession(request, response);
        var identifier = request.params(":identifier");
        var mobEditorPayload = session.sessionData();
        if (mobEditorPayload.settingsContainer().mobExists(identifier)) {
            halt(HttpStatus.CONFLICT_409, "Identifier already in use.");
        }

        mobEditorPayload.settingsContainer().createMob(identifier);
        return respondCreated(response);
    }

    public Object deleteMobSettings(Request request, Response response) {
        var session = getSession(request, response);
        var identifier = request.params(":identifier");
        var mobEditorPayload = session.sessionData();

        var removed = mobEditorPayload.settingsContainer().removeMob(identifier);

        if (!removed) halt(HttpStatus.NOT_MODIFIED_304, "Identifier not found.");

        return respondAccepted(response);
    }

    public Object getaAvailableTypes(Request request, Response response) {
        Map<BloodMobType, ClassDefinition> definitions = new HashMap<>();

        var mobSettings = getSessionMobSettings(request, response);

        for (var value : BloodMobType.values()) {
            if (mobSettings.wrapTypes().containsKey(value)) continue;
            definitions.put(value, ClassDefinition.of(value.typeSettingClazz()));
        }
        return writeJsonResponse(response, definitions);
    }

    public Object getSetWrapTypes(Request request, Response response) {
        Map<BloodMobType, DataDescriptionContainer<?, ?>> definitions = new HashMap<>();

        var mobSettings = getSessionMobSettings(request, response);

        for (var entry : mobSettings.wrapTypes().entrySet()) {
            var container = DataDescriptionContainer.of(entry.getValue());
            definitions.put(entry.getKey(), container);
        }

        return writeJsonResponse(response, definitions);
    }

    public Object removeWrapType(Request request, Response response) {
        var mobSettings = getSessionMobSettings(request, response);

        var type = EnumUtil.parse(request.params(":type"), BloodMobType.class);

        if (type == null) halt(HttpStatus.BAD_REQUEST_400, "Unknown BloodMobType");

        var remove = mobSettings.wrapTypes().remove(type) != null;
        return respondStatus(response, remove ? HttpStatus.ACCEPTED_202 : HttpStatus.NOT_MODIFIED_304);
    }

    public Object addWrapType(Request request, Response response) {
        var mobSettings = getSessionMobSettings(request, response);
        var params = request.params(":type");
        var type = EnumUtil.parse(params, BloodMobType.class);

        if (type == null) halt(HttpStatus.BAD_REQUEST_400, "Unknown BloodMobType");

        var typeSetting = readInput(request, type.typeSettingClazz());

        mobSettings.wrapTypes().put(type, typeSetting);

        return respondCreated(response);
    }

    public Object getSetting(Request request, Response response) {
        var mobSettings = getSessionMobSettings(request, response);
        var params = request.params(":setting");

        DataDescriptionContainer<?, ?> container;

        switch (params.toLowerCase(Locale.ROOT)) {
            case "extension" -> container = DataDescriptionContainer.of(mobSettings.extension());
            case "extension.equipment" -> container = DataDescriptionContainer.of(mobSettings.extension().equipment());
            case "equipment" -> container = DataDescriptionContainer.of(mobSettings.equipment());
            case "drops" -> container = DataDescriptionContainer.of(mobSettings.drops());
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

        return writeJsonResponse(response, container);
    }

    public Object setSetting(Request request, Response response) {
        var mobSettings = getSessionMobSettings(request, response);
        var params = request.params(":setting");

        var body = request.body();

        switch (params.toLowerCase(Locale.ROOT)) {
            case "extension" -> mobSettings.extension(readInput(request, Extension.class));
            case "extension.equipment" -> mobSettings.extension().equipment(readInput(request, Equipment.class));
            case "equipment" -> mobSettings.equipment(readInput(request, Equipment.class));
            case "drops" -> mobSettings.drops(readInput(request, Drops.class));
            default -> {
                halt(HttpStatus.BAD_REQUEST_400, "Unkown setting");
                return null;
            }
        }

        return respondCreated(response);
    }

    public Object addNode(Request request, Response response) {
        var nodes = getNodes(request, response);

        var node = readInput(request, Node.class);

        var chain = nodes.second.get(nodes.first);
        if (!(chain.getLast() instanceof NodeHolder)) {
            halt(HttpStatus.NOT_MODIFIED_304, "Chain is closed");
        }

        chain.addNode(node);
        return respondCreated(response);
    }

    public Object createNode(Request request, Response response) {
        var mobSettings = getSessionMobSettings(request, response);
        var type = request.params(":type");
        var nodeType = EnumUtil.parse(type, BehaviourNodeType.class);
        if (nodeType == null) {
            halt(HttpStatus.BAD_REQUEST_400, "Unknown node type");
            return null;
        }

        var node = readInput(request, Node.class);
        return respondStatus(response, HttpStatus.CREATED_201, String.valueOf(mobSettings.behaviour().addNode(nodeType, node)));
    }

    public Object removeLastNode(Request request, Response response) {
        var nodes = getNodes(request, response);
        var node = nodes.second.get(nodes.first);

        if (node.isLast()) {
            halt(HttpStatus.NOT_MODIFIED_304, "The last node cant be removed. Use delete instead.");
        }

        node.removeLast();

        return respondAccepted(response);
    }

    public Object deleteNode(Request request, Response response) {
        var nodes = getNodes(request, response);

        nodes.second.remove(nodes.first);

        return respondAccepted(response);
    }

    public Object nextNodes(Request request, Response response) {
        var nodes = getNodes(request, response);
        var node = nodes.second.get(nodes.first);
        var availableNodes = NodeRegistry.getAvailableNodes(node, ContextContainerFactory.mock(nodes.third));
        var definitions = availableNodes.stream().map(ClassDefinition::of).collect(Collectors.toList());
        return writeJsonResponse(response, definitions);
    }

    public Object getItems(Request request, Response response) {
        var session = getSession(request, response);
        var simpleItems = session.readData(data -> data.settingsContainer().items());
        return writeJsonResponse(response, simpleItems);
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
        return respondStatus(response, removed ? HttpStatus.OK_200 : HttpStatus.NOT_MODIFIED_304);
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
        if (nodeType == null) halt(HttpStatus.BAD_REQUEST_400, "Unknown node type");

        var nodes = mobSettings.behaviour().getNodes(nodeType);
        if (id >= nodes.size() || id < 0) {
            halt(HttpStatus.BAD_REQUEST_400, "Invalid id");
        }
        return Triple.of(id, nodes, nodeType);
    }

    public Object nextTypeNodes(Request request, Response response) {
        var type = request.params(":type");
        var nodeType = EnumUtil.parse(type, BehaviourNodeType.class);
        if (nodeType == null) halt(HttpStatus.BAD_REQUEST_400, "Unknown node type");

        var availableNodes = NodeRegistry.getAvailableNodes(nodeType);

        var definitions = availableNodes.stream().map(ClassDefinition::of).collect(Collectors.toList());
        return writeJsonResponse(response, definitions);
    }

    public Object getEventTypes(Request request, Response response) {
        var collect = Arrays.stream(BehaviourNodeType.values()).map(Enum::name).collect(Collectors.toList());
        return writeJsonResponse(response, collect, HttpStatus.OK_200);
    }

    public Object getChain(Request request, Response response) {
        var nodes = getNodes(request, response);
        var chain = nodes.second.get(nodes.first);
        var collect = chain.getClasses(new HashSet<>()).stream().map(ClassDefinition::of).collect(Collectors.toSet());
        var container = new NodeChain(chain, collect);
        return writeJsonResponse(response, container);
    }

    public Object getNodeType(Request request, Response response) {
        var type = request.params(":type");
        List<Class<?>> collect = Collections.emptyList();
        switch (type.toLowerCase(Locale.ROOT)) {
            case "node" -> collect = NodeRegistry.nodes();
            case "predicate" -> collect = NodeRegistry.predicates();
            default -> halt(HttpStatus.BAD_REQUEST_400, "Invalid node type");
        }

        return writeJsonResponse(response, ClassDefinition.of(collect));
    }

    public Object getNodeTypes(Request request, Response response) {
        Map<String, List<ClassDefinition>> definitions = new HashMap<>();
        definitions.put(ValueType.NODE.name(), ClassDefinition.of(NodeRegistry.nodes()));
        definitions.put(ValueType.PREDICATE.name(), ClassDefinition.of(NodeRegistry.predicates()));

        return writeJsonResponse(response, definitions);
    }

    private Object writeJsonResponse(Response response, Object object) {
        return writeJsonResponse(response, object, HttpStatus.OK_200);
    }

    private Object respondStatus(Response response, int status) {
        return respondStatus(response, status, null);
    }

    private Object respondStatus(Response response, int status, String message) {
        response.status(status);
        response.type(MediaType.PLAIN_TEXT_UTF_8.type());
        return message == null ? response.status() : message;
    }

    private <T> T readInput(Request request, Class<T> clazz) {
        try {
            return MobMapper.mapper().readValue(request.body(), clazz);
        } catch (JsonProcessingException e) {
            halt(HttpStatus.BAD_REQUEST_400, "Invalid format.");
        }
        return null;
    }

    private Object respondCreated(Response response) {
        return respondStatus(response, HttpStatus.CREATED_201);
    }

    private Object respondAccepted(Response response) {
        return respondStatus(response, HttpStatus.ACCEPTED_202);
    }

    private Object writeJsonResponse(Response response, Object object, int code) {
        try {
            response.body(MobMapper.mapper().writeValueAsString(object));
        } catch (JsonProcessingException e) {
            log.error("Could not serialize", e);
            halt(HttpStatus.INTERNAL_SERVER_ERROR_500);
        }

        response.type(MediaType.JSON_UTF_8.type());
        response.status(code);
        return response.body();
    }

    public Object removeGlobalDrop(Request request, Response response) {
        var session = getSession(request, response);
        var drop = readInput(request, Drop.class);
        var removed = session.sessionData().settingsContainer().globalDrops().remove(drop);
        return respondStatus(response, removed ? HttpStatus.ACCEPTED_202 : HttpStatus.NOT_MODIFIED_304);
    }

    public Object getGlobalDrops(Request request, Response response) {
        var session = getSession(request, response);
        return writeJsonResponse(response, session.sessionData().settingsContainer().globalDrops());
    }

    private static class NodeChain {
        Node data;
        Set<ClassDefinition> definitions;

        public NodeChain(Node data, Set<ClassDefinition> definitions) {
            this.data = data;
            this.definitions = definitions;
        }
    }
}
