package org.CardGame.server;

import org.CardGame.database.*;
import org.CardGame.model.HttpRequest;
import java.util.List;
import org.CardGame.model.Card;


public class UserShowDeckService {

    private DBAccess dbAccess; // Datenbankzugriff
    private DeckDB deckDB; // Kartenbezogene Datenbankabfragen
    private AuthDB authDB; // Authentifizierung
    private TokenValidator tokenValidator;

    // Konstruktor für Dependency Injection
    public UserShowDeckService(DBAccess dbAccess, AuthDB authDB, DeckDB deckDB) {
        this.dbAccess = dbAccess;
        this.authDB = authDB;
        this.deckDB = deckDB;
        this.tokenValidator = new TokenValidator(authDB);
    }

    public String getDeckCards(HttpRequest request) {
        String requestToken = request.getHeaders().get("Authorization"); // Token aus den Headern abrufen

        try {

            // Benutzername aus dem Token extrahieren und den Token validieren
            String username = tokenValidator.validate(requestToken);

            // Karten aus dem Deck des Benutzers abrufen
            List<Card> userDeck = deckDB.getUserDeck(username);

            // Überprüfen, ob das Deck leer ist
            if (userDeck == null || userDeck.isEmpty()) {
                return "{\"deck\": []}";  // Wenn kein Deck vorhanden, leere Liste zurückgeben
            }

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

    // Neue Methode für die Ausgabe im "Plain"-Format
    public String getDeckCardsPlain(HttpRequest request) {
        String requestToken = request.getHeaders().get("Authorization"); // Token aus den Headern abrufen

        try {
            // Benutzername aus dem Token extrahieren und den Token validieren
            String username = tokenValidator.validate(requestToken);

            // Karten aus dem Deck des Benutzers abrufen
            List<Card> userDeck = deckDB.getUserDeck(username);

            // Überprüfen, ob das Deck leer ist
            if (userDeck == null || userDeck.isEmpty()) {
                return "No cards in the deck.";  // Wenn kein Deck vorhanden, eine einfache Nachricht zurückgeben
            }

            // Karten im "plain"-Format zurückgeben
            StringBuilder response = new StringBuilder();
            for (Card card : userDeck) {
                response.append("Card: ")
                        .append(card.getName()).append("\n")
                        .append("  Type: ").append(card.getType()).append("\n")
                        .append("  Element: ").append(card.getElementType()).append("\n")
                        .append("  Damage: ").append(card.getDamage()).append("\n")
                        .append("----------------------------\n");
            }

            return response.toString();

        } catch (Exception e) {
            return "Internal Server Error: " + e.getMessage();  // Fehlernachricht im "Plain"-Format
        }

        }

    }

