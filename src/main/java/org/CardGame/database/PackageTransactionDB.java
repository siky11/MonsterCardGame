package org.CardGame.database;

import org.CardGame.model.Card;
import org.CardGame.model.CardType;
import org.CardGame.model.ElementType;
import org.CardGame.model.Package;

import java.sql.*;
import java.util.List;
import java.util.UUID;
import java.util.ArrayList;

public class PackageTransactionDB {
    private DBAccess dbAccess = new DBAccess();

    // Benutzercoins abrufen
    public int getUserCoins(UUID userId) throws SQLException {

        String query = "SELECT coins FROM game_user WHERE id = ?";

        try (Connection conn = dbAccess.connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setObject(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("coins");
            } else {
                throw new SQLException("Benutzer nicht gefunden.");
            }
        }
    }

    // Coins des Benutzers aktualisieren
    public void updateUserCoins(UUID userId, int newCoinBalance) throws SQLException {
        String updateQuery = "UPDATE game_user SET coins = ? WHERE id = ?";
        try (Connection conn = dbAccess.connect();
             PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {

            pstmt.setInt(1, newCoinBalance);
            pstmt.setObject(2, userId);
            pstmt.executeUpdate();
        }catch (SQLException e) {
            throw new SQLException("Fehler beim Aktualisieren der Coins für Benutzer: " + userId);
        }
    }

    // Karten dem Stack des Benutzers hinzufügen
    public void addCardsToUserStack(UUID userId, List<Card> cards) throws SQLException {
        for (Card card : cards) {

            String insertQuery = "INSERT INTO user_stack (user_id, card_id) VALUES (?, ?)";

            try (Connection conn = dbAccess.connect();
                 PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {

                pstmt.setObject(1, userId);
                pstmt.setObject(2, card.getCard_id());
                pstmt.executeUpdate();

            }catch (SQLException e) {
                throw new SQLException("Fehler beim Hinzufügen von Karten zum Stack des Benutzers: " + userId);
            }
        }
    }

    // Transaktion speichern
    public void saveTransaction(UUID userId, UUID packageId) throws SQLException {

        String insertQuery = "INSERT INTO package_transactions (user_id, package_id) VALUES (?, ?)";

        try (Connection conn = dbAccess.connect();
             PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {

            pstmt.setObject(1, userId);
            pstmt.setObject(2, packageId);
            pstmt.executeUpdate();

        }catch (SQLException e) {
            throw new SQLException("Fehler beim Speichern der Transaktion für Benutzer: " + userId + " und Paket-ID: " + packageId);
        }
    }

    // Methode, die ein zufälliges Paket mit zugehörigen Karten aus der Datenbank abruft
    public Package getRandomPackageCards() throws SQLException {
        List<Card> cards = new ArrayList<>();

        // 1. Zufälliges Paket aus der Pakete-Tabelle auswählen
        String packageQuery = "SELECT package_id FROM game_package ORDER BY RANDOM() LIMIT 1";
        UUID packageId = null;

        try (Connection conn = dbAccess.connect();
             PreparedStatement pstmt = conn.prepareStatement(packageQuery);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                packageId = UUID.fromString(rs.getString("package_id"));
            }
        }

        if (packageId != null) {
            // 2. Alle Karten aus dem zufälligen Paket abfragen (Verbindung zur package_cards-Tabelle)
            String cardQuery = "SELECT gc.card_id, gc.name, gc.damage, gc.element_type, gc.type " +
                    "FROM game_card gc " +
                    "INNER JOIN package_cards pc ON gc.card_id = pc.card_id " +
                    "WHERE pc.package_id = ?";

            try (Connection conn = dbAccess.connect();
                 PreparedStatement pstmt = conn.prepareStatement(cardQuery)) {

                pstmt.setObject(1, packageId);
                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    UUID cardId = UUID.fromString(rs.getString("card_id"));
                    String cardName = rs.getString("name");
                    int cardDamage = rs.getInt("damage");
                    String elementType = rs.getString("element_type");
                    String typeStr = rs.getString("type");

                    // Karte erstellen und hinzufügen
                    Card card = new Card(cardId, cardName, cardDamage);
                    card.setType(CardType.valueOf(typeStr.toUpperCase()));  // Setzen des Typs (enum-Wert)
                    card.setElementType(ElementType.valueOf(elementType.toUpperCase()));  // Setzen des ElementTyps (enum-Wert)

                    cards.add(card);
                }
            }
        }

        return new Package(cards, packageId);  // Gibt die Liste der Karten des zufälligen Pakets zurück
    }
}
