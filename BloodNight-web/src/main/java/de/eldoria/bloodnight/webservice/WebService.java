package de.eldoria.bloodnight.webservice;

import de.eldoria.bloodnight.webservice.modules.MobEditor.MobEditorService;

import static spark.Spark.*;

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
                        "HEAD, GET, POST, OPTIONS");
            }

            return "OK";
        });

        path("/v1", () -> {
            path("/mobEditor", () -> {
                post("/submit", mobEditorService::submit);
                post("/close", mobEditorService::close);
                get("/retrieve", mobEditorService::retrieve);
                get("/type", mobEditorService::getTypes);
                post("/type/:type", mobEditorService::getType);
                get("/moblist", mobEditorService::mobList);
                path("/mobSetting", () -> {
                    get("/:identifier", mobEditorService::getMobSettings);
                    path("/identifier", () -> {
                        get("/:setting", mobEditorService::getSetting);
                        put("/:setting", mobEditorService::setSetting);
                        path("/behaviour", () -> {
                            path("/node", () -> {
                                put("/:type/:id", mobEditorService::addNode);
                                put("/:type", mobEditorService::createNode);
                                put("/:type/:id/removeLast", mobEditorService::removeLastNode);
                                delete("/:type/:id", mobEditorService::deleteNode);
                                get("/:type/:id/nextNodes", mobEditorService::nextNodes);
                            });
                        });
                    });
                });
                // items
                get("/items", mobEditorService::getItems);
                delete("/items/:id", mobEditorService::deleteItem);
            });
        });
    }
}
