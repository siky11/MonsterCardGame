package org.CardGame.server;

import org.CardGame.database.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;

public class TokenValidatorTest {

    @Mock
    private UserDBInterface userDB;

    @Mock
    private AuthDBInterface authDB;

    @Mock
    private DBAccessInterface dbAccess;

    private TokenValidator tokenValidator;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        tokenValidator = new TokenValidator(authDB, userDB);
    }

    @Test
    void testStartPackageTransaction_InvalidToken() throws SQLException {
        // Arrange
        String invalidToken = "Bearer invalidToken";
        String username = "testuser";
        UUID userId = UUID.randomUUID();

        // Mocking
        when(userDB.getUserId(username)).thenReturn(userId);
        when(authDB.isValidToken("invalidToken", userId)).thenReturn(false);

        // Act
        boolean isValid = tokenValidator.validate(invalidToken, username);

        // Assert
        assertFalse(isValid, "The token should be invalid for an incorrect token.");
    }

    @Test
    void testValidate_NullToken() {
        // Arrange
        String nullToken = null;
        String username = "testuser";

        // Act
        boolean isValid = tokenValidator.validate(nullToken, username);

        // Assert
        assertFalse(isValid, "The token should be invalid when it's null.");
    }


    @Test
    void testValidate_UserNotFound() throws SQLException {
        // Arrange
        String validToken = "Bearer validToken123";
        String username = "nonexistentuser";

        // Mocking
        when(userDB.getUserId(username)).thenThrow(new SQLException("User not found"));

        // Act
        boolean isValid = tokenValidator.validate(validToken, username);

        // Assert
        assertFalse(isValid, "The validation should fail if the user does not exist.");
    }

    @Test
    void testGetTokenForId_UserNotFound() throws SQLException {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(authDB.getTokenForId(userId)).thenReturn(null);

        // Act
        String token = authDB.getTokenForId(userId);

        // Assert
        assertNull(token, "Token should be null if the user is not found.");
    }

    @Test
    void testIsValidToken_MismatchedToken() throws SQLException {
        // Arrange
        String requestToken = "wrongToken";
        UUID userId = UUID.randomUUID();
        String databaseToken = "validToken123";

        // Mocking
        when(authDB.getTokenForId(userId)).thenReturn(databaseToken);

        // Act
        boolean isValid = authDB.isValidToken(requestToken, userId);

        // Assert
        assertFalse(isValid, "The token should be invalid if it does not match the database value.");
    }
}



