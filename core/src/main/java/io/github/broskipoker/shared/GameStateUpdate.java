package io.github.broskipoker.shared;

import io.github.broskipoker.game.PokerGame;

import java.util.List;

public class GameStateUpdate {
    public List<CardInfo> communityCards;
    public List<PlayerInfo> players;
    public int pot;
    public int smallBlind;
    public int bigBlind;
    public int currentBet;
    public int currentPlayerIndex;
    public int lastRaisePlayerIndex;
    public int dealerPosition;
    public boolean needsPlayerAction;
    public PokerGame.GameState gameState;
    public boolean[] hasActedInRound;

    // Added fields for winner information
    public List<CardInfo> winningCards;       // The best hand to display
    public List<Integer> winnerIndices;       // Indices of winning players
    public boolean showAllCards;              // Flag to indicate all cards should be visible (for showdown)
}
