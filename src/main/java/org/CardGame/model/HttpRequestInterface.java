package org.CardGame.model;

import java.util.Map;

public interface HttpRequestInterface {
    Map<String, String> getHeaders();
    String getBody();
}
