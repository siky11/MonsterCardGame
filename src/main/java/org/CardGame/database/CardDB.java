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

}
