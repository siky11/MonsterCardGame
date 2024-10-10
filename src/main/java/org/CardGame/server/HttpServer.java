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
                try {
                    Socket clientSocket = serverSocket.accept();
                    handleRequest(clientSocket);
                } catch (IOException e) {
                    System.err.println("Fehler beim Verarbeiten der Anfrage: " + e.getMessage());
                    // Optional: Möglicherweise möchtest du den Server nicht stoppen, sondern weiterlaufen lassen
                }
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    private static void handleRequest(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             OutputStream out = clientSocket.getOutputStream()) {

            // Lies die Anfragezeile
            String requestLine = in.readLine();
            System.out.println("Request Line: " + requestLine);

            if (requestLine == null || requestLine.isEmpty()) {
                System.err.println("Empty request");
                sendResponse(out, "{\"error\": \"Empty request\"}", 400);
                return;
            }
            System.out.println("Received request: " + requestLine);

            // Lese die Header
            HttpHeader httpHeader = new HttpHeader();
            int contentLength = readHeaders(in, httpHeader);

            // Extrahiere HTTP-Methode und Pfad
            String[] requestParts = requestLine.split(" ");
            if (requestParts.length < 2) {
                System.err.println("Invalid request line");
                sendResponse(out, "{\"error\": \"Invalid request\"}", 400);
                return;
            }
            String method = requestParts[0];
            String path = requestParts[1];

            String responseBody;
            int status;

            if ("POST".equalsIgnoreCase(method) && "/sessions".equals(path)) {
                // Benutzeranmeldung
                responseBody = handleUserLogin(in, contentLength, out);
                status = responseBody.startsWith("{\"token\"") ? 200 : 401;

            } else if ("POST".equalsIgnoreCase(method) && "/users".equals(path)) {
                // Benutzerregistrierung
                responseBody = handleUserRegistration(in, contentLength, out);
                status = responseBody.startsWith("{\"error\"") ? 400 : 201;

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


    private static String handleUserRegistration(BufferedReader in, int contentLength, OutputStream out) throws IOException {
        String requestBody = readRequestBody(in, contentLength, out);
        ObjectMapper objectMapper = new ObjectMapper();
        User newUser;

        try {
            newUser = objectMapper.readValue(requestBody, User.class);
            System.out.println("Parsed User for Registration: " + newUser.getUsername() + ", " + newUser.getPassword());
        } catch (Exception e) {
            System.err.println("Failed to parse JSON: " + e.getMessage());
            return "{\"error\": \"Invalid JSON format\"}";
        }

        // Verarbeite die Benutzerregistrierung mit DBAccess
        return processUserRegistration(newUser);
    }

    private static String handleUserLogin(BufferedReader in, int contentLength, OutputStream out) throws IOException {
        String requestBody = readRequestBody(in, contentLength, out);
        ObjectMapper objectMapper = new ObjectMapper();
        User credentials;

        try {
            credentials = objectMapper.readValue(requestBody, User.class);
            System.out.println("Parsed User: " + credentials.getUsername() + ", " + credentials.getPassword());
        } catch (Exception e) {
            System.err.println("Failed to parse JSON: " + e.getMessage());
            return "{\"error\": \"Invalid JSON format\"}";
        }

        // Verarbeite die Anmeldeanfrage mit DBAccess
        return processLogin(credentials);
    }

    private static int readHeaders(BufferedReader in, HttpHeader httpHeader) throws IOException {
        String headerLine;
        int contentLength = 0; // Initialisiere contentLength
        int maxHeaders = 100; // Maximale Anzahl der Header (zum Schutz vor Endlosschleifen)
        int headerCount = 0;

        while (headerCount < maxHeaders && (headerLine = in.readLine()) != null && !headerLine.isEmpty()) {
            System.out.println("Header: " + headerLine);
            String[] parts = headerLine.split(": ");
            if (parts.length == 2) {
                httpHeader.setHeader(parts[0], parts[1]);
                // Überprüfe auf Content-Length
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

    private static String readRequestBody(BufferedReader in, int contentLength, OutputStream out) throws IOException {
        char[] bodyChars = new char[contentLength];
        int read = in.read(bodyChars, 0, contentLength);
        if (read != contentLength) {
            System.err.println("Expected " + contentLength + " chars, but got " + read);
            sendResponse(out, "{\"error\": \"Incomplete request body\"}", 400);
            return null; // In diesem Fall null zurückgeben
        }
        return new String(bodyChars);
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
        String response = "HTTP/1.1 " + status + " " + getStatusMessage(status) + "\r\n" +
                "Content-Type: application/json\r\n" +
                "Content-Length: " + responseBody.length() + "\r\n" +
                "\r\n" +
                responseBody;
        out.write(response.getBytes());
        out.flush();
    }


    private static String processUserRegistration(User user) {
        try {
            // Überprüfen, ob der Benutzer bereits existiert
            if (dbAccess.userExists(user.getUsername())) {
                return "{\"error\": \"User already exists\"}";
            }

            // Benutzer erstellen
            boolean isCreated = dbAccess.createUser(user);
            if (isCreated) {
                return "{\"message\": \"User created successfully\"}";
            } else {
                return "{\"error\": \"Failed to create user\"}";
            }
        } catch (SQLException e) {
            return "{\"error\": \"Database error: " + e.getMessage() + "\"}";
        }
    }


    private static String getStatusMessage(int status) {
        switch (status) {
            case 200: return "OK";
            case 400: return "Bad Request";
            case 401: return "Unauthorized";
            case 404: return "Not Found";
            default: return "Internal Server Error";
        }
    }
}
