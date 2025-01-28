package org.CardGame.server;

import org.CardGame.database.UserDB;
import org.CardGame.database.PackageTransactionDB;
import org.CardGame.model.HttpRequest;
import org.CardGame.database.AuthDB;
import org.CardGame.database.DBAccess;
import org.CardGame.model.Package;

import java.io.IOException;
import java.sql.SQLException;

import java.util.UUID;

public class PackageTransactionService {

    private DBAccess dbAccess; // Verwaltung von Datenbankinteraktionen
    private PackageTransactionDB packageTransactionDB; // Spezielle DB-Klasse für Pakettransaktionen
    private AuthDB authDB; // AuthDB für Authentifizierungsprüfungen
    private UserDB userDB;
    private TokenValidator tokenValidator;

    // Konstruktor mit Dependency Injection für DB und andere Services
    public PackageTransactionService(DBAccess dbAccess, AuthDB authDB, PackageTransactionDB packageTransactionDB, UserDB userDB) {
        this.dbAccess = dbAccess;
        this.packageTransactionDB = packageTransactionDB;
        this.authDB = authDB;
        this.userDB = userDB;
        this.tokenValidator = new TokenValidator(authDB, userDB);
    }

    public String startPackageTransaction(HttpRequest request) {
        String requestToken = request.getHeaders().get("Authorization");  // Token aus den Request-Headers holen

        try {
            String username = authDB.extractUsernameFromToken(requestToken);
            // Verwende validate Methode, um Token und Benutzer zu überprüfen
            if (!tokenValidator.validate(requestToken, username)) {
                return "{\"error\": \"Unauthorized: Invalid token or user.\"}";
            }


        }catch (IllegalArgumentException e) {
            // Fehler bei der Tokenüberprüfung oder ungültiger Benutzername
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }catch (IOException e){
            return "{\"error\": \"" + e.getMessage() + "\"}";
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
            System.out.println("Aktuelle Coins: " + currentCoins);

            if (currentCoins < packageCost) {
                System.out.println("Aktuelle Coins: " + currentCoins + ", Paketkosten: " + packageCost);
                return "{\"error\": \"Nicht genügend Coins für diese Transaktion.\"}"; // Fehlende Coins
            }

            // Hole ein vorhandenes Paket aus der Datenbank (hier Beispiel, erste verfügbare Zeile)
            Package packageCards = packageTransactionDB.getPackageCards(); // Beispielhafte Abfrage

            if (packageCards == null || packageCards.getCards().isEmpty()) {
                // Falls das Paket leer ist oder keine Karten enthält
                return "{\"error\": \"Keine verfügbaren Pakete gefunden.\"}"; // Fehler: Keine Pakete verfügbar
            }

            // Ziehe Coins vom Benutzer ab
            packageTransactionDB.updateUserCoins(userId, currentCoins - packageCost);

            int updatedCoins = packageTransactionDB.getUserCoins(userId);
            System.out.println("Benutzer nach der Transaktion hat " + updatedCoins + " Coins.");

            // Füge das Paket dem Benutzer zu
            packageTransactionDB.addCardsToUserStack(userId, packageCards.getCards());
            //speichert Transaktion
            packageTransactionDB.saveTransaction(userId, packageCards.getPackageId());
            //löscht das Package das aquired wurde
            packageTransactionDB.deletePackage(packageCards.getPackageId());

            return "{\"message\": \"Transaktion erfolgreich, Paket zugewiesen.\"}"; // Erfolgreiche Transaktion

        } catch (Exception e) {
            return "{\"error\": \"Fehler bei der Transaktion: " + e.getMessage() + "\"}";
        }
    }



}
