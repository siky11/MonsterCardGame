package org.CardGame.server;

import java.io.IOException;
import java.io.OutputStream;

public class HttpResponseSender {

    // Methode zum Senden der Antwort
    public void send(OutputStream out, String body, int status) throws IOException {
        HttpHeader headers = new HttpHeader();
        headers.setHeader("Content-Type", "application/json");
        headers.setHeader("Content-Length", String.valueOf(body.length()));

        String statusLine = "HTTP/1.1 " + status + " " + getStatusMessage(status) + "\r\n";
        String response = statusLine + headers.buildHeaders() + "\r\n" + body;

        out.write(response.getBytes());
        out.flush();
    }

    private String getStatusMessage(int status) {
        switch (status) {
            case 200: return "OK";
            case 201: return "Created";
            case 400: return "Bad Request";
            case 401: return "Unauthorized";
            case 404: return "Not Found";
            case 409: return "Conflict";
            default: return "Internal Server Error";
        }
    }
}

