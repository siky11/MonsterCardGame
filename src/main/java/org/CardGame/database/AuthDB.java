package org.CardGame.database;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.*;

public class AuthDB implements AuthDBInterface {

    private DBAccess dbAccess = new DBAccess();

    // Methode zur Benutzeranmeldung
    @Override
    public String loginUser(String username, String password) throws SQLException {
        String token = null;
        String query = "SELECT token FROM game_user WHERE username = ? AND password = ?";

        try (Connection conn = dbAccess.connect();
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
    @Override
    public void saveTokenForUser(String username, String token) throws SQLException {
        String updateQuery = "UPDATE game_user SET token = ? WHERE username = ?";

        try (Connection conn = dbAccess.connect();
             PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {

            pstmt.setString(1, token);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
        }
    }

    // Neue Methode, die das Token für einen Benutzer zurückgibt
    @Override
    public String getTokenForUser(String username) throws SQLException {
        String token = null;
        String query = "SELECT token FROM game_user WHERE username = ?";

        try (Connection conn = dbAccess.connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                token = rs.getString("token");
            }
        }
        return token;  // Gibt das Token zurück (oder null, wenn der Benutzer nicht gefunden wird)
    }

    @Override
    public String extractUsernameFromToken(String requestToken) throws IOException {
        try {
            // Token format: username-mtcgToken, split by "-"
            String[] tokenParts = requestToken.split("-");

            // Wenn das Token nicht richtig formatiert ist, werfe eine Ausnahme
            if (tokenParts.length != 2) {
                throw new IOException("Ungültiges Token-Format. Erforderlich: username-mtcgToken");
            }

            // Der Benutzername befindet sich am Anfang des Tokens
            String username = tokenParts[0];

            return username; // Benutzername zurückgeben

        } catch (IOException e) {
            // Fehler beim Extrahieren des Benutzernamens
            throw new IOException("Fehler beim Extrahieren des Benutzernamens aus dem Token", e);
        }
    }

    // Überprüfen, ob der Token für den spezifischen Benutzer gültig ist
    @Override
    public boolean isValidToken(String requestToken) {
        try {
            // Extrahiert den Benutzernamen aus dem Token (dies könnte anders aussehen, je nachdem, wie dein Token aufgebaut ist)
            String username = extractUsernameFromToken(requestToken);

            // Holt das Token für diesen spezifischen Benutzer aus der DB
            String validToken = getTokenForUser(username);

            // Vergleicht den übergebenen Token mit dem aus der DB gespeicherten Token des Benutzers
            return requestToken != null && requestToken.equals(validToken);

        } catch (SQLException e) {
            return false;
        }catch (IOException e) {
            // Fehler beim Extrahieren des Benutzernamens aus dem Token
            return false;
        }
    }
}
