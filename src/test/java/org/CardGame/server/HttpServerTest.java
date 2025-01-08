package org.CardGame.server;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.*;

public class HttpServerTest {

    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 10001;

    @Test
    public void testMultipleRequests() throws Exception {
        int numberOfRequests = 10;

        // Gleichzeitig 10 Requests senden
        for (int i = 0; i < numberOfRequests; i++) {
            final int requestId = i;
            new Thread(() -> {
                try {
                    URL url = new URL("http://" + SERVER_ADDRESS + ":" + SERVER_PORT + "/");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    // Antwort vom Server erhalten
                    int responseCode = connection.getResponseCode();
                    assertEquals(200, responseCode); // Sicherstellen, dass die Antwort 200 (OK) ist

                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    System.out.println("Request " + requestId + " Response: " + response.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                    fail("Request " + requestId + " failed");
                }
            }).start();
        }

        // Warten, bis alle Threads beendet sind, um den Test korrekt auszuführen
        Thread.sleep(2000);
    }
}
