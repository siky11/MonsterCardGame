package org.CardGame.model;

import org.CardGame.model.Card;

import java.util.ArrayList;
import java.util.List;

public class Package {
    private List<Card> packageCards;

    public Package() {
        this.packageCards = new ArrayList<>();
    }

    public boolean addCard(Card card) {
        if (packageCards.size() < 5) {
            packageCards.add(card);
            return true;
        } else {
            System.out.println("Ein Paket kann nur bis zu 5 Karten enthalten.");
            return false;
        }
    }

    public List<Card> getCards() {
        return packageCards;
    }

    // Getter, um die Anzahl der Karten zu erhalten
    public int getCardCount() { return packageCards.size(); }
}
