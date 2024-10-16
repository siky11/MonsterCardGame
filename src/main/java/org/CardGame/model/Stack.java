package org.CardGame.model;

import org.CardGame.model.Card;

import java.util.ArrayList;
import java.util.List;

public class Stack {
    private List<Card> stackCards;

    public Stack() {
        this.stackCards = new ArrayList<>();
    }

    public void addCard(Card card) {
        stackCards.add(card);
    }

    public void removeCard(Card card) {
        stackCards.remove(card);
    }

    public List<Card> getCards() {
        return stackCards;
    }
}