package org.CardGame.server;

import org.CardGame.database.AuthDBInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;

public class TokenValidatorTest {

    private TokenValidator tokenValidator;
    private AuthDBInterface mockAuthDB;

    @BeforeEach
    public void setUp() {
        // Mocking der AuthDBInterface
        mockAuthDB = mock(AuthDBInterface.class);
        tokenValidator = new TokenValidator(mockAuthDB);
    }

    @Test
    public void testValidToken() throws IOException {
        String validToken = "Bearer validToken123";
        String expectedUsername = "testuser";

        when(mockAuthDB.isValidToken("validToken123")).thenReturn(true);
        when(mockAuthDB.extractUsernameFromToken("validToken123")).thenReturn(expectedUsername);

        String username = tokenValidator.validate(validToken);
        assertEquals(expectedUsername, username, "Token validation should return the correct username.");
    }

    @Test
    public void testMissingBearerPrefix() {
        String invalidToken = "invalidToken123";

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            tokenValidator.validate(invalidToken);
        });

        assertEquals("Unauthorized: Missing or malformed token.", exception.getMessage(), "Token validation should throw the correct error message.");
    }

    @Test
    public void testInvalidToken() throws IOException {
        String invalidToken = "Bearer invalidToken123";

        // Token ist ungültig und gibt false zurück
        when(mockAuthDB.isValidToken("invalidToken123")).thenReturn(false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            tokenValidator.validate(invalidToken);
        });

        assertEquals("Unauthorized: Invalid token.", exception.getMessage(), "Token validation should throw an error for an invalid token.");
    }

    @Test
    public void testNonexistentUser() throws IOException {
        String tokenWithNonexistentUser = "Bearer validToken123";

        // Das Token existiert, aber der Benutzername ist null (nicht gefunden)
        when(mockAuthDB.isValidToken("validToken123")).thenReturn(true);
        when(mockAuthDB.extractUsernameFromToken("validToken123")).thenReturn(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            tokenValidator.validate(tokenWithNonexistentUser);
        });

        assertEquals("Unauthorized: User does not exist.", exception.getMessage(), "Token validation should throw an error if no user is associated with the token.");
    }

    @Test
    public void testIOExceptionWhileExtractingUsername() throws IOException {
        String malformedToken = "Bearer malformedToken";

        // Wenn die Extraktion des Benutzernamens aus dem Token eine IOException wirft
        when(mockAuthDB.isValidToken("malformedToken")).thenReturn(true);
        when(mockAuthDB.extractUsernameFromToken("malformedToken")).thenThrow(new IOException("Invalid token format"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            tokenValidator.validate(malformedToken);
        });

        assertEquals("Unauthorized: Unable to process token.", exception.getMessage(), "Token validation should throw an error on IOException.");
    }
}



