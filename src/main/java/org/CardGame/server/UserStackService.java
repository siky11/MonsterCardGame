package org.CardGame.server;

import org.CardGame.database.*;
import org.CardGame.model.HttpRequest;
import java.util.List;
import org.CardGame.model.Card;

public class UserStackService {

    private DBAccess dbAccess; // Verwaltung von Datenbankinteraktionen
    private CardDB cardDB; // Spezielle DB-Klasse für Pakettransaktionen
    private AuthDB authDB; // AuthDB für Authentifizierungsprüfungen

    // Konstruktor mit Dependency Injection für DB und andere Services
    public UserStackService(DBAccess dbAccess, AuthDB authDB, CardDB cardDB) {
        this.dbAccess = dbAccess;
        this.authDB = authDB;
        this.cardDB = cardDB;
    }

    public String getStackCards(HttpRequest request) {
        String requestToken = request.getHeaders().get("Authorization");  // Token aus den Request-Headers holen

        // Entfernt das Bearer vor dem Token
        if (requestToken != null && requestToken.startsWith("Bearer ")) {
            requestToken = requestToken.substring(7); // "Bearer " entfernen
        }else if(requestToken == null){
            return "{\"error\": \"Unauthorized.\"}";
        }

        // Überprüfe, ob der Token gültig ist
        if (!authDB.isValidToken(requestToken)) {
            return "{\"error\": \"Unauthorized. Invalid token.\"}";  // Token ungültig oder fehlt
        }

        try {
            // Extrahiere den Benutzernamen aus dem Token
            String username = authDB.extractUsernameFromToken(requestToken);

            // Holen der Benutzerkarten
            List<Card> userCards = cardDB.getCardsByUsername(username);

            if (userCards == null || userCards.isEmpty()) {
                return "{\"error\": \"No cards found for user.\"}";
            }

            // Karten-Daten in schönem JSON-Format zurückgeben
            StringBuilder response = new StringBuilder();
            response.append("{\n  \"cards\": [\n");
            for (int i = 0; i < userCards.size(); i++) {
                Card card = userCards.get(i);
                response.append("    {\n")
                        .append("      \"id\": \"").append(card.getCard_id()).append("\",\n")
                        .append("      \"name\": \"").append(card.getName()).append("\",\n")
                        .append("      \"damage\": ").append(card.getDamage()).append(",\n")
                        .append("      \"elementType\": \"").append(card.getElementType()).append("\",\n")
                        .append("      \"type\": \"").append(card.getType()).append("\"\n")
                        .append("    }");
                if (i < userCards.size() - 1) {
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


