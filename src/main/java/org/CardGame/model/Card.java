package org.CardGame.model;

import java.util.List;
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

    // Berechnet den Schaden dieser Karte gegen eine andere Karte
    public double calculateDamage(Card otherCard, List<String> battlelog) {
        double baseDamage = this.damage;

        // Beispiel: Schaden hängt vom Elementtyp ab
        if ((this.elementType == ElementType.FIRE && otherCard.getElementType() == ElementType.WATER) && (this.type == CardType.SPELL)) {
            battlelog.add("- Fire is weak against Water. Damage( " + baseDamage + " ) of **" + this.name + "** got worse\n");
            return baseDamage * 0.5; // Feuer ist schwach gegen Wasser
        } else if ((this.elementType == ElementType.WATER && otherCard.getElementType() == ElementType.FIRE) && (this.type == CardType.SPELL)) {
            battlelog.add("- Water is strong against Fire. Damage( " + baseDamage + " ) of **" + this.name + "** got better\n");
            return baseDamage * 1.5; // Wasser ist stark gegen Feuer
        } else if ((this.elementType == ElementType.NORMAL && otherCard.getElementType() == ElementType.WATER) && (this.type == CardType.SPELL)) {
            battlelog.add("- Normal is strong against Water. Damage( " + baseDamage + " ) of **" + this.name + "** got better\n");
            return baseDamage * 1.5; // Normal ist stark gegen Wasser
        } else if ((this.elementType == ElementType.WATER && otherCard.getElementType() == ElementType.NORMAL) && (this.type == CardType.SPELL)) {
            battlelog.add("- Water is weak against Normal. Damage( " + baseDamage + " ) of **" + this.name + "** got worse\n");
            return baseDamage * 0.5; // Wasser ist schwach gegen Normal
        } else if ((this.elementType == ElementType.NORMAL && otherCard.getElementType() == ElementType.FIRE) && (this.type == CardType.SPELL)) {
            battlelog.add("- Normal is weak against Fire. Damage( " + baseDamage + " ) of **" + this.name + "** got worse\n");
            return baseDamage * 0.5; // Normal ist schwach gegen Feuer
        } else if ((this.elementType == ElementType.FIRE && otherCard.getElementType() == ElementType.NORMAL) && (this.type == CardType.SPELL)) {
            battlelog.add("- Fire is strong against Normal. Damage( " + baseDamage + " ) of **" + this.name + "** got better\n");
            return baseDamage * 1.5; // Feuer ist stark gegen Normal
        } else if (this.type == CardType.MONSTER && otherCard.getType() == CardType.MONSTER) {
            battlelog.add("- Both cards are MONSTER type. No elemental advantage. Base damage remains. \n");
            return baseDamage; // Wenn beides Monster Karten sind, bleibt der Schaden gleich
        } else if ((this.type == CardType.MONSTER && otherCard.getType() == CardType.SPELL) && (this.elementType == ElementType.NORMAL && otherCard.getElementType() == ElementType.NORMAL)) {
            battlelog.add("- MONSTER (normal) vs SPELL (normal). Special rule: No damage dealt. Final damage: 0.\n");
            return baseDamage * 0; // Kein Schaden durch Spezialregel
        }

        // Standardregel: kein Vorteil oder Nachteil, wenn keine Spezialregeln greifen
        battlelog.add("- No special rule found. Damage remains the same. \n");
        return baseDamage;
    }



    @Override
    public String toString() {
        return "Card ID: " + card_id + ", Name: " + name + ", Type: " + type + ", Element: " + elementType + ", Damage: " + damage;
    }

}
