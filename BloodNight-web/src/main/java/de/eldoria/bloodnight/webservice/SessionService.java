package de.eldoria.bloodnight.webservice;

import com.google.common.hash.Hashing;
import de.eldoria.bloodnight.webservice.exceptions.InvalidSessionException;
import de.eldoria.bloodnight.webservice.exceptions.SessionClosedException;
import de.eldoria.bloodnight.webservice.sessions.Session;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class SessionService {
    private Map<String, Session<?>> openSessions;
    private Map<String, Session<?>> closedSessions;

    public String openSession(Session<?> session) {
        String token = createSessionToken();
        openSessions.put(token, session);
        return token;
    }

    private String createSessionToken() {
        return Hashing
                .sha256()
                .hashString(System.currentTimeMillis() + "." + System.nanoTime(), StandardCharsets.UTF_8)
                .toString();
    }

    public Session<?> getSession(String token) throws SessionClosedException, InvalidSessionException {
        if (!openSessions.containsKey(token)) throw new InvalidSessionException();
        Session<?> session = openSessions.get(token);
        if (session.isClosed()) throw new SessionClosedException();
        return session;
    }
}
