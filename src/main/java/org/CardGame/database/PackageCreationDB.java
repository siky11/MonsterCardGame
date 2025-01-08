package org.CardGame.database;

import org.CardGame.model.Card;

import java.sql.*;
import java.util.List;
import java.util.UUID;

public class PackageCreationDB implements PackageCreationDBInterface {
    private DBAccess dbAccess = new DBAccess();
    private CardDB cardDb = new CardDB();  // Zugriffsobjekt auf CardDB, um createCardIfNotExists zu nutzen

    // Paket erstellen und Karten hinzufügen
    @Override
    public String createPackageAndAddCards(List<Card> cards) {
        String query = "INSERT INTO game_package (package_id) VALUES (DEFAULT) RETURNING package_id";
        String jsonResult = null;

        try (Connection conn = dbAccess.connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            // Paket erstellen und die package_id abrufen
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                UUID packageId = rs.getObject("package_id", UUID.class);

                // Karten zu dem Paket hinzufügen oder in DB speichern, falls nicht vorhanden
                for (Card card : cards) {
                    UUID cardId = cardDb.createCardIfNotExists(card, conn); // Prüfen und speichern, falls nicht vorhanden
                    addCardToPackage(packageId, cardId, conn);  // Karte zum Paket hinzufügen
                }

                jsonResult = "{\"package_id\": \"" + packageId.toString() + "\", \"message\": \"Package created and cards added successfully\"}";
            }
        } catch (SQLException e) {
            jsonResult = "{\"error\": \"" + e.getMessage() + "\"}";
        }
        return jsonResult;
    }

    // Karte zu einem Paket hinzufügen
    @Override
    public void addCardToPackage(UUID packageId, UUID cardId, Connection conn) throws SQLException {
        String query = "INSERT INTO package_cards (package_id, card_id) VALUES (?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setObject(1, packageId);
            pstmt.setObject(2, cardId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            // Hier wird die Ausnahme weitergegeben, falls etwas schiefgeht
            throw e;  // Nur wenn etwas schiefgeht wird die Exception geworfen.
        }
    }

}
