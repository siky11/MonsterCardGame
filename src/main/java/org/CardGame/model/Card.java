package org.CardGame.model;

import java.util.ArrayList;
import java.util.List;

public class Card {
    private String name;
    private String type; // "monster" oder "spell"
    private int damage;
    private String elementType; // "fire", "water", oder "normal"

    public Card(String name, String type, int damage, String elementType) {
        this.name = name;
        this.type = type;
        this.damage = damage;
        this.elementType = elementType;
    }

    // Getter-Methoden
    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public int getDamage() {
        return damage;
    }

    public String getElementType() {
        return elementType;
    }
}
