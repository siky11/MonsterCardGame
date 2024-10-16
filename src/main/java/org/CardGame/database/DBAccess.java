package org.CardGame.database;

import org.CardGame.model.User;

import com.fasterxml.jackson.databind.ObjectMapper; // Für die JSON-Serialisierung
import java.sql.*; // Für die Datenbankoperationen



public class DBAccess {
    private String url = "jdbc:postgresql://localhost:5432/game_database"; // URL deiner Datenbank
    private String user = "simon"; // Benutzername
    private String password = "password"; // Passwort

    // Methode zur Herstellung der Verbindung
    public Connection connect() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    // Methode zum Abrufen eines Benutzers anhand des Benutzernamens wird jetzt noch nicht gebraucht
    public String getUserByUsername(String username) {
        String query = "SELECT * FROM game_user WHERE username = ?";
        String jsonResult = null; // Variable für das Ergebnis im JSON-Format

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) { // Vorbereiten der SQL-Abfrage

            pstmt.setString(1, username); // Setzen des Benutzernamens als Parameter
            ResultSet rs = pstmt.executeQuery(); // Ausführen der Abfrage

            if(rs.next()){

                // Erstelle ein Benutzerobjekt
                User user = new User();
                user.setId(rs.getString("id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));

                // Serialisiere das Benutzerobjekt in JSON
                ObjectMapper objectMapper = new ObjectMapper();
                jsonResult = objectMapper.writeValueAsString(user);

            }else{
                jsonResult = "{\"message\": \"User not found\"}";
            }
        } catch (SQLException e) {
            jsonResult = "{\"error\": \"" + e.getMessage() + "\"}";
        }catch(Exception e){
            jsonResult = "{\"error\": \"" + e.getMessage() + "\"}";
        }
        return jsonResult;
    }

    // Methode zur Benutzeranmeldung
    public String loginUser(String username, String password) throws SQLException {
        String token = null; // Variable für das Token des Benutzers
        String query = "SELECT token FROM game_user WHERE username = ? AND password = ?"; // Annahme: Es gibt ein Token-Feld

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);  // Setzen des Benutzernamens
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery(); // Ausführen der Abfrage

            if (rs.next()) { // Wenn Anmeldedaten korrekt sind
                token = rs.getString("token"); // Abrufen des Tokens

                // Wenn bereits ein Token vorhanden ist, ist der Benutzer bereits eingeloggt
                if (token != null && !token.isEmpty()) {
                    return "{\"error\": \"User is already logged in\"}";
                }

                // Wenn kein Token vorhanden ist, generiere ein neues Token im Format username-mtcgToken
                token = username + "-mtcgToken"; // Token in der gewünschten Form
                saveTokenForUser(username, token); // Speichere das Token in der Datenbank

            } else {
                // Benutzername oder Passwort sind falsch
                return "{\"error\": \"Invalid username or password\"}";
            }
        }
        // Gib das neue Token zurück
        return "{\"token\": \"" + token + "\"}";
    }

    // Methode zum Speichern des Tokens für den Benutzer in der Datenbank
    public void saveTokenForUser(String username, String token) throws SQLException {
        String updateQuery = "UPDATE game_user SET token = ? WHERE username = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {

            pstmt.setString(1, token);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
        }
    }

    // Überprüfen, ob der Benutzer existiert
    public boolean userExists(String username) throws SQLException{
        String sql = "SELECT COUNT(*) FROM game_user WHERE username = ?";
        try (Connection conn = connect();
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

    // Methode zum Erstellen eines neuen Benutzers
    public boolean createUser(User user) throws  SQLException{
        String sql = "INSERT INTO game_user (username, password) VALUES (?, ?)";
        try (Connection conn = connect(); // Verbindung herstellen
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