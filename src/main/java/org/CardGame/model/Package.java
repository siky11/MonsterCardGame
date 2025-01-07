package org.CardGame.model;

import org.CardGame.model.Card;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Package {
    private List<Card> packageCards;
    private UUID packageId;

    public Package(List<Card> packageCards, UUID packageId) {
        this.packageId = packageId;
        this.packageCards = packageCards;
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

    public UUID getPackageId() { return packageId; }

    // Getter, um die Anzahl der Karten zu erhalten
    public int getCardCount() { return packageCards.size(); }
}
