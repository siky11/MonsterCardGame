package org.CardGame.server;

import org.CardGame.model.HttpRequest;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;


public class HttpRequestParser {
    private BufferedReader input;
    private HttpHeader headers;
    private OutputStream output;
    private HttpResponseSender responseSender;

    public HttpRequestParser(BufferedReader input, OutputStream output) {
        this.input = input;
        this.output = output;
        this.headers = new HttpHeader();
        this.responseSender = responseSender != null ? responseSender : new HttpResponseSender();
    }

    public HttpRequest parse() throws IOException {
        String requestLine = input.readLine(); // Liest die erste Zeile (z.B. "POST /users HTTP/1.1")
        if (requestLine == null || requestLine.isEmpty()) {
            responseSender.send(output, "{\"error\": \"Empty request\"}", 400);
            return null; // Keine Anfrage oder Fehler
        }

        String[] parts = requestLine.split(" ");
        if (parts.length < 2) { // Prüfe, ob mindestens 2 Elemente vorhanden sind
            responseSender.send(output, "{\"error\": \"Invalid request line\"}", 400);
            return null;
        }
        String method = parts[0];  // HTTP Methode, z.B. POST
        String path = parts[1];    // Pfad, z.B. /users


        // Liest alle Header
        int contentLength = readHeaders(input, headers);

        // Liest den Body, wenn Content-Length angegeben wurde
        String body = "";
        if (contentLength > 0) {
            body = readRequestBody(input, contentLength, output);
        }
        Map<String, String> headerMap = headers.getHeaders();
        return new HttpRequest(method, path, headerMap, body); // Gibt eine vollständige Request zurück
    }

    private int readHeaders(BufferedReader in, HttpHeader httpHeader) throws IOException {
        String headerLine;
        int contentLength = 0;
        int maxHeaders = 100;
        int headerCount = 0;

        while (headerCount < maxHeaders && (headerLine = in.readLine()) != null && !headerLine.isEmpty()) {
            System.out.println("Header: " + headerLine);
            String[] parts = headerLine.split(": ");
            if (parts.length == 2) {
                httpHeader.setHeader(parts[0], parts[1]);
                if ("Content-Length".equalsIgnoreCase(parts[0])) {
                    contentLength = Integer.parseInt(parts[1].trim());
                }
            }
            headerCount++;
        }

        if (headerCount >= maxHeaders) {
            throw new IOException("Too many headers; possible infinite loop detected.");
        }

        return contentLength;
    }

    private String readRequestBody(BufferedReader in, int contentLength, OutputStream out) throws IOException {
        char[] bodyChars = new char[contentLength];
        int read = in.read(bodyChars, 0, contentLength);
        if (read != contentLength) {
            System.err.println("Expected " + contentLength + " chars, but got " + read);
            responseSender.send(out, "{\"error\": \"Incomplete request body\"}", 400);
            return null;
        }
        return new String(bodyChars);
    }
}