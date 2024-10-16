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
import java.sql.SQLException;

public class HttpServer {
    private static final int PORT = 10001;
    private static DBAccess dbAccess; // Instanz von DBAccess, um auf die Datenbank zuzugreifen

    public static void main(String[] args) {
        dbAccess = new DBAccess(); // Initialisiert die DBAccess-Instanz

        try (ServerSocket serverSocket = new ServerSocket(PORT)) { // Erzeugt einen ServerSocket
            System.out.println("Server läuft auf Port " + PORT);
            while (true) {  // Endlosschleife für die Annahme von Verbindungen
                try {
                    Socket clientSocket = serverSocket.accept(); // Akzeptiert eine Client-Verbindung
                    handleRequest(clientSocket); // Verarbeitet die eingehende Anfrage
                } catch (IOException e) {
                    System.err.println("Fehler beim Verarbeiten der Anfrage: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    private static void handleRequest(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             OutputStream out = clientSocket.getOutputStream()) {

            // Liest die Anfragezeile
            String requestLine = in.readLine();
            System.out.println("Request Line: " + requestLine);

            // Überprüft, ob die Anfragezeile leer ist
            if (requestLine == null || requestLine.isEmpty()) {
                System.err.println("Empty request");
                sendResponse(out, "{\"error\": \"Empty request\"}", 400);
                return;
            }

            // Liest die Header der Anfrage
            HttpHeader httpHeader = new HttpHeader();
            int contentLength = readHeaders(in, httpHeader);

            // Extrahiere HTTP-Methode und Pfad aus der Anfrage
            String[] requestParts = requestLine.split(" ");
            if (requestParts.length < 2) {
                System.err.println("Invalid request line");
                sendResponse(out, "{\"error\": \"Invalid request\"}", 400);
                return;
            }
            String method = requestParts[0]; // HTTP-Methode (z.B. POST, GET)
            String path = requestParts[1];  // Anfrage-Pfad (z.B. /users)

            String responseBody;
            int status;


            if ("POST".equalsIgnoreCase(method) && "/users".equals(path)) {
                // Benutzerregistrierung
                responseBody = handleUserRegistration(in, contentLength, out);
                status = responseBody.startsWith("{\"error\"") ? 400 : 201;

            } else if ("POST".equalsIgnoreCase(method) && "/sessions".equals(path)) {
                // Benutzeranmeldung
                responseBody = handleUserLogin(in, contentLength, out);
                status = responseBody.startsWith("{\"token\"") ? 200 : 401;

            } else {  // Rückgabe einer Fehlermeldung, wenn der Pfad nicht gefunden wurde
                responseBody = "{\"error\": \"Not Found\"}";
                status = 404; // Not Found
            }

            // Sendet die Antwort an den Client
            sendResponse(out, responseBody, status);

        } catch (IOException e) {
            System.err.println("IO Exception: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Exception: " + e.getMessage());
        } finally {
            try {
                clientSocket.close(); // Schließt den Client-Socket
            } catch (IOException e) {
                System.err.println("Failed to close client socket: " + e.getMessage());
            }
        }
    }


    private static String handleUserRegistration(BufferedReader in, int contentLength, OutputStream out) throws IOException {
        // Liest den Anfrage-Body für die Benutzerregistrierung
        String requestBody = readRequestBody(in, contentLength, out);
        ObjectMapper objectMapper = new ObjectMapper();
        User newUser;

        try {
            // Wandelt den JSON-Body in ein User-Objekt um
            newUser = objectMapper.readValue(requestBody, User.class);
        } catch (Exception e) {
            System.err.println("Failed to parse JSON: " + e.getMessage());
            return "{\"error\": \"Invalid JSON format\"}";
        }

        // Verarbeitet die Benutzerregistrierung
        return processUserRegistration(newUser);
    }

    private static String handleUserLogin(BufferedReader in, int contentLength, OutputStream out) throws IOException {
        // Liest den Anfrage-Body für die Benutzeranmeldung
        String requestBody = readRequestBody(in, contentLength, out);
        ObjectMapper objectMapper = new ObjectMapper(); // Erzeugt ein ObjectMapper für JSON-Verarbeitung
        User credentials;

        try {
            // Wandelt den JSON-Body in ein User-Objekt um
            credentials = objectMapper.readValue(requestBody, User.class);
            System.out.println("Parsed User: " + credentials.getUsername() + ", " + credentials.getPassword());
        } catch (Exception e) {
            System.err.println("Failed to parse JSON: " + e.getMessage());
            return "{\"error\": \"Invalid JSON format\"}";
        }

        // Verarbeitet die Anmeldeanfrage mit DBAccess
        return processLogin(credentials);
    }

    private static int readHeaders(BufferedReader in, HttpHeader httpHeader) throws IOException {
        String headerLine;
        int contentLength = 0; // Initialisiert contentLength
        int maxHeaders = 100; // Maximale Anzahl der Header (zum Schutz vor Endlosschleifen)
        int headerCount = 0;

        while (headerCount < maxHeaders && (headerLine = in.readLine()) != null && !headerLine.isEmpty()) {
            System.out.println("Header: " + headerLine);
            String[] parts = headerLine.split(": "); // Trennt Header-Namen und -Werte
            if (parts.length == 2) {
                httpHeader.setHeader(parts[0], parts[1]);
                // Überprüft auf Content-Length
                if ("Content-Length".equalsIgnoreCase(parts[0])) {
                    contentLength = Integer.parseInt(parts[1].trim());
                }
            }
            headerCount++;
        }

        // Überprüft auf zu viele Header und wirft eine Ausnahme
        if (headerCount >= maxHeaders) {
            throw new IOException("Too many headers; possible infinite loop detected.");
        }

        return contentLength;  // Gibt die Content-Length zurück
    }

    private static String readRequestBody(BufferedReader in, int contentLength, OutputStream out) throws IOException {
        char[] bodyChars = new char[contentLength];  // Erstellt ein Char-Array für den Body
        int read = in.read(bodyChars, 0, contentLength); // Liest den Body
        if (read != contentLength) {
            System.err.println("Expected " + contentLength + " chars, but got " + read);
            sendResponse(out, "{\"error\": \"Incomplete request body\"}", 400);
            return null; // Gibt null zurück, wenn die Anfrage unvollständig ist
        }
        return new String(bodyChars); // Gibt den gelesenen Body als String zurück
    }

    private static String processUserRegistration(User user) {
        try {
            // Überprüft, ob der Benutzer bereits existiert
            if (dbAccess.userExists(user.getUsername())) {
                return "{\"error\": \"User already exists\"}"; // Return error message
            } else {
                // Erstellt den Benutzer in der Datenbank
                boolean isCreated = dbAccess.createUser(user);
                if (isCreated) {
                    return "{\"message\": \"User created successfully\"}"; // Erfolgsnachricht
                } else {
                    return "{\"error\": \"Failed to create user\"}"; // Gibt eine Fehlermeldung zurück
                }
            }
        } catch (SQLException e) {
            // Handhabt SQL-Ausnahmen und gibt die entsprechende Fehlermeldung zurück
            return "{\"error\": \"Database error: " + e.getMessage() + "\"}"; // Return DB error
        }
    }

    private static String processLogin(User user) {
        try {
            // Überprüft die Benutzerdaten in der Datenbank
            return dbAccess.loginUser(user.getUsername(), user.getPassword());
        } catch (SQLException e) {
            // Handhabt SQL-Ausnahmen und gibt die entsprechende Fehlermeldung zurück
            return "{\"error\": \"Database error: " + e.getMessage() + "\"}";
        }
    }

    private static void sendResponse(OutputStream out, String responseBody, int status) throws IOException {
        HttpHeader httpHeader = new HttpHeader(); // Create a new HttpHeader instance
        String response = httpHeader.buildResponse(status, responseBody); // Build the response using HttpHeader
        out.write(response.getBytes());
        out.flush();
    }

}
