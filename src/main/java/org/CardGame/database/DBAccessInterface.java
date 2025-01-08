package org.CardGame.database;

import java.sql.Connection;
import java.sql.SQLException;

public interface DBAccessInterface {

    // Methode zum Herstellen der Verbindung zur Datenbank
    Connection connect() throws SQLException;
}
