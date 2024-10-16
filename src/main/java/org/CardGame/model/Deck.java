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

    public List<Card> getCards() {
        return deckCards;
    }
}
