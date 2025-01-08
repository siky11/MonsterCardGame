package org.CardGame.database;

import org.CardGame.model.Card;
import org.CardGame.model.CardType;
import org.CardGame.model.ElementType;

import java.sql.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DeckDB {

    private DBAccess dbAccess = new DBAccess();


    public List<Card> getUserDeck(String username) throws SQLException {
        List<Card> deck = new ArrayList<>();

        // SQL-Abfrage, um Karten aus dem Deck des Benutzers basierend auf dem Benutzernamen abzurufen
        String query = "SELECT gc.card_id, gc.name, gc.damage, gc.element_type, gc.type " +
                "FROM game_card gc " +
                "INNER JOIN user_deck ud ON gc.card_id = ud.card_id " +
                "INNER JOIN game_user gu ON ud.user_id = gu.id " +
                "WHERE gu.username = ?";

        try (Connection conn = dbAccess.connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);  // Bind the username to the query
            ResultSet rs = pstmt.executeQuery();

            // Iterate through the results and create Card objects
            while (rs.next()) {
                UUID cardId = UUID.fromString(rs.getString("card_id"));
                String cardName = rs.getString("name");
                int cardDamage = rs.getInt("damage");
                String elementType = rs.getString("element_type");
                String typeStr = rs.getString("type");

                // Create card and add it to the deck
                Card card = new Card(cardId, cardName, cardDamage);
                card.setType(CardType.valueOf(typeStr.toUpperCase()));  // Set card type (enum value)
                card.setElementType(ElementType.valueOf(elementType.toUpperCase()));  // Set element type (enum value)

                deck.add(card);
            }
        }

        return deck;  // Return the list of cards in the user's deck
    }

    public String setUserDeck(UUID userID, List<UUID> cardIds, String username) throws SQLException {
        final int MAX_CARDS_ALLOWED = 4; // Maximale Anzahl an Karten im Deck
        final String addCardQuery = "INSERT INTO user_deck (user_id, card_id) VALUES (?, ?)";
        String jsonResult;
        CardDB cardDB = new CardDB();

        try (Connection conn = dbAccess.connect()) {

            // 1. Überprüfe, ob genau 4 Karten übergeben wurden
            if (cardIds.size() != MAX_CARDS_ALLOWED) {
                return "{\"error\": \"Request must contain exactly " + MAX_CARDS_ALLOWED + " cards.\"}";
            }

            /*

            List<Card> cards = cardDB.getUserStack(username);
            for (Card card : cards) {
                // Ausgabe jeder Karte
                System.out.println(card);  // Die `toString()`-Methode der Card wird aufgerufen
            }


            // 2. Prüfe, ob alle Karten im UserStack existieren
            for (UUID cardId : cardIds) {
                if (!isCardInUserStack(userID, cardId, conn)) {
                    return "{\"error\": \"Card ID " + cardId + " is not in the user's stack and cannot be added to the deck.\"}";
                }
            }

             */
            // 3. Lösche das bestehende Deck, falls eines vorhanden ist
            clearUserDeck(userID, conn);

            // 4. Füge die neuen Karten zum Deck hinzu
            for (UUID cardId : cardIds) {
                try (PreparedStatement addCardPstmt = conn.prepareStatement(addCardQuery)) {
                    addCardPstmt.setObject(1, userID);
                    addCardPstmt.setObject(2, cardId);
                    int rowsAffected = addCardPstmt.executeUpdate();
                    if (rowsAffected != 1) {
                        return "{\"error\": \"Failed to add card " + cardId + ".\"}";
                    }
                } catch (SQLException e) {
                    return "{\"error\": \"Error while inserting card " + cardId + ": " + e.getMessage() + "\"}";
                }
            }

            jsonResult = "{\"success\": \"All cards added to the deck successfully.\"}";

        } catch (SQLException e) {
            jsonResult = "{\"error\": \"Database error: " + e.getMessage() + "\"}";
        }

        return jsonResult;
    }


    private void clearUserDeck(UUID userID, Connection conn) throws SQLException {
        final String deleteDeckQuery = "DELETE FROM user_deck WHERE user_id = ?";

        try (PreparedStatement deletePstmt = conn.prepareStatement(deleteDeckQuery)) {
            deletePstmt.setObject(1, userID);
            deletePstmt.executeUpdate();
        }
    }

    /*
    private boolean isCardInUserStack(UUID userID, UUID cardId, Connection conn) throws SQLException {
        String checkCardQuery = "SELECT EXISTS (SELECT 1 FROM user_stack WHERE user_id = ? AND card_id = ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(checkCardQuery)) {
            pstmt.setObject(1, userID);
            pstmt.setObject(2, cardId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getBoolean(1); // Gibt true zurück, wenn eine Karte gefunden wurde
            }
        } catch (SQLException e) {
            // Fehler protokollieren
            System.err.println("Fehler beim Überprüfen, ob die Karte im Stack ist: " + e.getMessage());
            throw new SQLException("Error while checking if card is in user stack: " + e.getMessage(), e);
        }
        return false; // Karte nicht im Stack, falls keine Zeilen im ResultSet vorhanden sind
    }
    */
}
