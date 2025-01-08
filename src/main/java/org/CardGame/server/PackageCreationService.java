package org.CardGame.server;

import org.CardGame.database.*;
import org.CardGame.model.Card;
import org.CardGame.model.HttpRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.SQLException;
import java.util.List;

public class PackageCreationService {

    private PackageCreationDBInterface packageCreationDB; // Spezielle DB-Klasse für die Paketverwaltung
    private AuthDBInterface authDB; // AuthDB für Authentifizierungsprüfungen

    // Konstruktor mit Dependency Injection für DB und ObjectMapper
    public PackageCreationService( AuthDBInterface authDB, PackageCreationDBInterface packageCreationDB) {
        this.packageCreationDB = packageCreationDB;
        this.authDB = authDB;
    }

    // Methode zum Erstellen eines Packages, überprüft den Token und verarbeitet Karten
    public String startPackageCreation(HttpRequest request) {
        String requestToken = request.getHeaders().get("Authorization");  // Token aus den Request-Headers holen

        //Entfernt das Bearer vor dem Token
        if (requestToken != null && requestToken.startsWith("Bearer ")) {
            requestToken = requestToken.substring(7); // "Bearer " entfernen
        }

        // Admin-Token dynamisch abrufen
        String adminToken = getAdminToken();

        // Überprüfe, ob der übergebene Token der des Admins ist
        if (requestToken == null || !adminToken.equals(requestToken)) {
            return "{\"error\": \"Unauthorized. Invalid or missing token.\"}";  // Token ungültig oder fehlt
        }

        // Erstelle das Paket und füge Karten hinzu
        return processPackage(request);
    }

    // Funktion zum Abrufen des Admin-Tokens
    private String getAdminToken() {
        try {
            return authDB.getTokenForUser("admin");  // Hol dir das Token für den Admin
        } catch (SQLException e) {
            return null; // Fehlerfall beim Abrufen des Admin-Tokens
        }
    }

    // Methode zum Erstellen des Packages und Hinzufügen der Karten
    private String processPackage(HttpRequest request)  {
        try {

            System.out.println(request.getBody());

            // Deserialisierung der Karten aus dem Request-Body
            ObjectMapper objectMapper = new ObjectMapper();
            List<Card> cards = objectMapper.readValue(request.getBody(), objectMapper.getTypeFactory().constructCollectionType(List.class, Card.class));

            System.out.println(request.getBody());

            // Überprüfen, ob Karten vorhanden sind
            if (cards == null || cards.isEmpty()) {
                return "{\"error\": \"No cards provided in the request body.\"}"; // Keine Karten im Body
            }

            // Erstelle das Paket und füge die Karten hinzu
            String jsonResult = packageCreationDB.createPackageAndAddCards(cards);

            return jsonResult;

        } catch (Exception e) {
            return "{\"error\": \"Failed to process package: " + e.getMessage() + "\"}";
        }
    }
}
