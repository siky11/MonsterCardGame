package org.CardGame;

import org.CardGame.database.*;
import org.CardGame.server.HttpServer;
import org.CardGame.server.PackageTransactionService;

public class Main {
    public static void main(String[] args) {
        // Initialisiere DBAccess-Instanz
        DBAccess dbAccess = new DBAccess();
        AuthDB authDB = new AuthDB();
        UserDB userDB = new UserDB();
        PackageCreationDB packageCreationDB = new PackageCreationDB();
        PackageTransactionDB packageTransactionDB = new PackageTransactionDB();

        // Erstelle eine HttpServer-Instanz
        HttpServer server = new HttpServer(dbAccess, authDB, userDB, packageCreationDB, packageTransactionDB);

        // Starte den Server über die Instanz
        server.runServer();  // runServer ist jetzt eine Instanzmethode
    }
}