package org.CardGame.server;

import org.CardGame.database.*;
import org.CardGame.model.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class BattleService {
    private final DBAccess dbAccess;
    private final AuthDBInterface authDB;
    private final UserDB userDB;
    private TokenValidator tokenValidator;
    private BattleQueue battleQueue;
    private CardDB cardDB;
    private DeckDB deckDB;

    public BattleService(DBAccess dbAccess, AuthDBInterface authDB, UserDB userDB, CardDB cardDB, DeckDB deckDB) {
        this.dbAccess = dbAccess;
        this.authDB = authDB;
        this.userDB = userDB;
        this.cardDB = cardDB;
        this.deckDB = deckDB;
        this.tokenValidator = new TokenValidator(authDB, userDB);
        this.battleQueue = new BattleQueue();
    }

    public String startBattle(HttpRequestInterface request) {
        String requestToken = request.getHeaders().get("Authorization");  // Token aus den Request-Headers holen

        try {
            String username = authDB.extractUsernameFromToken(requestToken);
            // Verwende validate Methode, um Token und Benutzer zu überprüfen
            if (!tokenValidator.validate(requestToken, username)) {
                return "{\"error\": \"Unauthorized: Invalid token or user.\"}";
            }

            int elo = userDB.getEloByUsername(username);

            Player player = loadPlayer(username, elo);

            // Synchronisierung der BattleQueue
            synchronized(battleQueue.getQueue()) {
                // Überprüfen, ob die Queue leer ist (also nur ein Spieler vorhanden ist)
                if (battleQueue.getQueue().isEmpty()) {
                    // Füge den Spieler der Warteschlange hinzu
                    battleQueue.addToQueue(player);
                    // Response, dass der Spieler auf einen Gegner wartet
                    return "{\"message\": \"You are waiting for an opponent.\"}";
                } else {
                    // Ein Gegner wird aus der Queue entfernt und der Kampf gestartet
                    Player opponent = battleQueue.removeFromQueue();

                    return battleQueue.startBattle(player, opponent);
                }
            }
        } catch (IllegalArgumentException e) {
            // Fehler bei der Token-Überprüfung oder ungültiger Benutzername
            return "{\"error\": \"" + e.getMessage() + "\"}";
        } catch (IOException e) {
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }catch (SQLException e) {
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }


    public Player loadPlayer(String username, int elo) {
        try {
            // Hole das Deck des Spielers aus der DB
            List<Card> deck = deckDB.getUserDeck(username);

            // Erstelle das Deck-Objekt
            Deck playerDeck = new Deck();

            // Füge alle Karten aus der DB zum Deck hinzu
            for (Card card : deck) {
                playerDeck.addCard(card);  // Karten zum Deck des Spielers hinzufügen
            }

            // Erstelle das Player-Objekt mit dem Benutzernamen und dem Deck
            Player player = new Player(username, playerDeck, elo);

            // Gib den Spieler zurück
            return player;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;  // Falls ein Fehler auftritt, gib null zurück
        }
    }

}
