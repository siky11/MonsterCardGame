package org.CardGame.server;

import java.util.HashMap;
import java.util.Map;

public class HttpHeader {
    private Map<String, String> headers;

    public HttpHeader() {
        headers = new HashMap<>();
    }

    public void setHeader(String key, String value) {
        headers.put(key, value);
    }

    public String getHeader(String key) {
        return headers.get(key);
    }

    public String buildHeaders() {
        StringBuilder headerString = new StringBuilder();
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            headerString.append(entry.getKey()).append(": ").append(entry.getValue()).append("\r\n");
        }
        return headerString.toString();
    }

    public String buildResponse(String body) {
        // Set the content length based on the body length
        setHeader("Content-Length", String.valueOf(body.length()));
        return buildHeaders() + "\r\n" + body; // Add a double CRLF to separate headers from body
    }
}