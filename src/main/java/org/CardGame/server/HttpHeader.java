package org.CardGame.server;

import java.util.HashMap;
import java.util.Map;

// Die Klasse HttpHeader verwaltet HTTP-Header für die Serverantwort
public class HttpHeader {
    private Map<String, String> headers; // Map zur Speicherung von Headern und ihren Werten

    // Konstruktor initialisiert die Header-Map und setzt den Content-Type auf JSON
    public HttpHeader() {
        headers = new HashMap<>();
        setHeader("Content-Type", "application/json");
    }

    // Methode zum Setzen eines Headerwerts für einen bestimmten Schlüssel
    public void setHeader(String key, String value) {
        headers.put(key, value); // Fügt den Header zur Map hinzu
    }

    // Methode zum Abrufen des Wertes eines bestimmten Headers
    public String getHeader(String key) {
        return headers.get(key);
    }

    // Methode zum Erstellen einer Header-Zeichenkette aus der Map
    public String buildHeaders() {
        StringBuilder headerString = new StringBuilder(); // StringBuilder zur effizienten Zeichenketten-Konstruktion
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            headerString.append(entry.getKey()).append(": ").append(entry.getValue()).append("\r\n");
        }
        return headerString.toString();
    }

    // Methode zum Erstellen einer vollständigen HTTP-Antwort einschließlich Header und Body
    public String buildResponse(int status, String body) {
        // Setzt die Content-Length basierend auf der Länge des Antwortkörpers
        setHeader("Content-Length", String.valueOf(body.length()));
        String statusLine = "HTTP/1.1 " + status + " " + getStatusMessage(status) + "\r\n";
        return statusLine + buildHeaders() + "\r\n" + body; // Add a double CRLF to separate headers from body
    }

    // Private Methode zur Bestimmung der Statusnachricht basierend auf dem Statuscode
    private static String getStatusMessage(int status) {
        switch (status) {
            case 200: return "- Login successful";
            case 201: return "- Created";
            case 400: return "- User already exists";
            case 401: return "- Login failed";
            case 404: return "Not Found";
            case 409: return "Conflict";
            default: return "Internal Server Error";
        }
    }
}