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

    public User getUserByUsername(String username) {
        String sql = "SELECT * FROM game_user WHERE username = ?";

        try (Connection conn = dbAccess.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username); // Benutzernamen in die Abfrage einfügen
            ResultSet rs = stmt.executeQuery();

            // Prüfen, ob der Benutzer gefunden wurde
            if (rs.next()) {
                String id = rs.getString("id");
                String dbUsername = rs.getString("username");
                String password = rs.getString("password");
                String bio = rs.getString("bio");
                String image = rs.getString("image");

                // Benutzerobjekt mit Username und Password erstellen
                User user = new User(dbUsername, password);

                // Verwende Setter für die restlichen Felder
                user.setId(id);  // Setze die ID, wenn sie aus der DB kommt
                user.setElo(rs.getInt("elo"));
                user.setCoins(rs.getInt("coins"));
                user.setGamesPlayed(rs.getInt("games_played"));
                user.setImage(image);
                user.setBio(bio);


                return user; // Benutzer zurückgeben
            } else {
                return null;  // Benutzer wurde nicht gefunden
            }
        } catch (Exception e) {
            return null;  // Fehler bei der Datenbankabfrage
        }
    }
}
