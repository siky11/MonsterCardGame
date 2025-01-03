package org.CardGame.model;

import java.util.Map;

public class HttpRequest {
    private String method;   // HTTP-Methode (z.B. GET, POST)
    private String path;     // Pfad der Anfrage (z.B. /users)
    private Map<String, String> headers;  // Header der Anfrage
    private String body;     // Body der Anfrage

    public HttpRequest(String method, String path, Map<String, String> headers, String body) {
        this.method = method;
        this.path = path;
        this.headers = headers;
        this.body = body;
    }

    // Getter und Setter für alle Felder
    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }


    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }
}
