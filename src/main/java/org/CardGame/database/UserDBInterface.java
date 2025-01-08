package org.CardGame.database;

import org.CardGame.model.User;

import java.sql.SQLException;
import java.util.UUID;

public interface UserDBInterface {
    /**
     * Überprüft, ob ein Benutzer mit dem angegebenen Benutzernamen existiert.
     *
     * @param username Der zu überprüfende Benutzername.
     * @return True, wenn der Benutzer existiert, sonst false.
     * @throws SQLException Wenn ein Fehler bei der Datenbankabfrage auftritt.
     */
    boolean userExists(String username) throws SQLException;

    /**
     * Erstellt einen neuen Benutzer in der Datenbank.
     *
     * @param user Das Benutzerobjekt, das erstellt werden soll.
     * @return True, wenn der Benutzer erfolgreich erstellt wurde, sonst false.
     * @throws SQLException Wenn ein Fehler bei der Datenbankabfrage auftritt.
     */
    boolean createUser(User user) throws SQLException;

    /**
     * Ruft die Benutzer-ID anhand des Benutzernamens ab.
     *
     * @param username Der Benutzername, für den die ID abgerufen werden soll.
     * @return Die UUID des Benutzers.
     * @throws SQLException Wenn ein Fehler bei der Datenbankabfrage auftritt oder der Benutzer nicht gefunden wird.
     */
    UUID getUserId(String username) throws SQLException;

    /**
     * Ruft einen Benutzer anhand seines Benutzernamens ab.
     *
     * @param username Der Benutzername des abzurufenden Benutzers.
     * @return Das Benutzerobjekt oder null, wenn der Benutzer nicht gefunden wird.
     */
    User getUserByUsername(String username);

    /**
     * Aktualisiert die Profildaten eines Benutzers.
     *
     * @param user Das Benutzerobjekt mit den zu aktualisierenden Profildaten.
     * @return True, wenn die Aktualisierung erfolgreich war, sonst false.
     */
    boolean updateUser(User user);
}
