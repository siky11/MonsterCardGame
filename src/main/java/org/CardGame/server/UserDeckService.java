package org.CardGame.server;

import org.CardGame.database.*;
import org.CardGame.model.HttpRequest;
import java.util.List;
import org.CardGame.model.Card;


public class UserDeckService {

    private DBAccess dbAccess; // Datenbankzugriff
    private CardDB cardDB; // Kartenbezogene Datenbankabfragen
    private AuthDB authDB; // Authentifizierung
    private TokenValidator tokenValidator;

    // Konstruktor für Dependency Injection
    public UserDeckService(DBAccess dbAccess, AuthDB authDB, CardDB cardDB) {
        this.dbAccess = dbAccess;
        this.authDB = authDB;
        this.cardDB = cardDB;
        this.tokenValidator = new TokenValidator(authDB);
    }

    public String getDeckCards(HttpRequest request) {
        String requestToken = request.getHeaders().get("Authorization"); // Token aus den Headern abrufen

        try {

            // Benutzername aus dem Token extrahieren und den Token validieren
            String username = tokenValidator.validate(requestToken);

            // Karten aus dem Deck des Benutzers abrufen
            List<Card> userDeck = cardDB.getUserDeck(username);


            // Karten in JSON umwandeln und als Antwort formatieren
            StringBuilder response = new StringBuilder();
            response.append("{\n  \"deck\": [\n");
            for (int i = 0; i < userDeck.size(); i++) {
                Card card = userDeck.get(i);
                response.append("    {\n")
                        .append("      \"id\": \"").append(card.getCard_id()).append("\",\n")
                        .append("      \"name\": \"").append(card.getName()).append("\",\n")
                        .append("      \"damage\": ").append(card.getDamage()).append(",\n")
                        .append("      \"elementType\": \"").append(card.getElementType()).append("\",\n")
                        .append("      \"type\": \"").append(card.getType()).append("\"\n")
                        .append("    }");
                if (i < userDeck.size() - 1) {
                    response.append(",\n\n");
                }
            }
            response.append("\n  ]\n}");
            return response.toString();
        } catch (Exception e) {
            return "{\"error\": \"Internal Server Error: " + e.getMessage() + "\"}";
        }
    }
}
