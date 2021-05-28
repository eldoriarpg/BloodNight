package de.eldoria.bloodnight.webservice;

import de.eldoria.bloodnight.webservice.modules.MobEditor.MobEditorService;

import static spark.Spark.delete;
import static spark.Spark.get;
import static spark.Spark.ipAddress;
import static spark.Spark.options;
import static spark.Spark.path;
import static spark.Spark.port;
import static spark.Spark.post;
import static spark.Spark.put;

public class WebService {
    private static WebService instance;
    private final MobEditorService mobEditorService = new MobEditorService();

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

        path("/v1", () -> {
            path("/mobEditor", () -> {
                post("/submit", mobEditorService::submit);
                post("/close", mobEditorService::close);
                get("/retrieve/:token", mobEditorService::retrieve);

                get("/types", mobEditorService::getTypes);
                get("/type/:type", mobEditorService::getType);

                get("/moblist", mobEditorService::mobList);

                path("/mobSetting", () -> {

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
                                put("/:type/:id", mobEditorService::addNode);
                                put("/:type", mobEditorService::createNode);
                                put("/:type/:id/removeLast", mobEditorService::removeLastNode);
                                delete("/:type/:id", mobEditorService::deleteNode);
                                get("/:type/:id/nextNodes", mobEditorService::nextNodes);
                                get("/:type/nextNodes", mobEditorService::nextTypeNodes);
                            });
                        });
                    });
                });

                get("/items", mobEditorService::getItems);
                delete("/items/:id", mobEditorService::deleteItem);
            });
        });
    }
}
