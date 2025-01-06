package org.CardGame;

import org.CardGame.database.AuthDB;
import org.CardGame.database.DBAccess;
import org.CardGame.database.PackageDB;
import org.CardGame.database.UserDB;
import org.CardGame.server.HttpServer;

public class Main {
    public static void main(String[] args) {
        // Initialisiere DBAccess-Instanz
        DBAccess dbAccess = new DBAccess();
        AuthDB authDB = new AuthDB();
        UserDB userDB = new UserDB();
        PackageDB packageDB = new PackageDB();

        // Erstelle eine HttpServer-Instanz
        HttpServer server = new HttpServer(dbAccess, authDB, userDB, packageDB);

        // Starte den Server über die Instanz
        server.runServer();  // runServer ist jetzt eine Instanzmethode
    }
}