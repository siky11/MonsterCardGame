package org.CardGame.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest implements HttpRequestInterface{
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

    // Standardkonstruktor (ohne Argumente)
    public HttpRequest() {
        this.headers = new HashMap<>();
        this.body = "";
    }

    // Getter und Setter für alle Felder

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public void setBody(String body) {
        this.body = body;
    }
    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }
    @Override
    public String getBody() {
        return body;
    }
}
