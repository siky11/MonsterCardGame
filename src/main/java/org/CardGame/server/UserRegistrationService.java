package org.CardGame.server;

import org.CardGame.database.DBAccess;
import org.CardGame.model.HttpRequest;
import org.CardGame.model.User;

import java.sql.SQLException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UserRegistrationService {

    private DBAccess dbAccess; // Annahme: du hast eine DB-Access-Klasse für DB-Interaktionen

    public UserRegistrationService(DBAccess dbAccess) {
        this.dbAccess = dbAccess;
    }

    public String registerUser(HttpRequest request) {
        String requestBody = request.getBody();

        // JSON in ein User-Objekt umwandeln
        ObjectMapper objectMapper = new ObjectMapper();
        User newUser;
        try {
            newUser = objectMapper.readValue(requestBody, User.class);
        } catch (Exception e) {
            System.err.println("Invalid JSON: " + e.getMessage());
            return "{\"error\": \"Invalid JSON format\"}"; // Fehler bei der JSON-Verarbeitung
        }

        // Benutzerregistrierung verarbeiten
        return processUserRegistration(newUser);
    }

    private String processUserRegistration(User user) {
        try {
            // Überprüft, ob der Benutzer bereits existiert
            if (dbAccess.userExists(user.getUsername())) {
                return "{\"error\": \"User already exists\"}"; // Benutzer existiert bereits
            } else {
                // Erstellt den Benutzer in der Datenbank
                boolean isCreated = dbAccess.createUser(user);
                if (isCreated) {
                    return "{\"message\": \"User created successfully\"}"; // Benutzer erfolgreich erstellt
                } else {
                    return "{\"error\": \"Failed to create user\"}"; // Datenbankfehler
                }
            }
        } catch (SQLException e) {
            return "{\"error\": \"Database error: " + e.getMessage() + "\"}"; // Datenbankfehlerbehandlung
        }
    }
}