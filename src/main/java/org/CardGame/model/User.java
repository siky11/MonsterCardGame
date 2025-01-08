package org.CardGame.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;

public class User {
    private String id; // Neue ID für den Benutzer
    @JsonProperty("Username")
    private String username;
    @JsonProperty("Password")
    private String password;
    private int elo;
    private int gamesPlayed;
    private int coins;
    private String bio;
    private String image;

    // Konstruktor mit Parametern, bei dem alle Felder übergeben werden
    @JsonCreator
    public User(@JsonProperty("Username") String username,
                @JsonProperty("Password") String password ) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.elo = 1000; // Verwende 1000 als Startwert, falls ELO 0 oder negativ ist
        this.gamesPlayed = 0; // Spiele nur auf 0 oder größer setzen
        this.coins = 20;
        this.bio = bio;
        this.image = image;
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

    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) { this.coins = coins; }

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

    public String getBio() { return bio; }

    public void setBio(String bio) { this.bio = bio; }

    public String getImage() { return image; }

    public void setImage(String image) { this.image = image; }
}
