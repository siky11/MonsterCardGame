package org.CardGame.database;

import org.CardGame.model.User;

import java.sql.SQLException;
import java.sql.*;
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
        String sql = "INSERT INTO game_user (username, password) VALUES (?, ?)";
        try (Connection conn = dbAccess.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.executeUpdate(); // Ausführen der Einfüge-Anweisung
            return true; // Benutzer erfolgreich erstellt
        } catch (SQLException e) {
            return false; // Fehler beim Erstellen des Benutzers
        }
    }
}
