package org.CardGame.database;

import org.CardGame.model.User;

import java.sql.SQLException;
import java.sql.*;
import java.util.UUID;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UserDB {

    private DBAccess dbAccess = new DBAccess();

    public boolean userExists(String username) throws SQLException{
        String sql = "SELECT COUNT(*) FROM game_user WHERE username = ?";
        try (Connection conn = dbAccess.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0; // Gibt true zurück, wenn der Benutzer existiert
            }
        } catch (SQLException e) {
            System.err.println("Fehler beim Überprüfen des Benutzers: " + e.getMessage());
        }
        return false; // Standardmäßig false, wenn ein Fehler auftritt
    }

    public boolean createUser(User user) throws SQLException {
        String sql = "INSERT INTO game_user (username, password, elo, games_played, coins) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dbAccess.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setInt(3, user.getElo());
            pstmt.setInt(4, user.getGamesPlayed());
            pstmt.setInt(5, user.getCoins());

            pstmt.executeUpdate(); // Ausführen der Einfüge-Anweisung
            return true; // Benutzer erfolgreich erstellt
        } catch (SQLException e) {
            return false; // Fehler beim Erstellen des Benutzers
        }
    }

    public UUID getUserId(String username) throws SQLException {
        String query = "SELECT id FROM game_user WHERE username = ?";
        try (Connection conn = dbAccess.connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return UUID.fromString(rs.getString("id"));
            } else {
                throw new SQLException("Benutzer nicht gefunden: " + username);
            }
        }
    }
}
