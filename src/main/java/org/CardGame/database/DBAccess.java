package org.CardGame.database;

import java.sql.*; // Für die Datenbankoperationen

public class DBAccess implements DBAccessInterface {
    public String url = "jdbc:postgresql://localhost:5432/game_database"; // URL deiner Datenbank
    public String user = "simon"; // Benutzername
    public String password = "password"; // Passwort

    // Methode zur Herstellung der Verbindung
    @Override
    public Connection connect() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

}