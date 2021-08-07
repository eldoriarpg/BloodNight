package de.eldoria.bloodnight.webservice.configuration.elements;

public class General {
    String host = "0.0.0.0";
    int port = 8888;

    public String host() {
        return host;
    }

    public int port() {
        return port;
    }
}
