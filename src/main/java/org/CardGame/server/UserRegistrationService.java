package org.CardGame.server;

import org.CardGame.database.DBAccess;
import org.CardGame.database.DBAccessInterface;
import org.CardGame.database.UserDB;
import org.CardGame.database.UserDBInterface;
import org.CardGame.model.HttpRequest;
import org.CardGame.model.HttpRequestInterface;
import org.CardGame.model.User;

import java.sql.SQLException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UserRegistrationService {

    private DBAccessInterface dbAccess; // Annahme: du hast eine DB-Access-Klasse für DB-Interaktionen
    private UserDBInterface userDB;

    public UserRegistrationService(DBAccessInterface dbAccess, UserDBInterface userDB) {
        this.dbAccess = dbAccess;
        this.userDB = userDB;
    }

    public String registerUser(HttpRequestInterface request) {
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
            if (userDB.userExists(user.getUsername())) {
                return "{\"error\": \"User already exists\"}"; // Benutzer existiert bereits
            } else {
                // Erstellt den Benutzer in der Datenbank
                boolean isCreated = userDB.createUser(user);
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