package org.CardGame.server;


import org.CardGame.database.DBAccess;
import org.CardGame.model.HttpRequest;
import org.CardGame.model.User;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.SQLException;

public class UserLoginService {

    private DBAccess dbAccess;  // Eine DB-Access-Klasse zur Verwaltung der Datenbankinteraktionen

    public UserLoginService(DBAccess dbAccess) {
        this.dbAccess = dbAccess;
    }

    public String authenticateUser(HttpRequest request) {
        String requestBody = request.getBody();

        // JSON in ein User-Objekt umwandeln
        ObjectMapper objectMapper = new ObjectMapper();
        User credentials;

        try {
            credentials = objectMapper.readValue(requestBody, User.class);
        } catch (Exception e) {
            System.err.println("Failed to parse JSON: " + e.getMessage());
            return "{\"error\": \"Invalid JSON format\"}"; // Fehler bei der JSON-Verarbeitung
        }

        // Verarbeitet die Benutzeranmeldung mit den DB-Zugangsdaten
        return processLogin(credentials);
    }

    private String processLogin(User user) {
        try {
            // Überprüft die Benutzerdaten in der Datenbank
            return dbAccess.loginUser(user.getUsername(), user.getPassword());
        } catch (SQLException e) {
            // Handhabt SQL-Ausnahmen und gibt die entsprechende Fehlermeldung zurück
            return "{\"error\": \"Database error: " + e.getMessage() + "\"}";
        }
    }
}