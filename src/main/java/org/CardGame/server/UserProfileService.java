package org.CardGame.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.CardGame.database.*;
import org.CardGame.model.HttpRequest;
import org.CardGame.model.User;

import java.util.List;


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
        this.tokenValidator = new TokenValidator(authDB, userDB);
    }

    // Methode zum Abrufen des Benutzerprofils
    public String getUserProfile(HttpRequest request, String username) {
        String requestToken = request.getHeaders().get("Authorization"); // Token aus den Headern abrufen

        try {

            if (!tokenValidator.validate(requestToken, username)) {
                return "{\"error\": \"Unauthorized: Invalid token or user.\"}";
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
            jsonResponse.append("\"profilename\": \"").append(user.getName()).append("\", ")
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
            if (!tokenValidator.validate(requestToken, username)) {
                return "{\"error\": \"Unauthorized: Invalid token or user.\"}";
            }

            // JSON-Body in ein UserProfile-Objekt umwandeln
            ObjectMapper objectMapper = new ObjectMapper();
            User user = objectMapper.readValue(requestBody, User.class);

            boolean success = userDB.updateUser(user, username);
            if (success) {
                return "{\"message\": \"Profile updated successfully.\"}";
            } else {
                return "{\"error\": \"Profile update failed.\"}";
            }
        } catch (Exception e) {
            return "{\"error\": \"Internal Server Error: " + e.getMessage() + "\"}";
        }

    }

    public String getUserStats(HttpRequest request) {
        String requestToken = request.getHeaders().get("Authorization");

        try {

            String username = authDB.extractUsernameFromToken(requestToken);

            if (!tokenValidator.validate(requestToken, username)) {
                return "{\"error\": \"Unauthorized: Invalid token or user.\"}";
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
            jsonResponse.append("\"profilename\": \"").append(user.getName()).append("\", ")
                    .append("\"elo\": ").append(user.getElo()).append(", ")
                    .append("\"games_played\": ").append(user.getGamesPlayed()).append(", ");
            // JSON-Antwort abschließen
            jsonResponse.append("}");

            // Rückgabe der JSON-Antwort als String
            return jsonResponse.toString();

        }catch (Exception e) {
            return "{\"error\": \"Internal Server Error: " + e.getMessage() + "\"}";
        }
    }

    public String getScoreboard(HttpRequest request) {
        String requestToken = request.getHeaders().get("Authorization");

        try {

            String username = authDB.extractUsernameFromToken(requestToken);

            if (!tokenValidator.validate(requestToken, username)) {
                return "{\"error\": \"Unauthorized: Invalid token or user.\"}";
            }

            // Hole die ELO-Werte der Benutzer
            List<String[]> userEloList = userDB.getAllUserEloSorted();

            // StringBuilder für den JSON-Response
            StringBuilder jsonResponse = new StringBuilder();

            jsonResponse.append("{\n");  // Start des JSON-Objekts
            jsonResponse.append("  \"scoreboard\": [\n"); // Start des scoreboard-Arrays

            // Durchlaufe die Userliste und füge die Profile hinzu
            for (int i = 0; i < userEloList.size(); i++) {
                String[] user = userEloList.get(i);
                String profileName = user[0];  // Benutzername
                String elo = user[1];  // ELO-Wert

                // Benutzerprofil als JSON-Objekt formatieren
                jsonResponse.append("    {\n");
                jsonResponse.append("      \"profilename\": \"").append(profileName).append("\",\n");
                jsonResponse.append("      \"elo\": ").append(elo).append("\n");
                jsonResponse.append("    }");

                // Komma nach jedem Profil, außer nach dem letzten
                if (i < userEloList.size() - 1) {
                    jsonResponse.append(",");
                }

                jsonResponse.append("\n");
            }

            jsonResponse.append("  ]\n");  // Ende des scoreboard-Arrays
            jsonResponse.append("}");  // Ende des JSON-Objekts

            return jsonResponse.toString();  // Rückgabe der formatierten JSON-Antwort

        } catch (Exception e) {
            return "{\"error\": \"Internal Server Error: " + e.getMessage() + "\"}";
        }
    }
}