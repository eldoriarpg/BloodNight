package de.eldoria.bloodnight.webservice.sessions;

import java.io.Closeable;
import java.io.IOException;
import java.time.Instant;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class Session<T> implements Closeable {
    Instant started = Instant.now();
    Instant lastActive = Instant.now();
    boolean closed = false;
    T sessionData;

    public Session(T sessionData) {
        this.sessionData = sessionData;
    }

    @Override
    public void close() throws IOException {
        closed = true;
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

    public T sessionData() {
        return sessionData;
    }
}
