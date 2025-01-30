package org.CardGame.model;

import java.util.ArrayList;
import java.util.List;

public class Player {
    private String username; // Der Benutzername des Spielers
    private Deck deck; // Das Deck des Spielers
    private List<Card> wonCards; // Karten, die der Spieler im Verlauf des Spiels gewonnen hat
    private List<Card> lostCards;
    private int elo;

    // Konstruktor
    public Player(String username, Deck deck, int elo) {
        this.username = username;
        this.deck = deck;
        this.wonCards = new ArrayList<Card>();
        this.lostCards = new ArrayList<Card>();
        this.elo = elo;
    }

    // Getter für den Benutzernamen
    public String getUsername() {
        return username;
    }

    // Getter für das Deck des Spielers
    public Deck getDeck() {
        return deck;
    }

    // Methode, um eine zufällige Karte aus dem Deck zu ziehen
    public Card drawRandomCard() {
        if (deck.getCards().isEmpty()) {
            return null; // Keine Karten mehr im Deck
        }
        int randomIndex = (int) (Math.random() * deck.getCards().size());
        return deck.getCards().get(randomIndex); // Karte zurückgeben
    }

    // Methode, um eine Karte zu gewinnen (hinzuzufügen zum Deck oder gewonnenen Karten)
    public void winCard(Card card) {
        wonCards.add(card); // Füge gewonnene Karte zu den gewonnenen Karten des Spielers hinzu
        deck.addCard(card);
    }

    // Methode, um eine Karte zu gewinnen (hinzuzufügen zum Deck oder gewonnenen Karten)
    public void loseCard(Card card) {
        lostCards.add(card); // Füge gewonnene Karte zu den gewonnenen Karten des Spielers hinzu
        deck.removeCard(card);
    }

    // Getter für die gewonnenen Karten
    public List<Card> getWonCards() {
        return wonCards;
    }

    public int getElo() {
        return elo;
    }

    public void setElo(int elo) {
        this.elo = elo;
    }
}
