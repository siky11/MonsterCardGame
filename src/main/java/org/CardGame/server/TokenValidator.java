package org.CardGame.server;

import org.CardGame.database.AuthDB;
import org.CardGame.database.AuthDBInterface;
import org.CardGame.database.UserDB;
import org.CardGame.database.UserDBInterface;

import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;

public class TokenValidator {

    public AuthDBInterface authDB; // Instanz für Authentifizierungsdatenbank
    public UserDBInterface userDB;

    public TokenValidator(AuthDBInterface authDB, UserDBInterface userDB) {
        this.userDB = new UserDB();
        this.authDB = authDB;
    }


    public boolean validate(String requestToken, String username) {
        try {
            // Token prüfen und "Bearer " entfernen
            if (requestToken == null || !requestToken.startsWith("Bearer ")) {
                throw new IllegalArgumentException("Unauthorized: Missing or malformed token.");
            }

            String token = requestToken.substring(7); // Entfernt "Bearer "

            // Prüfen, ob ein Benutzer mit diesem Token existiert
            if (username == null) {
                throw new IllegalArgumentException("Unauthorized: User does not exist.");
            }

            UUID userID = userDB.getUserId(username);

            // Gültigkeit des Tokens prüfen
            if (!authDB.isValidToken(token, userID)) {
                throw new IllegalArgumentException("Unauthorized: Invalid token.");
            }

            // Rückgabe true, wenn alles in Ordnung ist
            return true;

        } catch (IllegalArgumentException e) {
            // Fehlerfall, gibt false zurück, wenn ein Argument ungültig ist
            return false;
        } catch (SQLException e) {
            // Fehlerfall bei der Datenbankabfrage
            return false;
        }
    }
}

