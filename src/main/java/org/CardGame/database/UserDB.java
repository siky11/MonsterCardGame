package org.CardGame.database;

import org.CardGame.model.User;

import java.sql.SQLException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UserDB implements UserDBInterface {

    private DBAccess dbAccess = new DBAccess();

    @Override
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

    @Override
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

    @Override
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

    @Override
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
                String name = rs.getString("name");

                // Benutzerobjekt mit Username und Password erstellen
                User user = new User(dbUsername, password);

                // Verwende Setter für die restlichen Felder
                user.setId(id);  // Setze die ID, wenn sie aus der DB kommt
                user.setElo(rs.getInt("elo"));
                user.setCoins(rs.getInt("coins"));
                user.setGamesPlayed(rs.getInt("games_played"));
                user.setImage(image);
                user.setBio(bio);
                user.setName(name);


                return user; // Benutzer zurückgeben
            } else {
                return null;  // Benutzer wurde nicht gefunden
            }
        } catch (Exception e) {
            return null;  // Fehler bei der Datenbankabfrage
        }
    }

    @Override
    public boolean updateUser(User user, String username) {
        String updateQuery = "UPDATE game_user SET name = ?, bio = ?, image = ? WHERE username = ?";

        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Der ursprüngliche Username darf nicht leer sein.");
        }

        // Logge die Werte vor dem Update, um sicherzustellen, dass die richtigen Daten übergeben werden
        System.out.println("Profil Update gestartet:");
        System.out.println("Profilname: " + user.getName());
        System.out.println("Bio: " + (user.getBio() != null ? user.getBio() : "Kein Bio gesetzt"));
        System.out.println("Image: " + (user.getImage() != null ? user.getImage() : "Kein Bild gesetzt"));


        try (Connection conn = dbAccess.connect();
             PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {

            // Setze die neuen Werte
            pstmt.setString(1, user.getName()); // Neuer Profilname
            pstmt.setString(2, user.getBio());     // Neue Bio
            pstmt.setString(3, user.getImage());   // Neues Bild
            pstmt.setString(4, username);   // Ursprünglicher Username

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0; // Erfolgreiches Update, wenn mindestens eine Zeile betroffen ist
        } catch (SQLException e) {
            System.err.println("Fehler beim Aktualisieren des Benutzers: " + e.getMessage());
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            System.err.println("Fehler beim Aktualisieren des Benutzers: " + e.getMessage());
            return false;
        }
    }

    // Funktion zum Abrufen aller Benutzer mit ELO-Werten und Rückgabe einer sortierten Liste
    public List<String[]> getAllUserEloSorted() throws SQLException {
        List<String[]> userEloList = new ArrayList<>();
        String query = "SELECT name, elo FROM game_user ORDER BY elo DESC";

        try (Connection conn = dbAccess.connect();  // Datenbankverbindung herstellen
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            ResultSet rs = pstmt.executeQuery();

            // Durchlaufe alle Resultate und speichere sie in einem String-Array (Username, ELO)
            while (rs.next()) {
                String username = rs.getString("name");
                int elo = rs.getInt("elo");

                // Füge das Paar (Username, ELO) zur Liste hinzu
                userEloList.add(new String[]{username, String.valueOf(elo)});
            }
        }

        return userEloList;  // Rückgabe der sortierten Liste der Benutzer und deren ELO-Werte
    }

    public int getEloByUsername(String username) throws SQLException {
        String query = "SELECT elo FROM game_user WHERE username = ?";

        try (Connection conn = dbAccess.connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("elo");
                } else {
                    throw new SQLException("User not found: " + username);
                }
            }
        }
    }


    public void updateEloByUsername(String username, int newElo) throws SQLException {
        String query = "UPDATE game_user SET elo = ?, games_played = games_played + 1 WHERE username = ?";

        try (Connection conn = dbAccess.connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, newElo);
            pstmt.setString(2, username);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("User not found: " + username);
            }
        }
    }
}
