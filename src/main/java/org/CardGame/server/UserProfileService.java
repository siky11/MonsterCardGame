package org.CardGame.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.CardGame.database.*;
import org.CardGame.model.HttpRequest;
import org.CardGame.model.User;


public class UserProfileService {

    private DBAccess dbAccess; // Datenbankzugriff
    private AuthDB authDB; // Authentifizierung
    private UserDB userDB;
    private TokenValidator tokenValidator;

    // Konstruktor für Dependency Injection
    public UserProfileService(DBAccess dbAccess, AuthDB authDB, UserDB userDB) {
        this.dbAccess = dbAccess;
        this.authDB = authDB;
        this.userDB = userDB;
        this.tokenValidator = new TokenValidator(authDB);
    }

    // Methode zum Abrufen des Benutzerprofils
    public String getUserProfile(HttpRequest request, String username) {
        String requestToken = request.getHeaders().get("Authorization"); // Token aus den Headern abrufen

        try {
            // Benutzername aus dem Token extrahieren und validieren
            String validatedUsername = tokenValidator.validate(requestToken);

            // Überprüfen, ob der angeforderte Benutzername mit dem durch das Token validierten übereinstimmt
            if (!validatedUsername.equals(username)) {
                return "{\"error\": \"Unauthorized\"}";  // Zugriff verweigert, wenn der Benutzername nicht übereinstimmt
            }

            // Benutzerdaten aus der Datenbank abrufen
            User user = userDB.getUserByUsername(username); // Annahme: Eine Methode zum Abrufen von User-Daten

            // Überprüfen, ob der Benutzer existiert
            if (user == null) {
                return "{\"error\": \"User not found\"}"; // Benutzer nicht gefunden
            }

            // StringBuilder für JSON-Antwort
            StringBuilder jsonResponse = new StringBuilder("{");

            // Benutzername, Elo, Coins und Spiele gespielt
            jsonResponse.append("\"username\": \"").append(user.getUsername()).append("\", ")
                    .append("\"bio\": ").append(user.getBio()).append(", ")
                    .append("\"image\": ").append(user.getImage()).append(", ");
            // JSON-Antwort abschließen
            jsonResponse.append("}");

            // Rückgabe der JSON-Antwort als String
            return jsonResponse.toString();

        } catch (Exception e) {
            return "{\"error\": \"Internal Server Error: " + e.getMessage() + "\"}";
        }
    }

    public String editProfile(HttpRequest request, String username) {
        String requestToken = request.getHeaders().get("Authorization");
        String requestBody = request.getBody();

        try {
            // Benutzername aus dem Token extrahieren und validieren
            String validatedUsername = tokenValidator.validate(requestToken);

            // Überprüfen, ob der angeforderte Benutzername mit dem durch das Token validierten übereinstimmt
            if (!validatedUsername.equals(username)) {
                return "{\"error\": \"Unauthorized\"}";  // Zugriff verweigert, wenn der Benutzername nicht übereinstimmt
            }

            // JSON-Body in ein UserProfile-Objekt umwandeln
            ObjectMapper objectMapper = new ObjectMapper();
            User user = objectMapper.readValue(requestBody, User.class);

            boolean success = userDB.updateUser(user);
            if (success) {
                return "{\"message\": \"Profile updated successfully.\"}";
            } else {
                return "{\"error\": \"Profile update failed.\"}";
            }
        } catch (Exception e) {
            return "{\"error\": \"Internal Server Error: " + e.getMessage() + "\"}";
        }

    }
}