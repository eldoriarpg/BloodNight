package de.eldoria.bloodnight.webservice;

import de.eldoria.bloodnight.webservice.modules.mobeditor.MobEditorService;
import org.slf4j.Logger;

import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;
import static spark.Spark.before;
import static spark.Spark.delete;
import static spark.Spark.get;
import static spark.Spark.halt;
import static spark.Spark.ipAddress;
import static spark.Spark.options;
import static spark.Spark.path;
import static spark.Spark.port;
import static spark.Spark.post;
import static spark.Spark.put;

public class WebService {
    private static WebService instance;
    private final MobEditorService mobEditorService = new MobEditorService();

    private static final Logger log = getLogger(WebService.class);

    public static void main(String[] args) {
        WebService.instance = new WebService();
        instance.ignite();
    }

    private void ignite() {
        port(8888);
        ipAddress("0.0.0.0");

        options("/*", (request, response) -> {
            var accessControlRequestHeaders = request
                    .headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                response.header("Access-Control-Allow-Headers",
                        "token");
            }

            var accessControlRequestMethod = request
                    .headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                response.header("Access-Control-Allow-Methods",
                        "HEAD, GET, DELETE, PATCH, POST, OPTIONS");
            }

            return "OK";
        });

        before((request, response) -> {
            log.trace("Received request on route: {} {}\nHeaders:\n{}\nBody:\n{}",
                    request.requestMethod() + " " + request.uri(),
                    request.queryString(),
                    request.headers().stream().map(h -> "   " + h + ": " + request.headers(h))
                            .collect(Collectors.joining("\n")),
                    request.body().substring(0, Math.min(request.body().length(), 180)));
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Allow-Headers", "*");
        });


        path("/v1", () -> {
            path("/mobeditor", () -> {
                post("/submit", mobEditorService::submit);
                post("/close", mobEditorService::close);
                get("/retrieve/:token", mobEditorService::retrieve);

                get("/types", mobEditorService::getTypes);
                get("/type/:type", mobEditorService::getType);

                get("/moblist", mobEditorService::mobList);

                path("/mobsetting", () -> {

                    get("/:identifier", mobEditorService::getMobSettings);
                    post("/:identifier", mobEditorService::createMobSettings);
                    delete("/:identifier", mobEditorService::deleteMobSettings);

                    path("/:identifier", () -> {
                        get("/wraptypes/available", mobEditorService::getaAvailableTypes);
                        get("/wraptypes/set", mobEditorService::getSetWrapTypes);
                        delete("/wraptypes/:type", mobEditorService::removeWrapType);
                        put("/wraptypes/:type", mobEditorService::addWrapType);

                        get("/:setting", mobEditorService::getSetting);
                        put("/:setting", mobEditorService::setSetting);

                        path("/behaviour", () -> {
                            path("/node", () -> {
                                get("/types", mobEditorService::getEventTypes);
                                get("/nodes/:type", mobEditorService::getNodeType);
                                get("/nodes", mobEditorService::getNodeTypes);
                                get("/:type/nextNodes", mobEditorService::nextTypeNodes);
                                put("/:type", mobEditorService::createNode);
                                put("/:type/:id", mobEditorService::addNode);
                                delete("/:type/:id/last", mobEditorService::removeLastNode);
                                delete("/:type/:id", mobEditorService::deleteNode);
                                get("/:type/:id/nextNodes", mobEditorService::nextNodes);
                                get("/:type/:id", mobEditorService::getChain);
                            });
                        });
                    });
                });

                get("/items", mobEditorService::getItems);
                delete("/items/:id", mobEditorService::deleteItem);
                get("/globaldrops", mobEditorService::getGlobalDrops);
                delete("/globaldrops", mobEditorService::removeGlobalDrop);
            });
        });
    }
}
