package org.CardGame.server;

import org.CardGame.database.*;
import org.CardGame.model.HttpRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Map;
import java.util.HashMap;



import org.CardGame.database.PackageCreationDBInterface;


public class PackageCreationServiceTest {

    private PackageCreationService packageCreationService;
    private AuthDBInterface mockAuthDB;
    private PackageCreationDBInterface mockPackageCreationDB;

    @BeforeEach
    public void setUp() {
        // Mocks für Abhängigkeiten
        mockAuthDB = mock(AuthDBInterface.class);
        mockPackageCreationDB = mock(PackageCreationDBInterface.class);

        // Erstellen des PackageCreationService mit gemockten Dependencies
        packageCreationService = new PackageCreationService(mockAuthDB, mockPackageCreationDB);
    }

    @Test
    public void testStartPackageCreation_ValidToken_Success() throws Exception {
        // Arrange
        HttpRequest request = new HttpRequest();

        // Setzen des Headers mit dem Token
        String validToken = "Bearer validAdminToken123";
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", validToken);
        request.setHeaders(headers);

        String requestBody = "[{\"Id\": \"25b1201a-46d8-43e2-990e-d2ff363d04b0\", \"Name\": \"Card1\"}, {\"Id\": \"337f780f-6905-4ad1-b798-ff019a777b94\", \"Name\": \"Card2\"}]";
        request.setBody(requestBody);


        // Mocking der AuthDB: Das Token für "admin" muss mit dem übergebenen Token übereinstimmen
        when(mockAuthDB.getTokenForUser("admin")).thenReturn("validAdminToken123");

        // Mocking der Datenbankfunktion für die Paket-Erstellung
        when(mockPackageCreationDB.createPackageAndAddCards(anyList())).thenReturn("{\"status\": \"success\"}");

        // Act
        String response = packageCreationService.startPackageCreation(request);

        // Assert
        assertEquals("{\"status\": \"success\"}", response, "Package creation should succeed with valid token.");
    }

    @Test
    public void testStartPackageCreation_InvalidToken() throws Exception {
        // Arrange
        HttpRequest request = new HttpRequest();

        // Setzen des Headers mit einem ungültigen Token
        String invalidToken = "Bearer invalidToken123";
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", invalidToken);
        request.setHeaders(headers);

        // Der Admin-Token ist im System hinterlegt
        when(mockAuthDB.getTokenForUser("admin")).thenReturn("validAdminToken123");

        // Act
        String response = packageCreationService.startPackageCreation(request);

        // Assert
        assertEquals("{\"error\": \"Unauthorized. Invalid or missing token.\"}", response, "Package creation should fail with invalid or missing token.");
    }

    @Test
    public void testStartPackageCreation_EmptyCards() throws Exception {
        // Arrange
        HttpRequest request = new HttpRequest();

        // Gültiges Token und Body ohne Karten
        String validToken = "Bearer validAdminToken123";
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", validToken);
        request.setHeaders(headers);

        // Empty body, also keine Karten zum Erstellen
        String emptyBody = "[]";
        request.setBody(emptyBody);

        // Mocking der AuthDB: Token-Validierung
        when(mockAuthDB.getTokenForUser("admin")).thenReturn("validAdminToken123");

        // Act
        String response = packageCreationService.startPackageCreation(request);

        // Assert
        assertEquals("{\"error\": \"No cards provided in the request body.\"}", response, "Package creation should fail if no cards are provided.");
    }

    @Test
    public void testStartPackageCreation_UnexpectedError() throws Exception {
        // Arrange
        HttpRequest request = new HttpRequest();

        // Gültiges Token und Beispiel-Body
        String validToken = "Bearer validAdminToken123";
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", validToken);
        request.setHeaders(headers);

        // Fehlerhafte Body-Daten (Verarbeitet ungültige Daten)
        String invalidBody = "[{\"id\": \"invalid\", \"name\": \"Card1\"}]";
        request.setBody(invalidBody);

        // Mocking der AuthDB: Das Token für "admin"
        when(mockAuthDB.getTokenForUser("admin")).thenReturn("validAdminToken123");

        // Simuliere einen Fehler bei der Verarbeitung des Pakets
        when(mockPackageCreationDB.createPackageAndAddCards(anyList())).thenThrow(new RuntimeException("Database error"));

        // Act
        String response = packageCreationService.startPackageCreation(request);

        // Assert
        assertTrue(response.contains("Failed to process package"), "Package creation should fail with an error if the database throws an exception.");
    }

    @Test
    public void testStartPackageCreation_ValidRequest() throws Exception {
        // Arrange
        HttpRequest request = new HttpRequest();
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer admin-mtcgToken"); // Passender Admin-Token aus Beispiel
        headers.put("Content-Type", "application/json");        // Content-Type hinzugefügt
        request.setHeaders(headers);

        String requestBody = "["
                + "{\"Id\":\"845f0dc7-37d0-426e-994e-43fc3ac83c08\", \"Name\":\"WaterGoblin\", \"Damage\": 10.0},"
                + "{\"Id\":\"99f8f8dc-e25e-4a95-aa2c-782823f36e2a\", \"Name\":\"Dragon\", \"Damage\": 50.0},"
                + "{\"Id\":\"e85e3976-7c86-4d06-9a80-641c2019a79f\", \"Name\":\"WaterSpell\", \"Damage\": 20.0},"
                + "{\"Id\":\"1cb6ab86-bdb2-47e5-b6e4-68c5ab389334\", \"Name\":\"Ork\", \"Damage\": 45.0},"
                + "{\"Id\":\"dfdd758f-649c-40f9-ba3a-8657f4b3439f\", \"Name\":\"FireSpell\", \"Damage\": 25.0}"
                + "]";
        request.setBody(requestBody);

        // Mocking dependencies
        when(mockAuthDB.getTokenForUser("admin")).thenReturn("admin-mtcgToken");
        when(mockPackageCreationDB.createPackageAndAddCards(anyList())).thenReturn("{\"success\": \"Package created successfully.\"}");

        // Act
        String response = packageCreationService.startPackageCreation(request);

        // Assert
        assertTrue(response.contains("Package created successfully"), "Response should confirm successful package creation.");
        verify(mockPackageCreationDB, times(1)).createPackageAndAddCards(anyList());
    }

    @Test
    public void testStartPackageCreation_NullRequestBody() throws Exception {
        // Arrange
        HttpRequest request = new HttpRequest();
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer validAdminToken");
        request.setHeaders(headers);
        request.setBody(null); // Kein Body

        // Mocking dependencies
        when(mockAuthDB.getTokenForUser("admin")).thenReturn("validAdminToken");

        // Act
        String response = packageCreationService.startPackageCreation(request);

        // Assert
        assertTrue(response.contains("Failed to process package"), "Response should indicate failure to process the package.");
        verifyNoInteractions(mockPackageCreationDB); // DB-Interaktion sollte nicht passieren
    }


}
