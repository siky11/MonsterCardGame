package org.CardGame.model;

import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;

public class Card {
    @JsonProperty("Id")
    private UUID card_id;
    @JsonProperty("Name")
    private String name;
    private CardType type; // "monster" oder "spell"
    @JsonProperty("Damage")
    private int damage;
    private ElementType elementType; // "fire", "water", oder "normal"

    // Konstruktor mit @JsonCreator, um JSON beim Deserialisieren zu verarbeiten
    @JsonCreator
    public Card(@JsonProperty("Id") UUID card_id,
                @JsonProperty("Name") String name,
                @JsonProperty("Damage") int damage ) {
        this.card_id = card_id;
        this.name = name;
        this.type = extractTypeFromName(name);  // Bestimmt den Kartentyp basierend auf dem Namen
        this.damage = damage;
        this.elementType = elementType != null ? elementType : extractElementFromName(name); // Falls der ElementType null ist, auf Name prüfen
    }

    // Getter-Methoden
    public UUID getCard_id() { return card_id; }

    public String getName() {
        return name;
    }

    public CardType getType() {
        return type;
    }

    public int getDamage() {
        return damage;
    }

    public ElementType getElementType() {
        return elementType;
    }

    public void setElementType(ElementType elementType) { this.elementType = elementType; }

    public void setType(CardType type) { this.type = type; }

    public void setCard_id(UUID card_id) { this.card_id = card_id; }

    public void setDamage(int damage) { this.damage = damage; }

    public void setName(String name) { this.name = name; }

    // Typ basierend auf dem Namen bestimmen
    public CardType extractTypeFromName(String name) {
        if (name.toLowerCase().contains("spell")) {
            return CardType.SPELL;
        } else {
            return CardType.MONSTER;
        }
    }

    // Element basierend auf dem Namen bestimmen
    public ElementType extractElementFromName(String name) {
        if (name.toLowerCase().contains("fire")) {
            return ElementType.FIRE;
        } else if (name.toLowerCase().contains("water")) {
            return ElementType.WATER;
        } else {
            return ElementType.NORMAL;
        }
    }

    @Override
    public String toString() {
        return "Card ID: " + card_id + ", Name: " + name + ", Type: " + type + ", Element: " + elementType + ", Damage: " + damage;
    }

}
