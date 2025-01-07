package org.CardGame.server;

import org.CardGame.database.AuthDB;
import java.io.IOException;

public class TokenValidator {

    private AuthDB authDB; // Instanz für Authentifizierungsdatenbank

    public TokenValidator(AuthDB authDB) {
        this.authDB = authDB;
    }


    public String validate(String requestToken) throws IllegalArgumentException {
        try {
            // Token prüfen und "Bearer " entfernen
            if (requestToken == null || !requestToken.startsWith("Bearer ")) {
                throw new IllegalArgumentException("Unauthorized: Missing or malformed token.");
            }

            String token = requestToken.substring(7); // Entfernt "Bearer "

            // Gültigkeit des Tokens prüfen
            if (!authDB.isValidToken(token)) {
                throw new IllegalArgumentException("Unauthorized: Invalid token.");
            }

            // Benutzernamen aus dem Token extrahieren
            String username = authDB.extractUsernameFromToken(token);

            // Prüfen, ob ein Benutzer mit diesem Token existiert
            if (username == null) {
                throw new IllegalArgumentException("Unauthorized: User does not exist.");
            }

            // Geben den Benutzernamen zurück
            return username;

        } catch (IOException e) {
            throw new IllegalArgumentException("Unauthorized: Unable to process token.", e);
        }
    }
}

