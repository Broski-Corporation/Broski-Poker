package io.github.broskipoker.game.tests;

import io.github.broskipoker.game.Card;
import io.github.broskipoker.game.Deck;
import io.github.broskipoker.game.Player;
import io.github.broskipoker.game.PokerGame;
import io.github.broskipoker.game.PokerHand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*; // For mocking the Deck

public class PokerGameTest {

    private PokerGame game;
    private Deck mockDeck;

    // Helper method to create a card for cleaner test readability
    private Card c(Card.Suit suit, Card.Rank rank) {
        return new Card(suit, rank);
    }

    // Helper method to set private static dealerPosition via reflection
    private void setStaticDealerPosition(int position) {
        try {
            Field field = PokerGame.class.getDeclaredField("dealerPosition");
            field.setAccessible(true);
            field.set(null, position); // null for static fields
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Failed to set static dealerPosition via reflection: " + e.getMessage());
        }
    }

    // Helper method to get private static dealerPosition via reflection
    private int getStaticDealerPosition() {
        try {
            Field field = PokerGame.class.getDeclaredField("dealerPosition");
            field.setAccessible(true);
            return (int) field.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Failed to get static dealerPosition via reflection: " + e.getMessage());
            return -1; // Should not happen
        }
    }

    // Helper method to get private hasActedInRound via reflection
    private boolean[] getHasActedInRoundArray() {
        try {
            Field field = PokerGame.class.getDeclaredField("hasActedInRound");
            field.setAccessible(true);
            return (boolean[]) field.get(game);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Failed to get hasActedInRound via reflection: " + e.getMessage());
            return null; // Should not happen
        }
    }

    // Helper method to get private lastRaisePlayerIndex via reflection
    private int getLastRaisePlayerIndex() {
        try {
            Field field = PokerGame.class.getDeclaredField("lastRaisePlayerIndex");
            field.setAccessible(true);
            return (int) field.get(game);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Failed to get lastRaisePlayerIndex via reflection: " + e.getMessage());
            return -1; // Should not happen
        }
    }

    // Helper method to set private pot via reflection
    private void setGamePot(int newPot) {
        try {
            Field field = PokerGame.class.getDeclaredField("pot");
            field.setAccessible(true);
            field.set(game, newPot);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Failed to set pot via reflection: " + e.getMessage());
        }
    }


    @BeforeEach
    void setUp() {
        // Mock the Deck to control card distribution
        mockDeck = mock(Deck.class);
        // We cannot override createDeck() since it's not protected/public
        // So, we'll initialize PokerGame as normal and then,
        // if possible, inject the mock deck using reflection for the 'deck' field.
        game = new PokerGame(50, 100);

        try {
            Field deckField = PokerGame.class.getDeclaredField("deck");
            deckField.setAccessible(true);
            deckField.set(game, mockDeck);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Could not inject mockDeck into PokerGame: " + e.getMessage());
        }

        // Add players
        game.addPlayer("P1", 1000);
        game.addPlayer("P2", 1000);
        game.addPlayer("P3", 1000);
        game.addPlayer("P4", 1000); // 4 players for more robust betting tests
    }

    // A small helper to simulate game initialization with custom blinds
    // We can't directly test the custom constructor's private fields,
    // so this test will be limited to what's observable or settable.
    // The previous TestPokerGame subclass won't work as it relied on overriding protected methods.
    @Test
    @DisplayName("Test game initialization with custom blinds (limited checks)")
    void testCustomInitializationLimited() {
        // We cannot directly verify smallBlind/bigBlind if they are private and have no getters.
        // The default constructor is the only one that can add players.
        // So this test is very limited without getters in PokerGame.java.
        PokerGame customGame = new PokerGame(25, 50); // This will create an internal Deck.
        // We can only assert on static or publicly accessible state here.
        assertEquals(PokerGame.GameState.WAITING_FOR_PLAYERS, PokerGame.getGameState(), "Initial state should be WAITING_FOR_PLAYERS");
        assertEquals(0, customGame.getPot(), "Pot should be 0 on initialization"); // Accessible via public getPot
        assertFalse(customGame.needsPlayerAction(), "Needs player action should be false initially");
    }

    @Test
    @DisplayName("Test game initialization with default values (for testing)")
    void testDefaultInitialization() {
        PokerGame defaultGame = new PokerGame(); // Uses the default constructor (for testing)
        assertEquals(5, defaultGame.getPlayers().size(), "Default constructor should add 5 players");
        assertEquals(PokerGame.GameState.WAITING_FOR_PLAYERS, PokerGame.getGameState(), "Initial state should be WAITING_FOR_PLAYERS");
        // Cannot assert on default smallBlind/bigBlind without getters
    }

    @Test
    @DisplayName("Test adding players (limited checks)")
    void testAddPlayerLimited() {
        // Re-initialize game to have an empty player list for adding
        game = new PokerGame(50, 100); // This will create an internal Deck.
        try {
            Field deckField = PokerGame.class.getDeclaredField("deck");
            deckField.setAccessible(true);
            deckField.set(game, mockDeck);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Could not inject mockDeck into PokerGame: " + e.getMessage());
        }

        game.addPlayer("New Player 1", 500);
        assertEquals(1, game.getPlayers().size(), "Should have 1 player");
        assertEquals("New Player 1", game.getPlayers().get(0).getName());

        game.addPlayer("New Player 2", 700);
        assertEquals(2, game.getPlayers().size(), "Should have 2 players");
        assertEquals("New Player 2", game.getPlayers().get(1).getName());

        // Cannot verify hasActedInRound array size adjusts without a public getter
    }




    @Test
    @DisplayName("Test `determineWinners` - single winner")
    void testDetermineWinnersSingle() {
        // Set up specific hands for showdown
        // P1: Full House (Aces full of Kings)
        // P2: Flush (Hearts, King high)
        // P3: Straight (Queen high)
        // P4: Pair (Aces, King kicker)

        List<Card> community = Arrays.asList(
            c(Card.Suit.HEARTS, Card.Rank.ACE),
            c(Card.Suit.DIAMONDS, Card.Rank.ACE),
            c(Card.Suit.CLUBS, Card.Rank.KING),
            c(Card.Suit.SPADES, Card.Rank.SEVEN),
            c(Card.Suit.DIAMONDS, Card.Rank.SIX)
        );
        game.getCommunityCards().addAll(community);

        // P1 (Full House)
        game.getPlayers().get(0).receiveCard(c(Card.Suit.SPADES, Card.Rank.ACE));
        game.getPlayers().get(0).receiveCard(c(Card.Suit.HEARTS, Card.Rank.KING));
        // P2 (Flush)
        game.getPlayers().get(1).receiveCard(c(Card.Suit.HEARTS, Card.Rank.KING));
        game.getPlayers().get(1).receiveCard(c(Card.Suit.HEARTS, Card.Rank.QUEEN));
        // P3 (Straight)
        game.getPlayers().get(2).receiveCard(c(Card.Suit.CLUBS, Card.Rank.TEN));
        game.getPlayers().get(2).receiveCard(c(Card.Suit.SPADES, Card.Rank.NINE));
        // P4 (Pair)
        game.getPlayers().get(3).receiveCard(c(Card.Suit.CLUBS, Card.Rank.QUEEN));
        game.getPlayers().get(3).receiveCard(c(Card.Suit.DIAMONDS, Card.Rank.TWO));

        // Ensure all players are active for showdown
        for (Player p : game.getPlayers()) {
            p.setActive(true);
        }

        List<Player> winners = game.determineWinners();
        assertEquals(1, winners.size(), "Should have one winner");
        assertEquals("P1", winners.get(0).getName(), "P1 (Full House) should win");

        // Verify that their actual evaluated hand matches
        PokerHand p1Hand = new PokerHand(game.getPlayers().get(0).getHoleCards(), community);
        assertEquals(PokerHand.HandRank.FULL_HOUSE, p1Hand.getRank());
    }

    @Test
    @DisplayName("Test `determineWinners` - multiple winners (chop pot)")
    void testDetermineWinnersChop() {
        // Set up specific hands for showdown resulting in a tie
        // P1: Straight (Ace high)
        // P2: Straight (Ace high)
        // P3: Flush (King high)
        // P4: Three of a Kind

        List<Card> community = Arrays.asList(
            c(Card.Suit.CLUBS, Card.Rank.QUEEN),
            c(Card.Suit.SPADES, Card.Rank.JACK),
            c(Card.Suit.HEARTS, Card.Rank.TEN),
            c(Card.Suit.DIAMONDS, Card.Rank.NINE),
            c(Card.Suit.SPADES, Card.Rank.FIVE)
        );
        game.getCommunityCards().addAll(community);

        // P1 (Straight: A-K-Q-J-10)
        game.getPlayers().get(0).receiveCard(c(Card.Suit.CLUBS, Card.Rank.ACE));
        game.getPlayers().get(0).receiveCard(c(Card.Suit.DIAMONDS, Card.Rank.KING));
        // P2 (Straight: A-K-Q-J-10) - same hand as P1 but different suits
        game.getPlayers().get(1).receiveCard(c(Card.Suit.HEARTS, Card.Rank.ACE));
        game.getPlayers().get(1).receiveCard(c(Card.Suit.CLUBS, Card.Rank.KING));
        // P3 (Flush) - a lower hand
        game.getPlayers().get(2).receiveCard(c(Card.Suit.CLUBS, Card.Rank.KING));
        game.getPlayers().get(2).receiveCard(c(Card.Suit.CLUBS, Card.Rank.EIGHT));
        // P4 (Three of a Kind) - even lower
        game.getPlayers().get(3).receiveCard(c(Card.Suit.DIAMONDS, Card.Rank.QUEEN));
        game.getPlayers().get(3).receiveCard(c(Card.Suit.HEARTS, Card.Rank.QUEEN));

        // Ensure all players are active for showdown
        for (Player p : game.getPlayers()) {
            p.setActive(true);
        }

        List<Player> winners = game.determineWinners();
        assertEquals(2, winners.size(), "Should have two winners (chop pot)");
        assertTrue(winners.contains(game.getPlayers().get(0)), "P1 should be a winner");
        assertTrue(winners.contains(game.getPlayers().get(1)), "P2 should be a winner");
        assertFalse(winners.contains(game.getPlayers().get(2)), "P3 should not be a winner");
        assertFalse(winners.contains(game.getPlayers().get(3)), "P4 should not be a winner");
    }

}
