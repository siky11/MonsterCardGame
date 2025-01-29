package org.CardGame.server;

import org.CardGame.database.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class HttpServer {

    private DBAccess dbAccess;
    private AuthDBInterface authDB;
    private UserDB userDB;
    private CardDB cardDB;
    private DeckDB deckDB;
    private PackageCreationDB packageCreationDB;
    private PackageTransactionDB packageTransactionDB;
    private static int PORT = 10001;
    private ExecutorService threadPool; // ExecutorService für Multithreading

    // Konstruktordefinition: Hier wird DBAccess injiziert
    public HttpServer(DBAccess db, AuthDBInterface authDB, UserDB userDB, PackageCreationDB packageCreationDB, PackageTransactionDB packageTransactionDB, CardDB cardDB, DeckDB deckDB, int threadPoolSize) {
        this.dbAccess = db;  // DBAccess wird über den Konstruktor übergeben
        this.authDB = authDB;       // AuthDB wird über den Konstruktor übergeben
        this.userDB = userDB;
        this.cardDB = cardDB;
        this.deckDB = deckDB;
        this.packageCreationDB = packageCreationDB;
        this.packageTransactionDB = packageTransactionDB;
        // Erstellt einen ThreadPool mit fester Größe
        this.threadPool = Executors.newFixedThreadPool(threadPoolSize);
    }

    // Run-Methode zum Starten des Servers
    public void runServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server läuft auf Port " + PORT);
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept(); // Akzeptiert Client-Verbindung
                    // Einen neuen Thread im Pool ausführen, der die Client-Anfrage bearbeitet
                    threadPool.execute(() -> handleRequest(clientSocket));  // Anfrage behandeln
                } catch (IOException e) {
                    System.err.println("Fehler beim Verarbeiten der Anfrage: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    private void handleRequest(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             OutputStream out = clientSocket.getOutputStream()) {

            HttpRequestHandler requestHandler = new HttpRequestHandler(dbAccess, authDB, userDB, packageCreationDB, packageTransactionDB, cardDB, deckDB);
            requestHandler.handle(in, out, clientSocket);
        } catch (IOException e) {
            System.err.println("IO Exception: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Exception: " + e.getMessage());
        } finally {
            try{
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }

    // Shutdown-Methode für den Server falls irgendwann benötigt
    public void stopServer() {
        threadPool.shutdown(); // Stoppt den ThreadPool
        System.out.println("Server wurde gestoppt.");
    }
}



