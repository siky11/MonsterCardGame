package org.CardGame.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class User {
    private String id; // Neue ID für den Benutzer
    @JsonProperty("Username")
    private String username;
    @JsonProperty("Password")
    private String password;
    private int elo;
    private int gamesPlayed;


    // Standardkonstruktor
    public User() {
        // Standardwerte oder keine Initialisierung
    }

    // Konstruktor mit Parametern
    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.elo = 1000; // Beispiel für einen Start-ELO-Wert
        this.gamesPlayed = 0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // Getter- und Setter-Methoden
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getElo() {
        return elo;
    }

    public void setElo(int elo) {
        this.elo = elo;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public void setGamesPlayed(int gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }
}
