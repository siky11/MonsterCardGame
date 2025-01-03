package org.CardGame.server;

import org.CardGame.database.DBAccess;
import org.CardGame.database.AuthDB;
import org.CardGame.database.UserDB;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;


public class HttpServer {

    private DBAccess dbAccess;
    private AuthDB authDB;
    private UserDB userDB;
    private static final int PORT = 10001;

    // Konstruktordefinition: Hier wird DBAccess injiziert
    public HttpServer(DBAccess db, AuthDB authDB, UserDB userDB) {
        this.dbAccess = db;  // DBAccess wird über den Konstruktor übergeben
        this.authDB = authDB;       // AuthDB wird über den Konstruktor übergeben
        this.userDB = userDB;       // UserDB wird über den Konstruktor übergeben
    }

    // Run-Methode zum Starten des Servers
    public void runServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server läuft auf Port " + PORT);
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept(); // Akzeptiert Client-Verbindung
                    handleRequest(clientSocket);  // Anfrage behandeln
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

            HttpRequestHandler requestHandler = new HttpRequestHandler(dbAccess, authDB, userDB);
            requestHandler.handle(in, out, clientSocket);
        } catch (IOException e) {
            System.err.println("IO Exception: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Exception: " + e.getMessage());
        }
        }
    }

