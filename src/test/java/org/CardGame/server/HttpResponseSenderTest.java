package org.CardGame.server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class HttpResponseSenderTest {

    private HttpResponseSender responseSender;
    private ByteArrayOutputStream mockOutput;

    @BeforeEach
    public void setUp() {
        // Initialisiere den ResponseSender und ByteArrayOutputStream
        responseSender = new HttpResponseSender();
        mockOutput = new ByteArrayOutputStream();
    }

    @Test
    public void testSendValidResponse() throws IOException {
        // Vorbereitungen: Entsprechend der Methode wird Content-Type und Content-Length gesetzt
        String body = "{\"message\":\"Success\"}";
        int statusCode = 200;

        // Rufe die send-Methode auf
        responseSender.send(mockOutput, body, statusCode);

        // Verifiziere, dass die Antwort korrekt gesendet wurde
        String expectedResponse = "HTTP/1.1 200 Success\r\nContent-Length: 21\r\nContent-Type: application/json\r\n\r\n{\"message\":\"Success\"}";
        String actualResponse = mockOutput.toString();

        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    public void testSendResponseForStatus204() throws IOException {
        String body = "";
        int statusCode = 204; // No Content

        responseSender.send(mockOutput, body, statusCode);

        // Angepasste erwartete Antwort mit "Content-Length" vor "Content-Type"
        String expectedResponse = "HTTP/1.1 204 No Content\r\nContent-Length: 0\r\nContent-Type: application/json\r\n\r\n";
        String actualResponse = mockOutput.toString();

        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    public void testSendResponseWithCustomStatusCode() throws IOException {
        // Test für einen nicht standardmäßigen Statuscode
        String body = "{\"error\":\"Unauthorized\"}";
        int statusCode = 401; // Unauthorized

        responseSender.send(mockOutput, body, statusCode);

        // Verifiziere die Ausgabe
        String expectedResponse = "HTTP/1.1 401 Unauthorized\r\nContent-Length: 24\r\nContent-Type: application/json\r\n\r\n{\"error\":\"Unauthorized\"}";
        String actualResponse = mockOutput.toString();

        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    public void testGetStatusMessage() {
        // Test der Methode zur Status-Nachricht
        assertEquals("OK", responseSender.getStatusMessage(201));
        assertEquals("Bad Request", responseSender.getStatusMessage(400));
        assertEquals("Unauthorized", responseSender.getStatusMessage(401));
        assertEquals("Not Found", responseSender.getStatusMessage(404));
        assertEquals("Conflict", responseSender.getStatusMessage(409));
        assertEquals("Internal Server Error", responseSender.getStatusMessage(500)); // Default-Fall
    }

    @Test
    public void testSendResponseWithDifferentStatus() throws IOException {
        // Test für 409 Conflict
        String body = "{\"message\":\"Conflict occurred\"}";
        int statusCode = 409;

        responseSender.send(mockOutput, body, statusCode);

        String expectedResponse = "HTTP/1.1 409 Conflict\r\nContent-Length: 31\r\nContent-Type: application/json\r\n\r\n{\"message\":\"Conflict occurred\"}";
        String actualResponse = mockOutput.toString();

        assertEquals(expectedResponse, actualResponse);
    }
}
