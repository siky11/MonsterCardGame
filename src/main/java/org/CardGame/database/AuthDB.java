package org.CardGame.database;

import java.sql.SQLException;
import java.sql.*;

public class AuthDB {

    private DBAccess dbAccess = new DBAccess();
    private UserDB userDB = new UserDB();

    // Methode zur Benutzeranmeldung
    public String loginUser(String username, String password) throws SQLException {
        String token = null;
        String query = "SELECT token FROM game_user WHERE username = ? AND password = ?";

        try (Connection conn = new DBAccess().connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery(); // Ausführen der Abfrage

            if (rs.next()) {
                token = rs.getString("token");

                // Wenn bereits ein Token vorhanden ist, ist der Benutzer bereits eingeloggt
                if (token != null && !token.isEmpty()) {
                    return "{\"error\": \"User is already logged in\"}";
                }

                // Generiere ein neues Token im Format username-mtcgToken
                token = username + "-mtcgToken";
                saveTokenForUser(username, token); // Speichert das Token in der Datenbank
            } else {
                return "{\"error\": \"Invalid username or password\"}";
            }
        }
        return "{\"token\": \"" + token + "\"}";
    }

    // Methode zum Speichern des Tokens für den Benutzer in der Datenbank
    public void saveTokenForUser(String username, String token) throws SQLException {
        String updateQuery = "UPDATE game_user SET token = ? WHERE username = ?";

        try (Connection conn = dbAccess.connect();
             PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {

            pstmt.setString(1, token);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
        }
    }
}
