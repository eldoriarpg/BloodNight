package de.eldoria.bloodnight.webservice.sessions;

import com.google.common.hash.Hashing;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.function.Consumer;
import java.util.function.Function;

public class Session<T> {
    private static long incr = 0;
    private final String accessToken;
    private String retrievalToken;
    Instant started = Instant.now();
    Instant lastActive = Instant.now();
    boolean closed = false;
    T sessionData;

    public Session(T sessionData) {
        this.accessToken = createToken();
        this.sessionData = sessionData;
    }

    /**
     * Closes
     */
    public void close() {
        if(closed) throw new IllegalStateException();
        closed = true;
        retrievalToken = createToken();
    }

    public void alterData(Consumer<T> apply) {
        lastActive = Instant.now();
        apply.accept(sessionData);
    }

    public <R> R readData(Function<T, R> apply) {
        lastActive = Instant.now();
        return apply.apply(sessionData);
    }

    public Instant started() {
        return started;
    }

    public Instant lastActive() {
        return lastActive;
    }

    public boolean isClosed() {
        return closed;
    }

    public String accessToken() {
        return accessToken;
    }

    public String retrievaltoken() {
        lastActive = Instant.now();
        return retrievalToken;
    }

    public T sessionData() {
        lastActive = Instant.now();
        return sessionData;
    }

    private String createToken() {
        return Hashing.sha256()
                .hashString(System.currentTimeMillis() + "." + System.nanoTime() + "." + getId(), StandardCharsets.UTF_8)
                .toString();
    }

    private static synchronized long getId() {
        return incr++;
    }
}
