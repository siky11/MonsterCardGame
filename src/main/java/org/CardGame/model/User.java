package org.CardGame.model;

public class User {
    private String username;
    private String password;
    private int elo;
    private int gamesPlayed;

    // Konstruktor mit Parametern
    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.elo = 1000; // Beispiel für einen Start-ELO-Wert
        this.gamesPlayed = 0;
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
