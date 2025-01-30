package org.CardGame.model;

import org.CardGame.model.Card;

import java.util.ArrayList;
import java.util.List;

public class Deck {
    private List<Card> deckCards;

    public Deck() {
        this.deckCards = new ArrayList<>();
    }

    public void addCard(Card card) {
        deckCards.add(card);
    }

    public void removeCard(Card card) {
        deckCards.remove(card);
    }

    public List<Card> getCards() {
        return deckCards;
    }

    public boolean isEmpty() {
        return deckCards.isEmpty();  // Delegiert an die `isEmpty()` Methode der ArrayList
    }

    // Gibt die Anzahl der Karten im Deck zurück
    public int size() {
        return deckCards.size();
    }


}
