package org.CardGame.model;

import java.util.Map;

public interface HttpRequestInterface {
    Map<String, String> getHeaders();
    String getBody();
    String getMethod();  // HTTP Methode
    String getPath();    // Pfad der Anfrag
}
