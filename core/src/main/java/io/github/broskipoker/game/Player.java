package io.github.broskipoker.game;

import java.util.ArrayList;
import java.util.List;

public class Player {
    private final String name;
    private int chips;
    private final List<Card> holeCards;
    private boolean isActive;
    private int currentBet;

    public Player(String name, int startingChips) {
        this.name = name;
        this.chips = startingChips;
        this.holeCards = new ArrayList<>();
        this.isActive = true;
        this.currentBet = 0;
    }

    public Player(Player player){
        this.name = player.name;
        this.chips = player.chips;
        this.holeCards = new ArrayList<>(player.holeCards);
        this.isActive = player.isActive;
        this.currentBet = player.currentBet;
    }

    public void receiveCard(Card card) {
        holeCards.add(card);
    }

    public void clearHand() {
        holeCards.clear();
    }

    public List<Card> getHoleCards() {
        return holeCards;
    }

    public int bet(int amount) {
        int actualBet = Math.min(amount, chips);
        chips -= actualBet;
        currentBet += actualBet;
        return actualBet;
    }

    public void addChips(int amount) {
        chips += amount;
    }

    public String getName() {
        return name;
    }

    public int getChips() {
        return chips;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isActive() {
        return isActive;
    }

    public int getCurrentBet() {
        return currentBet;
    }

    public void resetBet() {
        currentBet = 0;
    }

    public void setCurrentBet(int bet) {
        this.currentBet = bet;
    }

    public void clearHoleCards() {
        holeCards.clear();
    }

    public void addCard(Card card) {
        holeCards.add(card);
    }
}
