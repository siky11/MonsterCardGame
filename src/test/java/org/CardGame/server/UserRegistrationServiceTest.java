package org.CardGame.server;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;  // Statischer Import

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class UserRegistrationServiceTest {

    private static final String BASE_URL = "http://localhost:10001/users";

    @Test
    public void testCreateUserSuccessfully() throws IOException {
        String payload = "{\"Username\":\"test\", \"Password\":\"user\"}";

        URL url = new URL(BASE_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        connection.getOutputStream().write(payload.getBytes());

        int responseCode = connection.getResponseCode();

        assertEquals(201, responseCode, "Expected HTTP 201 status for successful registration");
    }

    @Test
    public void testCreateUserFailure() throws IOException {
        String invalidPayload = "{\"Username\":\"test\", \"Password\":\"user\"}";

        URL url = new URL(BASE_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        connection.getOutputStream().write(invalidPayload.getBytes());

        int responseCode = connection.getResponseCode();

        assertEquals(400, responseCode, "Expected HTTP 400 status for invalid registration request");
    }
}
