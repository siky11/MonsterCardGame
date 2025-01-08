package org.CardGame.database;

import org.CardGame.model.Card;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public interface PackageCreationDBInterface {
    // Methode, um ein neues Paket zu erstellen und Karten hinzuzufügen
    String createPackageAndAddCards(List<Card> cards);

    // Methode, um eine Karte zu einem Paket hinzuzufügen
    void addCardToPackage(UUID packageId, UUID cardId, Connection conn) throws SQLException;
}
