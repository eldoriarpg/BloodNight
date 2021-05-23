package de.eldoria.bloodnight.webservice;

import com.fasterxml.jackson.core.JacksonException;
import de.eldoria.bloodnight.bloodmob.serialization.container.MobEditorPayload;
import de.eldoria.bloodnight.bloodmob.serialization.mapper.MobMapper;
import de.eldoria.bloodnight.webservice.sessions.MobEditorSession;
import org.eclipse.jetty.http.HttpStatus;

import static spark.Spark.*;

public class WebService {
    private static WebService instance;
    private final SessionService sessionService = new SessionService();

    public static void main(String[] args) {
        WebService.instance = new WebService();
        instance.ignite();
    }

    private void ignite() {
        path("/v1", () -> {
            post("/editMobs", ((request, response) -> {
                MobEditorPayload mobEditorPayload;
                try {
                    mobEditorPayload = MobMapper.mapper().readValue(request.body(), MobEditorPayload.class);
                } catch (JacksonException e) {
                    response.status();
                    halt(HttpStatus.BAD_REQUEST_400, "Invalid payload");
                    return null;
                }
                String s = sessionService.openSession(new MobEditorSession(mobEditorPayload));
                response.body(s);
                response.status(HttpStatus.CREATED_201);
                return response.body();
            }));
        });
    }
}
