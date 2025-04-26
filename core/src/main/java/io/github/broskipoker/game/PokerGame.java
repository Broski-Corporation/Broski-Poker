package io.github.broskipoker.game;

import java.util.ArrayList;
import java.util.List;

public class PokerGame {
    private Deck deck;
    private List<Player> players;
    private List<Card> communityCards;
    private int pot;
    private int currentBet;
    private int currentPlayerIndex;
    private GameState gameState;

    public enum GameState {
        WAITING_FOR_PLAYERS, DEALING, BETTING_PRE_FLOP, FLOP, BETTING_FLOP, TURN, BETTING_TURN, RIVER,
        BETTING_RIVER, SHOWDOWN
    }

    public PokerGame() {
        deck = new Deck();
        players = new ArrayList<>();
        communityCards = new ArrayList<>();
        pot = 0;
        currentBet = 0;
        gameState = GameState.WAITING_FOR_PLAYERS;

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
        currentPlayerIndex = 0;
        communityCards.clear();

        // Reset player states
        for (Player player : players) {
            player.clearHand();
            player.resetBet();
            player.setActive(true);
        }

        // Deal cards
        dealHoleCards();
        gameState = GameState.BETTING_PRE_FLOP;
    }

    private void dealHoleCards() {
        for (int i = 0; i < 2; i++) {
            for (Player player : players) {
                player.receiveCard(deck.drawCard());
            }
        }
    }

    // TODO: Implement update()
    public void update(float delta) {
        switch (gameState) {
            case BETTING_PRE_FLOP:
                // Handle betting logic
                break;
            case BETTING_FLOP:
                // Handle betting logic
                break;
            case BETTING_TURN:
                // Handle betting logic
                break;
            case BETTING_RIVER:
                // Handle betting logic
                break;
            case SHOWDOWN:
                // Handle showdown logic
                break;
            default:
                break;
        }
    }

    public void dealFlop() {
        deck.drawCard(); // Burn a card
        for (int i = 0; i < 3; i++) {
            communityCards.add(deck.drawCard());
        }
        gameState = GameState.BETTING_FLOP;
    }

    public void dealTurn() {
        deck.drawCard(); // Burn a card
        communityCards.add(deck.drawCard());
        gameState = GameState.BETTING_TURN;
    }

    public void dealRiver() {
        deck.drawCard(); // Burn a card
        communityCards.add(deck.drawCard());
        gameState = GameState.BETTING_RIVER;
    }

    public Player determineWinner() {
        Player winner = null;
        PokerHand bestHand = null;

        for (Player player : players) {
            if (player.isActive()) {
                PokerHand currentHand = new PokerHand(player.getHoleCards(), communityCards);
                if (bestHand == null || currentHand.compareTo(bestHand) > 0) {
                    bestHand = currentHand;
                    winner = player;
                }
            }
        }
        return winner;
    }

    // TODO: betting logic, showdown

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

}
