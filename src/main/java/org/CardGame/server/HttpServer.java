package org.CardGame.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.CardGame.model.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.stream.Collectors;

public class HttpServer {
    private static final int PORT = 10001;

    public static void main(String[] args) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server läuft auf Port " + PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                handleRequest(clientSocket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleRequest(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             OutputStream out = clientSocket.getOutputStream()) {

            String requestLine = in.readLine();
            System.out.println("Received request: " + requestLine);

            // HTTP-Methode und Pfad extrahieren
            String[] requestParts = requestLine.split(" ");
            String method = requestParts[0];
            String path = requestParts[1];

            if ("POST".equalsIgnoreCase(method) && "/sessions".equals(path)) {
                // Lese den Request-Body
                String requestBody = in.lines().collect(Collectors.joining(System.lineSeparator()));
                ObjectMapper objectMapper = new ObjectMapper();
                User credentials = objectMapper.readValue(requestBody, User.class);

                // Verarbeite die Anmeldeanfrage
                String response = processLogin(credentials);

                // Sende die Antwort
                String httpResponse = "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: application/json\r\n" +
                        "\r\n" +
                        response;
                out.write(httpResponse.getBytes("UTF-8"));
            } else {
                // Unbekannte Methode oder Pfad
                String httpResponse = "HTTP/1.1 404 Not Found\r\n\r\n";
                out.write(httpResponse.getBytes("UTF-8"));
            }
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static String processLogin(User user) {
        // Beispielüberprüfung: Hier würdest du die Logik für die Benutzerüberprüfung implementieren
        if ("kienboec".equals(user.getUsername()) && "daniel".equals(user.getPassword())) {
            // Erfolgreiche Anmeldung
            return "{\"token\": \"" + user.getUsername() + "-mtcgToken\"}";
        } else {
            // Fehlgeschlagene Anmeldung
            return "{\"error\": \"Invalid username or password\"}";
        }
    }
}
