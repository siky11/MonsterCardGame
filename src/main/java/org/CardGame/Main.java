package org.CardGame;

import org.CardGame.database.DBAccess;
import org.CardGame.server.HttpServer;

public class Main {
    public static void main(String[] args) {
        // Initialisiere DBAccess-Instanz
        DBAccess dbAccess = new DBAccess();

        // Erstelle eine HttpServer-Instanz
        HttpServer server = new HttpServer(dbAccess);

        // Starte den Server über die Instanz
        server.runServer();  // runServer ist jetzt eine Instanzmethode
    }
}