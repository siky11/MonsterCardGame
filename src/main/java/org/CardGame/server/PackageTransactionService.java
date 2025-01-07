package org.CardGame.server;

import org.CardGame.database.UserDB;
import org.CardGame.database.PackageTransactionDB;
import org.CardGame.model.HttpRequest;
import org.CardGame.database.AuthDB;
import org.CardGame.database.DBAccess;
import org.CardGame.model.Package;

import java.util.UUID;

public class PackageTransactionService {

    private DBAccess dbAccess; // Verwaltung von Datenbankinteraktionen
    private PackageTransactionDB packageTransactionDB; // Spezielle DB-Klasse für Pakettransaktionen
    private AuthDB authDB; // AuthDB für Authentifizierungsprüfungen
    private UserDB userDB;

    // Konstruktor mit Dependency Injection für DB und andere Services
    public PackageTransactionService(DBAccess dbAccess, AuthDB authDB, PackageTransactionDB packageTransactionDB, UserDB userDB) {
        this.dbAccess = dbAccess;
        this.packageTransactionDB = packageTransactionDB;
        this.authDB = authDB;
        this.userDB = userDB;
    }

    // Methode zum Starten einer Pakettransaktion
    public String startPackageTransaction(HttpRequest request) {
        String requestToken = request.getHeaders().get("Authorization");  // Token aus den Request-Headers holen

        // Entfernt das Bearer vor dem Token
        if (requestToken != null && requestToken.startsWith("Bearer ")) {
            requestToken = requestToken.substring(7); // "Bearer " entfernen
        }

        // Überprüfe, ob der Token gültig ist
        if (!authDB.isValidToken(requestToken)) {
            return "{\"error\": \"Unauthorized. Invalid or missing token.\"}";  // Token ungültig oder fehlt
        }

        // Verarbeite die Transaktion
        return processTransaction(request, requestToken);
    }


    private String processTransaction(HttpRequest request, String requestToken) {
        try {
            // Extrahiere den Benutzernamen aus dem Token
            String username = authDB.extractUsernameFromToken(requestToken);

            // Hole die User-ID basierend auf dem Benutzernamen
            UUID userId = userDB.getUserId(username);

            // Hole die Anzahl der benötigten Coins für das Paket
            int packageCost = 5;

            // Hole den aktuellen Coin-Bestand des Benutzers
            int currentCoins = packageTransactionDB.getUserCoins(userId);

            if (currentCoins < packageCost) {
                return "{\"error\": \"Nicht genügend Coins für diese Transaktion.\"}"; // Fehlende Coins
            }

            // Ziehe Coins vom Benutzer ab
            packageTransactionDB.updateUserCoins(userId, currentCoins - packageCost);

            // Hole ein vorhandenes Paket aus der Datenbank (hier Beispiel, erste verfügbare Zeile)
            Package packageCards = packageTransactionDB.getRandomPackageCards(); // Beispielhafte Abfrage

            if (packageCards == null || packageCards.getCards().isEmpty()) {
                // Falls das Paket leer ist oder keine Karten enthält
                return "{\"error\": \"Keine verfügbaren Pakete gefunden.\"}"; // Fehler: Keine Pakete verfügbar
            }

            // Füge das Paket dem Benutzer zu
            packageTransactionDB.addCardsToUserStack(userId, packageCards.getCards());
            //speichert Transaktion
            packageTransactionDB.saveTransaction(userId, packageCards.getPackageId());

            return "{\"message\": \"Transaktion erfolgreich, Paket zugewiesen.\"}"; // Erfolgreiche Transaktion

        } catch (Exception e) {
            return "{\"error\": \"Fehler bei der Transaktion: " + e.getMessage() + "\"}";
        }
    }



}
