/**
 * PokerGame.java
 * <p>
 * Game logic and state management for the poker game.
 * Acts as the Model in the MVC pattern.
 * <p>
 * Responsibilities:
 * - Manages core game state (pre-flop, flop, turn, river, showdown)
 * - Handles poker rules and gameplay logic
 * - Tracks players, cards, bets, and pot
 * - Controls game progression through betting rounds
 * - Evaluates hands and determines winners
 * - Manages chip distribution
 * - Maintains dealer position and blind structure
 */

package io.github.broskipoker.game;

import com.badlogic.gdx.Gdx;
import io.github.broskipoker.Main;
import io.github.broskipoker.ui.DealingAnimator;
import io.github.broskipoker.ui.GameRenderer;

import java.util.ArrayList;
import java.util.List;

public class PokerGame {
    private Deck deck;
    private List<Player> players;
    private List<Card> communityCards;
    private int pot;
    private int smallBlind;
    private int bigBlind;
    private int currentBet;
    private int currentPlayerIndex;
    private int lastRaisePlayerIndex;
    private static int dealerPosition;
    private boolean needsPlayerAction;
    private static GameState gameState;
    // Track players who have acted in the current betting round
    private boolean[] hasActedInRound;
    private float showdownTimer = 0;
    private final float SHOWDOWN_DURATION = 10.0f;
    // Table code for multiplayer mode
    private String tableCode;

    public enum GameState {
        WAITING_FOR_PLAYERS, DEALING, BETTING_PRE_FLOP, FLOP, BETTING_FLOP, TURN, BETTING_TURN, RIVER,
        BETTING_RIVER, SHOWDOWN
    }

    public enum PlayerAction {
        CHECK, CALL, RAISE, FOLD
    }

    public PokerGame(int smallBlind, int bigBlind) {
        deck = new Deck();
        players = new ArrayList<>();
        communityCards = new ArrayList<>();
        pot = 0;
        this.smallBlind = smallBlind;
        this.bigBlind = bigBlind;
        currentBet = 0;
        currentPlayerIndex = 0;
        lastRaisePlayerIndex = -1; // Initialize to -1 (no raises yet)
        dealerPosition = -1;
        needsPlayerAction = false;
        gameState = GameState.WAITING_FOR_PLAYERS;
    }

    public PokerGame() {
        this(50, 100); // Testing values

        // Initialize players for tests
        for (int i = 0; i < 5; i++) {
            addPlayer("Player " + (i + 1), 10000);
        }
    }

    public void addPlayer(String name, int startingChips) {
        players.add(new Player(name, startingChips));
        // Resize the hasActedInRound array when adding players
        hasActedInRound = new boolean[players.size()];
    }

    public void startNewHand() {
        if (dealerPosition == -1) {
            dealerPosition = players.size() - 1;
        }

        // Reset game state
        deck.reset(); // Also includes shuffle
        pot = 0;
        currentBet = 0;
        communityCards.clear();

        // Initialize tracking of player actions
        hasActedInRound = new boolean[players.size()];

        // Reset player states
        for (Player player : players) {
            player.clearHand();
            player.resetBet();
            player.setActive(true);
        }

        // Rotate dealer position
        dealerPosition = (dealerPosition + 1) % players.size();

        // Set blinds
        int smallBlindPos = (dealerPosition) % players.size();
        int bigBlindPos = (dealerPosition + 1) % players.size();

        // Players post blinds
        Player smallBlindPlayer = players.get(smallBlindPos);
        Player bigBlindPlayer = players.get(bigBlindPos);

        pot += smallBlindPlayer.bet(smallBlind);
        pot += bigBlindPlayer.bet(bigBlind);
        currentBet = bigBlind;

        // Mark players who posted blinds as having acted
        hasActedInRound[smallBlindPos] = true;
        hasActedInRound[bigBlindPos] = false;

        // Don't set lastRaisePlayerIndex to bigBlindPos as it's not a raise
        lastRaisePlayerIndex = bigBlindPos;

        // First player to act is UTG (Under the Gun) - player after big blind
        currentPlayerIndex = (bigBlindPos + 1) % players.size();

        gameState = GameState.BETTING_PRE_FLOP;
        GameRenderer.resetGameRenderer();

        // Deal cards
        dealHoleCards();
        needsPlayerAction = true;

        // for multiplayer shift the players list
        if (Main.getInstance().getRenderer().isMultiplayer()) {
            Player firstPlayer = players.remove(0);
            players.add(firstPlayer);
        }
    }

    // Method called from libGDX game loop
    public void update(float delta) {
        if (!needsPlayerAction) {
            switch (gameState) {
                case BETTING_PRE_FLOP:
                    if (isBettingRoundComplete()) {
                        dealFlop();
                        resetBettingRound();
                    }
                    break;
                case BETTING_FLOP:
                    if (isBettingRoundComplete()) {
                        dealTurn();
                        resetBettingRound();
                    }
                    break;
                case BETTING_TURN:
                    if (isBettingRoundComplete()) {
                        dealRiver();
                        resetBettingRound();
                    }
                    break;
                case BETTING_RIVER:
                    if (isBettingRoundComplete() || hasWinnerByFold()) {
                        goToShowdown();
                    }
                    break;
                case SHOWDOWN:
                    showdownTimer += delta;
                    if (showdownTimer >= SHOWDOWN_DURATION) {
                        distributeWinnings();
                        startNewHand();
                        showdownTimer = 0;
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void dealHoleCards() {
        for (int i = 0; i < 2; i++) {
            for (Player player : players) {
                player.receiveCard(deck.drawCard());
            }
        }
    }

    public void dealFlop() {
        deck.drawCard(); // Burn a card
        for (int i = 0; i < 3; i++) {
            communityCards.add(deck.drawCard());
        }
        gameState = GameState.BETTING_FLOP;
        needsPlayerAction = true;
    }

    public void dealTurn() {
        deck.drawCard(); // Burn a card
        communityCards.add(deck.drawCard());
        gameState = GameState.BETTING_TURN;
        needsPlayerAction = true;
    }

    public void dealRiver() {
        deck.drawCard(); // Burn a card
        communityCards.add(deck.drawCard());
        gameState = GameState.BETTING_RIVER;
        needsPlayerAction = true;
    }

    public void goToShowdown() {
        gameState = GameState.SHOWDOWN;
        needsPlayerAction = false;
        showdownTimer = 0;
    }

    private void resetBettingRound() {
        currentBet = 0;
        lastRaisePlayerIndex = -1;

        // Reset player bets and action tracking
        for (int i = 0; i < players.size(); i++) {
            players.get(i).resetBet();
            hasActedInRound[i] = false;
        }

        // First player to act is the one after the dealer (small blind)
        currentPlayerIndex = (dealerPosition + 2) % players.size();

        // Skip players who have folded
        while (currentPlayerIndex < players.size() && !players.get(currentPlayerIndex).isActive()) {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        }
        needsPlayerAction = true;
    }

    public boolean isBettingRoundComplete() {
        if(hasWinnerByFold()) {
            goToShowdown();
            return true;
        }
        int activePlayers = 0;
        for (Player player : players) {
            if (player.isActive()) {
                activePlayers++;
            }
        }

        // If only one player remains active, betting is complete
        if (activePlayers <= 1) {
            return true;
        }

        // Check if all active players have matched the current bet or are all-in
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            if (player.isActive() &&
                !hasActedInRound[i] &&
                player.getCurrentBet() < currentBet &&
                player.getChips() > 0) {
                return false;
            }
        }

        // Check if all active players have had a chance to act in this round
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).isActive() && !hasActedInRound[i]) {
                return false;
            }
        }

        // If we had a raise, check if everyone after the raiser has had a chance to act
        if (lastRaisePlayerIndex != -1) {
            // Start from the player after the last raiser
            int checkIndex = (lastRaisePlayerIndex + 1) % players.size();

            // Go around until we reach the raiser again
            while (checkIndex != lastRaisePlayerIndex) {
                Player player = players.get(checkIndex);

                // If this player is active but hasn't acted since the last raise
                if (player.isActive() && !hasActedInRound[checkIndex]) {
                    return false;
                }

                checkIndex = (checkIndex + 1) % players.size();
            }
        }

        return true;
    }

    // Called via UI
    public void performAction(PlayerAction action, int betAmount) {
        if (!needsPlayerAction) {
            return;
        }

        Player currentPlayer = players.get(currentPlayerIndex);
        if (!currentPlayer.isActive()) {
            moveToNextPlayer();
            return;
        }

        boolean validAction = false;

        switch (action) {
            case CHECK:
                if (currentPlayer.getCurrentBet() < currentBet) {
                    return; // invalid action
                }
                validAction = true;
                break;

            case CALL:
                int callAmount = currentBet - currentPlayer.getCurrentBet();
                if (callAmount > 0) {
                    pot += currentPlayer.bet(callAmount);
                }
                validAction = true;
                break;

            case RAISE:
                if (betAmount <= currentBet) {
                    return; // invalid raise amount (raise must be at least current bet)
                }
                int raiseAmount = betAmount - currentPlayer.getCurrentBet();
                pot += currentPlayer.bet(raiseAmount);
                currentBet = betAmount;

                // When there's a raise, reset the acted flags except for the raiser
                for (int i = 0; i < hasActedInRound.length; i++) {
                    if (i != currentPlayerIndex) {
                        hasActedInRound[i] = false;
                    }
                }

                lastRaisePlayerIndex = currentPlayerIndex;
                validAction = true;
                break;

            case FOLD:
                currentPlayer.setActive(false);
                validAction = true;
                break;
        }

        if (validAction) {
            // Mark this player as having acted in this round
            hasActedInRound[currentPlayerIndex] = true;

            // Check if the betting round is complete after this action
            if (isBettingRoundComplete() || hasWinnerByFold()) {
                needsPlayerAction = false;
            }
            else {
                // Move to the next player
                moveToNextPlayer();
            }
        }

    }

    private void moveToNextPlayer() {
        do {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        } while (currentPlayerIndex < players.size() && !players.get(currentPlayerIndex).isActive());
    }

    public List<Player> determineWinners() {
        List<Player> winners = new ArrayList<>();
        PokerHand bestHand = null;

        for (Player player : players) {
            if (player.isActive()) {
                PokerHand currentHand = new PokerHand(player.getHoleCards(), communityCards);
                if (bestHand == null || currentHand.compareTo(bestHand) > 0) {
                    bestHand = currentHand;
                    winners = new ArrayList<>(); // clear the winners list
                    winners.add(player);
                } else if (currentHand.compareTo(bestHand) == 0) {
                    winners.add(player);
                }
            }
        }
        return winners;
    }

    public void distributeWinnings() {
        List<Player> winners = determineWinners();
        if (winners.isEmpty()) {
            return;
        }

        int winAmount = pot / winners.size();
        for (Player winner : winners) {
            winner.addChips(winAmount);
        }

        // if there is a division remainder, give it to the first winner in the list
        int remainder = pot % winners.size();
        if (remainder > 0) {
            winners.getFirst().addChips(remainder);
        }

        pot = 0;
    }

    // When all but one players fold, we have a single winner
    public boolean hasWinnerByFold() {
        int activePlayers = 0;
        Player lastActive = null;

        for (Player player : players) {
            if (player.isActive()) {
                activePlayers++;
                lastActive = player;
            }
        }

        if (activePlayers == 1) {
            // Award the winner
            lastActive.addChips(pot);
            pot = 0;
            return true;
        }
        return false;
    }

    public boolean hasPlayerActedInRound(int playerIndex) {
        if (playerIndex >= 0 && playerIndex < hasActedInRound.length) {
            return hasActedInRound[playerIndex];
        }
        return false;
    }

    public List<Card> getCommunityCards() {
        return communityCards;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public int getPot() {
        return pot;
    }

    public static GameState getGameState() {
        return gameState;
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    public boolean needsPlayerAction() {
        return needsPlayerAction;
    }

    public int getCurrentBet() {
        return currentBet;
    }

    public static int getDealerPosition() {
        return dealerPosition;
    }

    public int getSmallBlind() {
        return smallBlind;
    }

    public int getBigBlind() {
        return bigBlind;
    }

    public int getLastRaisePlayerIndex() {
        return lastRaisePlayerIndex;
    }

    public boolean[] getHasActedInRound() {
        return hasActedInRound;
    }

    public Deck getDeck() {
        return deck;
    }

    public void setDeck(Deck deck) {
        this.deck = deck;
    }

    public float getSHOWDOWN_DURATION() {
        return SHOWDOWN_DURATION;
    }

    public float getShowdownTimer() {
        return showdownTimer;
    }

    public void setShowdownTimer(float showdownTimer) {
        this.showdownTimer = showdownTimer;
    }

    public void setHasActedInRound(boolean[] hasActedInRound) {
        this.hasActedInRound = hasActedInRound;
    }

    public static void setGameState(GameState gameState) {
        PokerGame.gameState = gameState;
    }

    public boolean isNeedsPlayerAction() {
        return needsPlayerAction;
    }

    public void setNeedsPlayerAction(boolean needsPlayerAction) {
        this.needsPlayerAction = needsPlayerAction;
    }

    public static void setDealerPosition(int dealerPosition) {
        PokerGame.dealerPosition = dealerPosition;
    }

    public void setLastRaisePlayerIndex(int lastRaisePlayerIndex) {
        this.lastRaisePlayerIndex = lastRaisePlayerIndex;
    }

    public void setCurrentPlayerIndex(int currentPlayerIndex) {
        this.currentPlayerIndex = currentPlayerIndex;
    }

    public void setCurrentBet(int currentBet) {
        this.currentBet = currentBet;
    }

    public void setBigBlind(int bigBlind) {
        this.bigBlind = bigBlind;
    }

    public void setSmallBlind(int smallBlind) {
        this.smallBlind = smallBlind;
    }

    public void setPot(int pot) {
        this.pot = pot;
    }

    public void setCommunityCards(List<Card> communityCards) {
        this.communityCards = communityCards;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public String getTableCode() {
        return tableCode;
    }

    public void setTableCode(String tableCode) {
        this.tableCode = tableCode;
    }
}
