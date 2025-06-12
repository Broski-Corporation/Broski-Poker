/**
 * DealingAnimator.java
 * <p>
 * Helper class that manages the animation state for dealing cards.
 * <p>
 * Responsibilities:
 * - Track which cards have been dealt to which players
 * - Manage timing between card dealing animations
 * - Control the animation sequence for distributing cards
 * - Provide methods to query animation state
 * - Reset animation state between game rounds
 */

package io.github.broskipoker.ui;

import io.github.broskipoker.Main;
import io.github.broskipoker.game.Card;
import io.github.broskipoker.game.Player;
import io.github.broskipoker.game.PokerGame;

import java.util.Arrays;
import java.util.List;

public class DealingAnimator {
    private int currentPlayerIndex;
    private int dealingRound;
    private boolean dealingComplete;
    private boolean[][] dealtCards;
    private float elapsedTime;
    private final float dealingInterval = 0.3f;
    private int maxPlayers;
    private int dealerPosition;


    public DealingAnimator(int playerCount, int dealerPosition) {
        this.maxPlayers = playerCount;
        dealtCards = new boolean[playerCount][2];
        this.dealerPosition = dealerPosition;
        reset();
    }

    public void update(float delta, List<Player> players, int maxPlayerPositions) {
        // in mp mode, imeediately mark all cards as dealt
        if (Main.getInstance().getRenderer().isMultiplayer()) {
            forceAllCardsDealt();
            return;
        }

        if (dealingComplete){
            return;
        }

        // Animate card distribution one at a time
        elapsedTime += delta;
        if (elapsedTime > dealingInterval) {
            if (currentPlayerIndex < players.size() && currentPlayerIndex < maxPlayerPositions) {
                Card[] playerCards = players.get(currentPlayerIndex).getHoleCards().toArray(new Card[0]);
                if (dealingRound < 2 && playerCards.length >= 2) {
                    dealtCards[currentPlayerIndex][dealingRound] = true; // Mark this card as dealt
                    currentPlayerIndex = (currentPlayerIndex + 1) % maxPlayers; // Move to next player
                    // If we've dealt to all players in this round, move to next round
                    if (currentPlayerIndex == PokerGame.getDealerPosition() || currentPlayerIndex >= maxPlayerPositions) {
                        if (dealingRound < 1) {
                            dealingRound++; // Start second round
                            currentPlayerIndex = PokerGame.getDealerPosition();
                        } else {
                            dealingComplete = true; // Both rounds completed
                        }
                    }
                }
            }
            elapsedTime = 0f;
        }

    }

    public void reset() {
        currentPlayerIndex = PokerGame.getDealerPosition() % maxPlayers; // Start from the next player
        dealingRound = 0;
        dealingComplete = false;
        elapsedTime = 0f;


        for (boolean[] dealtCard : dealtCards) {
            Arrays.fill(dealtCard, false);
        }
    }

    public boolean isCardDealt(int playerIndex, int cardIndex) {
        if (playerIndex < 0 || playerIndex >= dealtCards.length ||
            cardIndex < 0 || cardIndex >= 2) {
            return false;
        }
        return dealtCards[playerIndex][cardIndex];
    }

    // used in multiplayer to skip the animation and force all cards to be dealt
    public void forceAllCardsDealt() {
        for (int i = 0; i < dealtCards.length; i++) {
            for(int j = 0; j < dealtCards[i].length; j++) {
                dealtCards[i][j] = true;
            }
        }
        dealingComplete = true;
    }

    public boolean isComplete() {
        return dealingComplete;
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    public int getDealingRound() {
        return dealingRound;
    }
}
