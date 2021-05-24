package de.eldoria.bloodnight.webservice;

import de.eldoria.bloodnight.webservice.sessions.Session;
import org.eclipse.jetty.http.HttpStatus;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static spark.Spark.halt;

public class SessionService<T> implements Runnable {
    private final Map<String, Session<T>> openSessions = new HashMap<>();
    private final Map<String, Session<T>> closedSessions = new HashMap<>();
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    private SessionService() {
    }

    public static <T> SessionService<T> create() {
        var sessionService = new SessionService<T>();
        sessionService.init();
        return sessionService;
    }

    private void init() {
        executorService.scheduleAtFixedRate(this, 15, 15, TimeUnit.MINUTES);
    }

    public Session<T> openSession(T data) {
        var session = new Session<>(data);
        openSessions.put(session.accessToken(), session);
        return session;
    }


    public Session<T> getSession(String token) {
        if (!openSessions.containsKey(token)) halt(HttpStatus.UNAUTHORIZED_401, "Invalid Session");
        var session = openSessions.get(token);
        if (session.isClosed()) halt(HttpStatus.FORBIDDEN_403, "Session is closed.");
        return session;
    }

    public Session<T> getClosedSession(String token) {
        if (!closedSessions.containsKey(token)) halt(HttpStatus.UNAUTHORIZED_401);
        return closedSessions.get(token);
    }

    public Session<T> closeSession(Session<T> session) {
        session.close();
        closedSessions.put(session.retrievaltoken(), openSessions.remove(session.accessToken()));
        return session;
    }

    @Override
    public void run() {
        openSessions.entrySet().removeIf(next -> next.getValue().lastActive().isBefore(Instant.now().minus(60, ChronoUnit.MINUTES)));
        closedSessions.entrySet().removeIf(next -> next.getValue().lastActive().isBefore(Instant.now().minus(30, ChronoUnit.MINUTES)));
    }
}
