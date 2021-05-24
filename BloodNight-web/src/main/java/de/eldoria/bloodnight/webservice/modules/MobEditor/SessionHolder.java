package de.eldoria.bloodnight.webservice.modules.MobEditor;

import de.eldoria.bloodnight.webservice.SessionService;
import de.eldoria.bloodnight.webservice.sessions.Session;
import spark.Request;
import spark.Response;

public class SessionHolder<T> {
    private final SessionService<T> sessionService = SessionService.create();

    protected Session<T> getSession(Request request, Response response) {
        var token = request.headers("token");
        return sessionService.getSession(token);
    }

    public Session<T> openSession(T data) {
        return sessionService.openSession(data);
    }

    public Session<T> getClosedSession(Request request, Response response) {
        var token = request.headers("token");
        return sessionService.getClosedSession(token);
    }

    public Session<T> closeSession(Session<T> session) {
        return sessionService.closeSession(session);
    }
}
