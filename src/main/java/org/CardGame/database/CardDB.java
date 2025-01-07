package org.CardGame.database;

import org.CardGame.model.Card;
import org.CardGame.model.CardType;
import org.CardGame.model.ElementType;

import java.sql.*;
import java.util.*;

public class CardDB {
    private DBAccess dbAccess = new DBAccess();


    // Karte erstellen, wenn sie noch nicht existiert
    public UUID createCardIfNotExists(Card card, Connection conn) throws SQLException {
        // Überprüfen, ob die Karte bereits existiert
        String checkCardQuery = "SELECT card_id FROM game_card WHERE card_id = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkCardQuery)) {
            checkStmt.setObject(1, card.getCard_id()); // Setze die card_id der zu prüfenden Karte
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    // Karte existiert bereits, zurückgegebene card_id verwenden
                    return rs.getObject("card_id", UUID.class);
                } else {
                    // Karte existiert noch nicht, also hinzufügen
                    String insertCardQuery = "INSERT INTO game_card (card_id, name, type, element_type, damage) VALUES (?, ?, ?, ?, ?)";
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertCardQuery)) {
                        insertStmt.setObject(1, card.getCard_id());
                        insertStmt.setString(2, card.getName());
                        insertStmt.setString(3, card.getType().name()); // Enum als String speichern
                        insertStmt.setString(4, card.getElementType().name()); // Enum als String speichern
                        insertStmt.setInt(5, card.getDamage());
                        insertStmt.executeUpdate(); // Führe das Insert aus
                    }
                    return card.getCard_id(); // Karte existiert jetzt, daher zurückgeben
                }
            }
        }
    }

    public List<Card> getCardsByUsername(String username) throws SQLException {
        List<Card> cards = new ArrayList<>();

        // SQL-Abfrage, um Karten aus dem Stack des Benutzers basierend auf dem Benutzernamen abzurufen
        String query = "SELECT gc.card_id, gc.name, gc.damage, gc.element_type, gc.type " +
                "FROM game_card gc " +
                "INNER JOIN user_stack us ON gc.card_id = us.card_id " +
                "INNER JOIN game_user gu ON us.user_id = gu.id " +
                "WHERE gu.username = ?";

        try (Connection conn = dbAccess.connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);  // Username binden
            ResultSet rs = pstmt.executeQuery();

            // Ergebnisse iterieren und Card-Objekte erstellen
            while (rs.next()) {
                UUID cardId = UUID.fromString(rs.getString("card_id"));
                String cardName = rs.getString("name");
                int cardDamage = rs.getInt("damage");
                String elementType = rs.getString("element_type");
                String typeStr = rs.getString("type");

                // Karte erstellen und zur Liste hinzufügen
                Card card = new Card(cardId, cardName, cardDamage);
                card.setType(CardType.valueOf(typeStr.toUpperCase()));  // Setzen des Typs (enum-Wert)
                card.setElementType(ElementType.valueOf(elementType.toUpperCase()));  // Setzen des ElementTyps (enum-Wert)

                cards.add(card);
            }
        }

        return cards;  // Liste der Karten des Benutzers zurückgeben
    }

}
