package org.CardGame.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.CardGame.model.User;
import org.CardGame.database.DBAccess;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.stream.Collectors;
import java.sql.SQLException;

public class HttpServer {
    private static final int PORT = 10001;
    private static DBAccess dbAccess; // Declare the DBAccess instance

    public static void main(String[] args) {
        dbAccess = new DBAccess(); // Initialize the DBAccess instance

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server läuft auf Port " + PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                handleRequest(clientSocket);
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
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

            String responseBody;
            int status;

            if ("POST".equalsIgnoreCase(method) && "/sessions".equals(path)) {
                // Lese den Request-Body
                String requestBody = in.lines().collect(Collectors.joining(System.lineSeparator()));
                ObjectMapper objectMapper = new ObjectMapper();

                User credentials;
                try {
                    credentials = objectMapper.readValue(requestBody, User.class);
                } catch (Exception e) {
                    System.err.println("Failed to parse JSON: " + e.getMessage());
                    responseBody = "{\"error\": \"Invalid JSON format\"}";
                    status = 400; // Bad Request
                    sendResponse(out, responseBody, status);
                    return;
                }

                // Verarbeite die Anmeldeanfrage mit DBAccess
                responseBody = processLogin(credentials);
                status = 200; // OK
            } else {
                responseBody = "{\"error\": \"Not Found\"}";
                status = 404; // Not Found
            }

            // Build the response
            sendResponse(out, responseBody, status);

        } catch (IOException e) {
            System.err.println("IO Exception: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Exception: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Failed to close client socket: " + e.getMessage());
            }
        }
    }

    private static String processLogin(User user) {
        try {
            // Use DBAccess to check user credentials
            String token = dbAccess.loginUser(user.getUsername(), user.getPassword());
            if (token != null) {
                return "{\"token\": \"" + token + "\"}";
            } else {
                return "{\"error\": \"Invalid username or password\"}";
            }
        } catch (SQLException e) {
            // Handle SQL exceptions and return appropriate error response
            return "{\"error\": \"Database error: " + e.getMessage() + "\"}";
        }
    }

    private static void sendResponse(OutputStream out, String responseBody, int status) throws IOException {
        // Create the header
        HttpHeader header = new HttpHeader();
        header.setHeader("HTTP/1.1", status + " OK"); // Set status line
        header.setHeader("Content-Type", "application/json"); // Set content type
        header.setHeader("Content-Length", String.valueOf(responseBody.length()));

        // Build and send the response
        String httpResponse = header.buildResponse(responseBody);
        System.out.println("Response: " + responseBody); // Log response for debugging
        out.write(httpResponse.getBytes("UTF-8"));
        out.flush();
    }
}
