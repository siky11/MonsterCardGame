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

    // Methode, um die Header als Map zurückzugeben
    public Map<String, String> getHeaders() {
        return headers; // Gibt die Map von Headern zurück
    }

    // Methode zum Erstellen einer Header-Zeichenkette aus der Map
    public String buildHeaders() {
        StringBuilder headerString = new StringBuilder(); // StringBuilder zur effizienten Zeichenketten-Konstruktion
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            headerString.append(entry.getKey()).append(": ").append(entry.getValue()).append("\r\n");
        }
        return headerString.toString();
    }
}