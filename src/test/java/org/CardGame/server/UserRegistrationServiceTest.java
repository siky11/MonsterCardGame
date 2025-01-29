package org.CardGame.server;

import org.CardGame.database.DBAccessInterface;
import org.CardGame.database.UserDB;
import org.CardGame.database.DBAccess;
import org.CardGame.database.UserDBInterface;
import org.CardGame.model.HttpRequest;
import org.CardGame.model.HttpRequestInterface;
import org.CardGame.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.sql.SQLException;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class UserRegistrationServiceTest {

    @Mock
    private UserDBInterface userDB;

    @Mock
    private DBAccessInterface dbAccess;

    @Mock
    private HttpRequestInterface request;  // Wir mocken die Schnittstelle


    private UserRegistrationService userRegistrationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userRegistrationService = new UserRegistrationService(dbAccess, userDB);
    }

    @Test
    void testRegisterUser_Success() throws Exception {
        // Arrange
        String jsonBody = "{\"Username\": \"testuser\", \"Password\": \"securepass\"}";
        when(request.getBody()).thenReturn(jsonBody);
        when(userDB.userExists("testuser")).thenReturn(false);
        when(userDB.createUser(any(User.class))).thenReturn(true);

        // Act
        String result = userRegistrationService.registerUser(request);

        // Assert
        assertTrue(result.contains("User created successfully"), "User should be created successfully");
    }


    @Test
    void testRegisterUser_AlreadyExists() throws SQLException {
        // Arrange
        String jsonBody = "{\"Username\": \"existingUser\", \"Password\": \"password\"}";
        when(request.getBody()).thenReturn(jsonBody);
        when(userDB.userExists("existingUser")).thenReturn(true);

        // Act
        String result = userRegistrationService.registerUser(request);

        // Assert
        assertTrue(result.contains("User already exists"), "Existing user should not be created again");
    }

    @Test
    void testRegisterUser_InvalidJSON() throws SQLException {
        // Arrange
        String jsonBody = "invalid_json";
        when(request.getBody()).thenReturn(jsonBody);

        // Act
        String result = userRegistrationService.registerUser(request);

        // Assert
        assertTrue(result.contains("Invalid JSON format"), "Invalid JSON should return an error message");
    }

    @Test
    void testRegisterUser_DatabaseError() throws SQLException {
        // Arrange
        String jsonBody = "{\"Username\": \"newuser\", \"Password\": \"password\"}";
        when(request.getBody()).thenReturn(jsonBody);
        when(userDB.userExists("newuser")).thenReturn(false);
        when(userDB.createUser(any(User.class))).thenThrow(new SQLException("Database failure"));

        // Act
        String result = userRegistrationService.registerUser(request);

        // Assert
        assertTrue(result.contains("Database error"), "Database failure should return an error message");
    }
}
