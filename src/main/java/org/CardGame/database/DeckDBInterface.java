package org.CardGame.database;

import org.CardGame.model.Card;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public interface DeckDBInterface {

    //Retrieves the deck of a specific user by their username.
    List<Card> getUserDeck(String username) throws SQLException;

    //Sets the deck for a specific user with a given list of card IDs.
    String setUserDeck(UUID userID, List<UUID> cardIds, String username) throws SQLException;

    void clearUserDeck(UUID userID, Connection conn) throws SQLException;
}
