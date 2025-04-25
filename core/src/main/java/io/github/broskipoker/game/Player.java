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

}
