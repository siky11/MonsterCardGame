package org.CardGame.model;

import org.CardGame.database.CardDB;
import org.CardGame.database.DeckDB;
import org.CardGame.database.UserDB;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BattleQueue {

    private static final Queue<Player> queue = new ConcurrentLinkedQueue<>();  //Warteschlange für die Spieler die ein Battle eingehen wollen
    private List<String> battleLog = new ArrayList<>();
    private int MAX_ROUNDS = 100;
    private DeckDB deckDB;
    private CardDB cardDB;
    private UserDB userDB = new UserDB();


    public Queue<Player> getQueue() {
        return queue;
    }

    public void addToQueue(Player player) {
        System.out.println("[BattleQueue] Adding player to queue: " + player.getUsername());
        queue.add(player);
        System.out.println("[BattleQueue] Queue size after adding: " + queue.size());
    }

    public Player removeFromQueue() {
        if (!queue.isEmpty()) {
            Player opponent = queue.poll();
            System.out.println("[BattleQueue] Removed player from queue: " + (opponent != null ? opponent.getUsername() : "NULL"));
            return opponent;
        }
        System.out.println("[BattleQueue] Queue was empty, no opponent available.");
        return null;
    }


    public String startBattle(Player player1, Player player2) {
        battleLog.add("[Battle] " + player1.getUsername() + " is waiting for an opponent... \n");

        if (player2 == null) {
            battleLog.add("[Battle] No opponent found, returning wait message. \n");
            return "{\"message\": \"You are waiting for an opponent.\"}";
        }

        battleLog.add("[Battle] Found opponent: " + player2.getUsername() + "\n");
        battleLog.add("[Battle] Starting battle between **" + player1.getUsername() + "** ELo( " + player1.getElo() + ") and **" + player2.getUsername() + "** Elo( " + player2.getElo() + ")\n");

        // Bestimme zufälliges Wetter
        String[] weatherTypes = {"Rain", "Heat", "MysticalFog", "Storm", "SunnyDay"};
        String battleWeather = weatherTypes[new Random().nextInt(weatherTypes.length)];
        battleLog.add("[Weather] The weather has been set to: " + battleWeather + "\n");

        int round = 0;

        while (round < MAX_ROUNDS && !player1.getDeck().isEmpty() && !player2.getDeck().isEmpty()) {

            round++;
            Card card1 = player1.drawRandomCard();
            Card card2 = player2.drawRandomCard();

            battleLog.add("-------------------- Round " + round + ": " + card1.getName() + " vs. " + card2.getName() + " --------------------\n");
            battleLog.add("[BATTLELOG] **" + player1.getUsername() + "** Cards in Deck: " + player1.getDeck().size() + " <---> **" +player2.getUsername() + "** Cards in Deck: " + player2.getDeck().size() + "\n" );
            String result = resolveBattle(card1, card2, player1, player2, battleWeather);
            battleLog.add(result);
        }

        String winner = determineWinner(player1, player2);
        battleLog.add("!! The Winner of this battle is: **" + winner + "** !!\n");
        battleLog.add("The elo of **" + player1.getUsername() + "** is now: " + player1.getElo() + "\nThe elo of **" + player2.getUsername() + "** is now: " + player2.getElo() + "\n");
        battleLog.add("-------------------------------------------------------------------------\n");

        return String.join("\n", battleLog);


    }

    public String resolveBattle(Card card1, Card card2, Player player1, Player player2, String battleWeather) {
        if (isSpecialRuleApplied(player1, player2, card1, card2)) {
            return "Special rule was applied! \n";
        }

        double damage1 = card1.calculateDamage(card2, battleLog);
        double damage2 = card2.calculateDamage(card1, battleLog);

        // Anpassung des Schadens basierend auf dem Wetter
        damage1 = applyWeatherEffect(damage1, card1, battleWeather);
        damage2 = applyWeatherEffect(damage2, card2, battleWeather);

        // Log the results of the damage calculation
        battleLog.add("     - Card1: " + card1.getName() + " with a new damage value of(after Calculation & WeatherEffect): " + damage1 +
                "\n     - Card2: " + card2.getName() + " with a new damage value of(after Calculation & WeatherEffect): " + damage2 + "\n");


        if (damage1 > damage2) {
            player1.winCard(card2);
            player2.loseCard(card2);
            return "Result: **" + player1.getUsername() + "** wins the round! and wins the card: **" + card2.getName() + "** \n";
        } else if (damage2 > damage1) {
            player2.winCard(card1);
            player1.loseCard(card1);
            return "Result: **" + player2.getUsername() + "** wins the round! and wins the card: **" + card1.getName() + "** \n";
        } else {
            return "Round is a draw.";
        }
    }

    private boolean isSpecialRuleApplied(Player player1, Player player2, Card card1, Card card2) {
        // Goblins vs. Dragons
        if (isGoblin(card1) && isDragon(card2)) {
            battleLog.add("[INFO] Goblins are too afraid of Dragons to attack.");
            player2.winCard(card1);
            player1.loseCard(card1);
            battleLog.add("Result: **" + player2.getUsername() + "** wins the round! and wins the card: **" + card1.getName() + "** \n");
            return true;
        }else if (isDragon(card1) && isGoblin(card2)) {
            battleLog.add("[INFO] Goblins are too afraid of Dragons to attack.");
            player1.winCard(card2);
            player2.loseCard(card2);
            battleLog.add("Result: **" + player1.getUsername() + "** wins the round! and wins the card: **" + card2.getName() + "** \n");
            return true;
        }else if (isOrk(card1) && isWizard(card2)) {    //Ork vs Wizzard
            battleLog.add("[INFO] Wizards can control Orks. Orks deal no damage.");
            player2.winCard(card1);
            player1.loseCard(card1);
            battleLog.add("Result: **" + player2.getUsername() + "** wins the round! and wins the card: **" + card1.getName() + "** \n");
            return true;
        }else if (isWizard(card1) && isOrk(card2)) {
            battleLog.add("[INFO] Wizards can control Orks. Orks deal no damage.");
            player1.winCard(card2);
            player2.loseCard(card2);
            battleLog.add("Result: **" + player1.getUsername() + "** wins the round! and wins the card: **" + card2.getName() + "** \n");
            return true;
        }else if (isKnight(card1) && isWaterSpell(card2)) {  // Knight vs. WaterSpell
            battleLog.add("[INFO] Knights drown instantly when hit by WaterSpells.");
            player2.winCard(card1);
            player1.loseCard(card1);
            battleLog.add("Result: **" + player2.getUsername() + "** wins the round! and wins the card: **" + card1.getName() + "** \n");
            return true;
        }else if (isWaterSpell(card1) && isKnight(card2)) {
            battleLog.add("[INFO] Knights drown instantly when hit by WaterSpells.");
            player1.winCard(card2);
            player2.loseCard(card2);
            battleLog.add("Result: **" + player1.getUsername() + "** wins the round! and wins the card: **" + card2.getName() + "** \n");
            return true;
        }else if (isKraken(card1) && isSpell(card2)) {    // Kraken is immune to spells
            battleLog.add("[INFO] The Kraken is immune to spells.");
            player1.winCard(card2);
            player2.loseCard(card2);
            battleLog.add("Result: **" + player1.getUsername() + "** wins the round! and wins the card: **" + card2.getName() + "** \n");
            return true;
        }if (isSpell(card1) && isKraken(card2)) {
            battleLog.add("[INFO] The Kraken is immune to spells.");
            player2.winCard(card1);
            player1.loseCard(card1);
            battleLog.add("Result: **" + player2.getUsername() + "** wins the round! and wins the card: **" + card1.getName() + "** \n");
            return true;
        }else if (isFireElf(card1) && isDragon(card2)) {  // FireElf can evade Dragons
            battleLog.add("[INFO] FireElves can evade Dragon attacks.");
            player1.winCard(card2);
            player2.loseCard(card2);
            battleLog.add("Result: **" + player1.getUsername() + "** wins the round! and wins the card: **" + card2.getName() + "** \n");
            return true;
        }else if (isDragon(card1) && isFireElf(card2)) {
            battleLog.add("[INFO] FireElves can evade Dragon attacks.");
            player2.winCard(card1);
            player1.loseCard(card1);
            battleLog.add("Result: **" + player2.getUsername() + "** wins the round! and wins the card: **" + card1.getName() + "** \n");
            return true;
        }

        return false;
    }

    private double applyWeatherEffect(double damage, Card card, String weather) {
        // Wenden die Wettereffekte auf den Schaden an
        if (weather.equals("Rain")) {
            if (card.getElementType() == ElementType.WATER) {
                damage *= 1.1;  // +10% für Wasser-Karten
            } else if (card.getElementType() == ElementType.NORMAL) {
                damage *= 0.9;  // -10% für Feuer-Karten
            }
        } else if (weather.equals("Heat")) {
            if (card.getElementType() == ElementType.FIRE) {
                damage *= 1.1;  // +10% für Feuer-Karten
            } else if (card.getElementType() == ElementType.WATER) {
                damage *= 0.9;  // -10% für Wasser-Karten
            }
        } else if (weather.equals("MysticalFog")) {
            if (card.getType() == CardType.SPELL) {
                damage *= 1.05;  // +5% für Spells
            } else if (card.getType() == CardType.MONSTER) {
                damage *= 0.95;  // -5% für Monster
            }
        } else if (weather.equals("Storm")) {
            if(card.getType() == CardType.MONSTER) {}
            damage *= 1.05;  // +5% für alle Monster
            if (card.getType() == CardType.SPELL) {
                damage *= 0.95;  // -5% für Spells
            }
        } else if (weather.equals("SunnyDay")) {
            if(card.getElementType() == ElementType.NORMAL) {}
            damage *= 1.1;  // +10% für Normale Karten
            if (card.getElementType() == ElementType.FIRE) {
                damage *= 0.9;  // -10% für Feuer Karten
            }
        }
        return damage;
    }

    public String determineWinner(Player player1, Player player2) {
        //Player 1 gewinnt
        if (player1.getDeck().size() > player2.getDeck().size()) {
            adjustElo(player1, player2);
            return player1.getUsername();
            //player 2 gewinnt
        } else if (player2.getDeck().size() > player1.getDeck().size()) {
            adjustElo(player2, player1);
            return player2.getUsername();
        } else {
            return "Battle ended in a draw! ";
        }
    }

    public void adjustElo(Player winner, Player loser) {
        //Elo beim player objekt und in der Datenbank anpassen dort wird games_played auch upgedated
        int winnerElo = winner.getElo() + 3;
        winner.setElo(winnerElo);
        String winnerUsername = winner.getUsername();

        int loserElo = loser.getElo() - 5;
        loser.setElo(loserElo);
        String loserUsername = loser.getUsername();
        try {
            userDB.updateEloByUsername(winnerUsername, winnerElo);
            userDB.updateEloByUsername(loserUsername, loserElo);

        } catch (SQLException e) {
            System.err.println("Error updating ELO: " + e.getMessage());
        }
    }


    private boolean isGoblin(Card card) {
        return card.getName().toLowerCase().contains("goblin");
    }

    private boolean isDragon(Card card) {
        return card.getName().toLowerCase().contains("dragon");
    }

    private boolean isOrk(Card card) {
        return card.getName().toLowerCase().contains("ork");
    }

    private boolean isWizard(Card card) {
        return card.getName().toLowerCase().contains("wizzard");
    }

    private boolean isKnight(Card card) {
        return card.getName().toLowerCase().contains("knight");
    }

    private boolean isWaterSpell(Card card) {
        return card.getName().toLowerCase().contains("waterspell");
    }

    private boolean isKraken(Card card) {
        return card.getName().toLowerCase().contains("kraken");
    }

    private boolean isSpell(Card card) {
        return card.getName().toLowerCase().contains("spell");
    }

    private boolean isFireElf(Card card) {
        return card.getName().toLowerCase().contains("fireelf");
    }

}  