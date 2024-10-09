package org.CardGame.database;

import org.CardGame.model.User;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class DBAccess {
    private String url = "jdbc:postgresql://localhost:5432/game_database"; // URL deiner Datenbank
    private String user = "simon"; // Benutzername
    private String password = "password"; // Passwort

    // Methode zur Herstellung der Verbindung
    public Connection connect() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    // Beispielmethode zum Abrufen eines Benutzers
    public String getUserByUsername(String username) {
        String query = "SELECT * FROM game_user WHERE username = ?";
        String jsonResult = null;

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

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

    public String loginUser(String username, String password) throws SQLException {
        String token = null;
        String query = "SELECT token FROM users WHERE username = ? AND password = ?"; // Adjust according to your schema

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                token = rs.getString("token"); // Assuming you have a token field in your users table
            }
        }
        return token;
    }

    // Weitere Methoden für INSERT, UPDATE, DELETE, etc. hinzufügen
}