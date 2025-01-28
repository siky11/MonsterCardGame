package org.CardGame.server;

import org.CardGame.database.*;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import com. fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.CardGame.database.DBAccess;
import org.CardGame.database.DeckDB;
import org.CardGame.model.HttpRequest;

import java.io.IOException;


public class UserConfigureDeckService {

    public TokenValidator tokenValidator;
    private DeckDBInterface deckDB;
    private AuthDBInterface authDB;
    private UserDBInterface userDB;

    // Konstruktor, der TokenValidator und CardDB übergibt
    public UserConfigureDeckService(DeckDBInterface deckDB, AuthDBInterface authDB, UserDBInterface userDB) {
        this.authDB = authDB;
        this.userDB = userDB;
        this.tokenValidator = new TokenValidator(authDB, userDB);
        this.deckDB = deckDB;
    }

    public String configureDeck(HttpRequest request) {

        String requestToken = request.getHeaders().get("Authorization");

        try {
            String username = authDB.extractUsernameFromToken(requestToken);
            // Benutzernamen aus dem Token extrahieren

            if (!tokenValidator.validate(requestToken, username)) {
                return "{\"error\": \"Unauthorized: Invalid token or user.\"}";
            }

            // Karten-IDs aus dem JSON-Request parsen
            List<UUID> cardIds = parseCardIds(request);
            System.out.println("Parsed Card IDs: " + cardIds);

            // Überprüfen, ob Karten-IDs gültig sind
            if (cardIds.isEmpty()) {
                return "{\"error\": \"No cards provided to configure the deck.\"}";
            }

            UUID userID = userDB.getUserId(username);

            // Das Deck für den Benutzer konfigurieren (mit Rückgabewerten als JSON)
            String result = deckDB.setUserDeck( userID, cardIds, username);

            // Umwandlung und Rückgabe je nach Erfolgsstatus
            if (result.contains("error")) {
                return result;  // Fehler-Meldung aus setUserDeck (dort bei SQL-Fehler oder falschen Karten-IDs)
            } else {
                return "{\"message\": \"Deck configured successfully. Deck ID: " + result + "\"}";
            }
        } catch (SQLException e) {
            return "{\"error\": \"Internal Server Error: " + e.getMessage() + "\"}";
        }catch (IOException e){
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }

    private List<UUID> parseCardIds(HttpRequest request) {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            // Extrahieren des JSON-Strings aus dem Body des Requests
            String jsonRequestBody = request.getBody(); // Annahme, dass getBody() den Anfrageinhalt als String liefert

            // Umwandeln des JSON-Strings in eine List von UUIDs
            return objectMapper.readValue(jsonRequestBody, new TypeReference<List<UUID>>() {});
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid card IDs format.", e);
        }
    }
}
