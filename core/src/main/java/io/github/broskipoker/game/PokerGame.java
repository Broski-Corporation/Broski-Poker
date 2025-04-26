package io.github.broskipoker.game;

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
    private int dealerPosition;
    private boolean needsPlayerAction;
    private GameState gameState;

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
        lastRaisePlayerIndex = 0;
        dealerPosition = 0;
        needsPlayerAction = false;
        gameState = GameState.WAITING_FOR_PLAYERS;
    }

    public PokerGame() {
        this(50, 100); // Testing values

        // Initialize players for tests
        for (int i = 0; i < 4; i++) {
            addPlayer("Player " + (i + 1), 10000);
        }
    }

    public void addPlayer(String name, int startingChips) {
        players.add(new Player(name, startingChips));
    }

    public void startNewHand() {
        // Reset game state
        deck.reset(); // Also includes shuffle
        pot = 0;
        currentBet = 0;
        communityCards.clear();

        // Reset player states
        for (Player player : players) {
            player.clearHand();
            player.resetBet();
            player.setActive(true);
        }

        // Rotate dealer position
        dealerPosition = (dealerPosition + 1) % players.size();

        // Set blinds
        int smallBlindPos = (dealerPosition + 1) % players.size();
        int bigBlindPos = (dealerPosition + 2) % players.size();

        // Players post blinds
        Player smallBlindPlayer = players.get(smallBlindPos);
        Player bigBlindPlayer = players.get(bigBlindPos);

        pot += smallBlindPlayer.bet(smallBlind);
        pot += bigBlindPlayer.bet(bigBlind);
        currentBet = bigBlind;

        // First player to act is the one after the bigBlindPlayer
        currentPlayerIndex = (bigBlindPos + 1) % players.size();

        // Deal cards
        dealHoleCards();
        gameState = GameState.BETTING_PRE_FLOP;
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
                    if (isBettingRoundComplete()) {
                        goToShowdown();
                    }
                    break;
                case SHOWDOWN:
                    // TODO: handle end of hand logic
                    distributeWinnings();
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
    }

    private void resetBettingRound() {
        currentBet = 0;
        lastRaisePlayerIndex = -1;
        for (Player player : players) {
            player.resetBet();
        }

        // First player to act is the one after the dealer
        currentPlayerIndex = (dealerPosition + 1) % players.size();

        // Skip players who have folded
        while (currentPlayerIndex < players.size() && !players.get(currentPlayerIndex).isActive()) {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        }
        needsPlayerAction = true;
    }

    public boolean isBettingRoundComplete() {
        int activePlayers = 0;
        for (Player player : players) {
            if (player.isActive()) {
                activePlayers++;
            }
        }

        if (activePlayers <= 1) {
            return true;
        }

        // Check if all active players matched the current bet
        for (Player player : players) {
            if (player.isActive() && player.getCurrentBet() < currentBet && player.getChips() > 0) {
                return false;
            }
        }

        // Check if we did a complete trip over the table since last raise,
        // while keeping in mind that there can be inactive players
        if (lastRaisePlayerIndex == -1) {
            return true; // no raises and all players checked
        }

        int checkIndex = currentPlayerIndex;
        do {
            if (players.get(checkIndex).isActive()) {
                if (checkIndex == lastRaisePlayerIndex) {
                    return true; // full circle
                }
            }
            checkIndex = (checkIndex + 1) % players.size();
        } while (checkIndex != currentPlayerIndex);

        return false;
    }

    // Called via UI
    public boolean performAction(PlayerAction action, int betAmount) {
        if (!needsPlayerAction) {
            return false;
        }

        Player currentPlayer = players.get(currentPlayerIndex);
        if (!currentPlayer.isActive()) {
            moveToNextPlayer();
            return true;
        }

        switch (action) {
            case CHECK:
                if (currentPlayer.getCurrentBet() < currentBet) {
                    return false; // invalid action
                }
                break;
            case CALL:
                int callAmount = currentBet - currentPlayer.getCurrentBet();
                if (callAmount > 0) {
                    pot += currentPlayer.bet(callAmount);
                }
                break;
            case RAISE:
                if (betAmount <= currentBet) {
                    return false; // invalid raise amount (raise mut be at least current bet)
                }
                int raiseAmount = betAmount - currentPlayer.getCurrentBet();
                pot += currentPlayer.bet(raiseAmount);
                currentBet = betAmount;
                lastRaisePlayerIndex = currentPlayerIndex;
                break;
            case FOLD:
                currentPlayer.setActive(false);
                break;
        }

        moveToNextPlayer();
        if (isBettingRoundComplete()) {
            needsPlayerAction = false;
        }
        return true;
    }

    private void moveToNextPlayer() {
        do {
            currentPlayerIndex = (currentPlayerIndex + 1) & players.size();
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

    public List<Card> getCommunityCards() {
        return communityCards;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public int getPot() {
        return pot;
    }

    public GameState getGameState() {
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

    public int getDealerPosition() {
        return dealerPosition;
    }
}
