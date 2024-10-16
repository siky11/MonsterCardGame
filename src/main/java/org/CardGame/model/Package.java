package org.CardGame.model;

import org.CardGame.model.Card;

import java.util.ArrayList;
import java.util.List;

public class Package {
    private List<Card> packageCards;

    public Package() {
        this.packageCards = new ArrayList<>();
    }

    public void addCard(Card card) {
        if (packageCards.size() < 5) {
            packageCards.add(card);
        } else {
            System.out.println("Ein Paket kann nur bis zu 5 Karten enthalten.");
        }
    }

    public List<Card> getCards() {
        return packageCards;
    }}
