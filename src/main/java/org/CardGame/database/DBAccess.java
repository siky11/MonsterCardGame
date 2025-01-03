package org.CardGame.database;

import java.sql.*; // Für die Datenbankoperationen

public class DBAccess {
    private String url = "jdbc:postgresql://localhost:5432/game_database"; // URL deiner Datenbank
    private String user = "simon"; // Benutzername
    private String password = "password"; // Passwort

    // Methode zur Herstellung der Verbindung
    public Connection connect() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}