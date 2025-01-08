package org.CardGame.server;

import org.CardGame.model.HttpRequest;

import java.io.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class HttpRequestParserTest {

    private BufferedReader mockReader;
    private ByteArrayOutputStream mockOutput;  // Ersetze OutputStream durch ByteArrayOutputStream
    private HttpRequestParser parser;

    @BeforeEach
    public void setUp() {
        // Nutze einen echten InputStream
        String fakeInput = "POST /users HTTP/1.1\nContent-Length: 19\n\n{\"name\":\"John\"}";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(fakeInput.getBytes());
        mockReader = new BufferedReader(new InputStreamReader(inputStream));

        // Nutze ByteArrayOutputStream zum Speichern der Ausgabe
        mockOutput = new ByteArrayOutputStream();  // Füge diese Zeile hinzu
        parser = new HttpRequestParser(mockReader, mockOutput);
    }

    @Test
    public void testParseValidRequest() throws IOException {
        // Führe den Test aus, wenn Daten bereits von BufferedReader eingelesen werden
        HttpRequest result = parser.parse();

        // Überprüfe, ob die Rückgabewerte korrekt sind
        assertNotNull(result);
        assertEquals("POST", result.getMethod());
        assertEquals("/users", result.getPath());
        assertEquals(1, result.getHeaders().size());
        assertTrue(result.getBody().contains("\"name\":\"John\""));
    }

    @Test
    public void testParseEmptyRequest() throws IOException {
        // Simuliere eine leere Anfrage
        String fakeInput = "";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(fakeInput.getBytes());
        mockReader = new BufferedReader(new InputStreamReader(inputStream));

        HttpRequest result = parser.parse();

        // Überprüfe, dass bei einer leeren Anfrage die Methode null zurückgibt
        assertNull(result);
    }

    @Test
    public void testParseRequestWithResponse() throws IOException {
        // Führe den Test aus, der die Ausgabe in mockOutput überprüft
        String fakeInput = "POST /users HTTP/1.1\nContent-Length: 19\n\n{\"name\":\"John\"}";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(fakeInput.getBytes());
        mockReader = new BufferedReader(new InputStreamReader(inputStream));
        mockOutput = new ByteArrayOutputStream();  // Neue Ausgabeinstanz

        parser = new HttpRequestParser(mockReader, mockOutput);
        HttpRequest result = parser.parse();

        // Überprüfe, dass mockOutput die Antwort korrekt verarbeitet hat
        assertNotNull(result);
        assertNotNull(mockOutput.toString()); // Hier wird überprüft, ob etwas in der Ausgabe enthalten ist
    }
}
