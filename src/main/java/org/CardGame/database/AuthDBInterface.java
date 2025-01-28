package org.CardGame.database;
import java.sql.*;
import java.io.IOException;
import java.util.UUID;

public interface AuthDBInterface {
    String loginUser(String username, String password) throws SQLException;
    void saveTokenForUser(String username, String token) throws SQLException;
    String getTokenForUser(String username) throws SQLException;
    String getTokenForId(UUID userId) throws SQLException;
    String extractUsernameFromToken(String requestToken) throws IOException;
    boolean isValidToken(String requestToken, UUID userid) throws SQLException;
}
