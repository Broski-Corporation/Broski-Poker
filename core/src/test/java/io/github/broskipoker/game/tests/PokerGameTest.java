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
    @DisplayName("Test startNewHand - blinds and initial state")
    void testStartNewHandBlindsAndState() {
        // Mock deck behavior for dealing initial cards (2 per player)
        when(mockDeck.drawCard())
            .thenReturn(c(Card.Suit.HEARTS, Card.Rank.ACE)) // P1 Card 1
            .thenReturn(c(Card.Suit.HEARTS, Card.Rank.KING)) // P2 Card 1
            .thenReturn(c(Card.Suit.HEARTS, Card.Rank.QUEEN)) // P3 Card 1
            .thenReturn(c(Card.Suit.HEARTS, Card.Rank.JACK)) // P4 Card 1
            .thenReturn(c(Card.Suit.DIAMONDS, Card.Rank.ACE)) // P1 Card 2
            .thenReturn(c(Card.Suit.DIAMONDS, Card.Rank.KING)) // P2 Card 2
            .thenReturn(c(Card.Suit.DIAMONDS, Card.Rank.QUEEN)) // P3 Card 2
            .thenReturn(c(Card.Suit.DIAMONDS, Card.Rank.JACK)); // P4 Card 2

        // Ensure dealer position is predictable for testing (set using reflection)
        setStaticDealerPosition(game.getPlayers().size() - 1); // Set dealer to P4 initially, so P1 becomes dealer after rotation

        game.startNewHand();

        // Check dealer position rotation
        assertEquals(0, getStaticDealerPosition(), "Dealer position should rotate to P1 (index 0)");

        // Blinds positions:
        // P1 (Dealer): index 0
        // P2 (Small Blind): index 1
        // P3 (Big Blind): index 2
        // P4 (UTG / First to act): index 3

        Player p1 = game.getPlayers().get(0); // Dealer
        Player p2 = game.getPlayers().get(1); // Small Blind
        Player p3 = game.getPlayers().get(2); // Big Blind
        Player p4 = game.getPlayers().get(3); // UTG

        // Chips assertions remain valid as getChips() is public
        assertEquals(1000 - 0, p1.getChips(), "Dealer (P1) chips should be unchanged");
        assertEquals(1000 - 50, p2.getChips(), "Small blind (P2) chips should be reduced by 50");
        assertEquals(1000 - 100, p3.getChips(), "Big blind (P3) chips should be reduced by 100");
        assertEquals(1000, p4.getChips(), "UTG (P4) chips should be unchanged initially");

        assertEquals(50 + 100, game.getPot(), "Pot should contain small and big blinds");
        assertEquals(100, game.getCurrentBet(), "Current bet should be equal to big blind");

        assertEquals(PokerGame.GameState.BETTING_PRE_FLOP, PokerGame.getGameState(), "Game state should be PRE_FLOP");
        assertTrue(game.needsPlayerAction(), "Needs player action should be true");

        assertEquals(3, game.getCurrentPlayerIndex(), "Current player should be UTG (P4)");

        // Check hole cards sizes
        assertEquals(2, p1.getHoleCards().size(), "P1 should have 2 hole cards");
        assertEquals(2, p2.getHoleCards().size(), "P2 should have 2 hole cards");
        assertEquals(2, p3.getHoleCards().size(), "P3 should have 2 hole cards");
        assertEquals(2, p4.getHoleCards().size(), "P4 should have 2 hole cards");

        // Verify hasActedInRound flags using reflection
        boolean[] hasActedInRound = getHasActedInRoundArray();
        assertNotNull(hasActedInRound, "hasActedInRound array should not be null");
        assertEquals(game.getPlayers().size(), hasActedInRound.length);
        assertTrue(hasActedInRound[1], "P2 (SB) should have acted (posted blind)"); // Small Blind is index 1
        assertFalse(hasActedInRound[2], "P3 (BB) should NOT have acted yet"); // Big Blind is index 2
        assertFalse(hasActedInRound[3], "P4 (UTG) should NOT have acted yet"); // UTG is index 3
        // P1 (Dealer) index 0: Needs specific logic for `startNewHand` to mark it true if desired.
        // Based on your PokerGame.java: hasActedInRound[smallBlindPos] = true;
        // This implies others are false. So P1 (dealer) would be false.
        assertFalse(hasActedInRound[0], "P1 (Dealer) should NOT have acted yet");

        // Verify lastRaisePlayerIndex (using reflection)
        assertEquals(2, getLastRaisePlayerIndex(), "Last raise player index should be Big Blind (P3)");
    }


    @Test
    @DisplayName("Test startNewHand - resets states")
    void testStartNewHandResets() {
        // Manipulate state to ensure reset works
        game.addPlayer("TestPlayer", 1000); // Add an extra player for hasActedInRound resizing
        game.getPlayers().get(0).receiveCard(c(Card.Suit.CLUBS, Card.Rank.ACE));
        game.getPlayers().get(0).setActive(false);
        game.getPlayers().get(0).bet(50);
        game.getCommunityCards().add(c(Card.Suit.HEARTS, Card.Rank.TEN));
        setGamePot(500); // Use reflection to set pot

        // Mock deck behavior for dealing initial cards
        when(mockDeck.drawCard()).thenReturn(c(Card.Suit.SPADES, Card.Rank.TWO));
        doNothing().when(mockDeck).reset(); // Mock reset call

        game.startNewHand();

        // Verify resets
        assertEquals(2, game.getPlayers().get(0).getHoleCards().size(), "Player should have 2 hole cards after new hand starts");

        assertTrue(game.getPlayers().get(0).isActive(), "Player should be active after reset");
        assertEquals(0, game.getPlayers().get(0).getCurrentBet(), "Player's current bet should be reset");
        assertEquals(0, game.getCommunityCards().size(), "Community cards should be cleared");
        assertTrue(game.getPot() > 0, "Pot should be non-zero (blinds) after reset and new hand starts"); // Blinds are posted
        verify(mockDeck, times(1)).reset(); // Ensure deck.reset() was called
    }

    @Test
    @DisplayName("Test player action - FOLD")
    void testPlayerActionFold() {
        // Setup initial state for a betting round
        startBettingRoundWithPlayersReady(); // This helper also mocks initial cards

        Player currentPlayerBeforeAction = game.getCurrentPlayer(); // P4 is UTG

        game.performAction(PokerGame.PlayerAction.FOLD, 0);

        assertFalse(currentPlayerBeforeAction.isActive(), "Player should be inactive after folding");

        // Use reflection to get hasActedInRound
        boolean[] hasActedInRound = getHasActedInRoundArray();
        assertFalse(hasActedInRound[3], "P4 (original current player) should be marked as acted after folding"); // P4 is index 3
        assertNotEquals(currentPlayerBeforeAction, game.getCurrentPlayer(), "Current player should move after fold");
        assertTrue(game.needsPlayerAction(), "Game should still need player action if others are active");
        assertFalse(game.hasWinnerByFold(), "No winner yet with multiple players");
    }

    @Test
    @DisplayName("Test player action - CALL")
    void testPlayerActionCall() {
        startBettingRoundWithPlayersReady(); // P4 is UTG, currentBet is 100 (BB)

        Player p4 = game.getPlayers().get(game.getCurrentPlayerIndex()); // P4
        int p4InitialChips = p4.getChips();
        int initialPot = game.getPot();

        game.performAction(PokerGame.PlayerAction.CALL, 0); // Call amount is calculated internally

        assertEquals(p4InitialChips - (game.getCurrentBet() - p4.getCurrentBet()), p4.getChips(), "P4 chips should decrease by call amount");
        assertEquals(game.getCurrentBet(), p4.getCurrentBet(), "P4 current bet should match table current bet");
        assertEquals(initialPot + (game.getCurrentBet() - p4.getCurrentBet()), game.getPot(), "Pot should increase by call amount");

        boolean[] hasActedInRound = getHasActedInRoundArray();
        assertTrue(hasActedInRound[3], "P4 should have acted"); // P4 is index 3

        assertNotEquals(3, game.getCurrentPlayerIndex(), "Current player should move from P4 after P4 calls");
        assertTrue(game.needsPlayerAction(), "Game should still need player action");
    }

    @Test
    @DisplayName("Test player action - RAISE")
    void testPlayerActionRaise() {
        startBettingRoundWithPlayersReady(); // P4 is UTG, currentBet is 100 (BB)

        Player p4 = game.getPlayers().get(game.getCurrentPlayerIndex()); // P4
        int p4InitialChips = p4.getChips();
        int initialPot = game.getPot();
        int raiseAmount = 250; // Raise to 250 (initial bet was 100)

        game.performAction(PokerGame.PlayerAction.RAISE, raiseAmount);

        assertEquals(p4InitialChips - (raiseAmount - p4.getCurrentBet()), p4.getChips(), "P4 chips should decrease by raise amount");
        assertEquals(raiseAmount, game.getCurrentBet(), "Current bet should be updated to raise amount");
        assertEquals(initialPot + (raiseAmount - p4.getCurrentBet()), game.getPot(), "Pot should increase by raise amount");
        assertEquals(game.getCurrentPlayerIndex(), getLastRaisePlayerIndex(), "Last raise player index should be P4");

        // Verify other players' hasActedInRound are reset (except P4) using reflection
        boolean[] hasActedInRound = getHasActedInRoundArray();
        assertTrue(hasActedInRound[3], "P4 should have acted");
        assertFalse(hasActedInRound[0], "P1 should have hasActedInRound reset");
        assertFalse(hasActedInRound[1], "P2 should have hasActedInRound reset");
        assertFalse(hasActedInRound[2], "P3 should have hasActedInRound reset");

        assertNotEquals(3, game.getCurrentPlayerIndex(), "Current player should move from P4 (next after raiser)");
        assertTrue(game.needsPlayerAction(), "Game should still need player action");
    }

    @Test
    @DisplayName("Test player action - CHECK (invalid)")
    void testPlayerActionInvalidCheck() {
        startBettingRoundWithPlayersReady(); // P4 is UTG, currentBet is 100 (BB)

        Player p4 = game.getPlayers().get(game.getCurrentPlayerIndex()); // P4
        int p4InitialChips = p4.getChips();
        int initialPot = game.getPot();

        game.performAction(PokerGame.PlayerAction.CHECK, 0); // Invalid because currentBet > P4's currentBet

        assertEquals(p4InitialChips, p4.getChips(), "P4 chips should NOT change for invalid check");
        assertEquals(initialPot, game.getPot(), "Pot should NOT change for invalid check");

        // hasActedInRound should remain false if action was invalid (not marked as acted)
        boolean[] hasActedInRound = getHasActedInRoundArray();
        assertFalse(hasActedInRound[3], "P4 should NOT have acted for invalid check"); // P4 is index 3
        assertEquals(3, game.getCurrentPlayerIndex(), "Current player should NOT move for invalid check"); // Still P4
        assertTrue(game.needsPlayerAction(), "Game should still need player action");
    }

    @Test
    @DisplayName("Test player action - RAISE (invalid amount)")
    void testPlayerActionInvalidRaiseAmount() {
        startBettingRoundWithPlayersReady(); // P4 is UTG, currentBet is 100 (BB)

        Player p4 = game.getPlayers().get(game.getCurrentPlayerIndex()); // P4
        int p4InitialChips = p4.getChips();
        int initialPot = game.getPot();

        // Attempt to raise to less than current bet
        game.performAction(PokerGame.PlayerAction.RAISE, 50);

        assertEquals(p4InitialChips, p4.getChips(), "P4 chips should NOT change for invalid raise");
        assertEquals(initialPot, game.getPot(), "Pot should NOT change for invalid raise");

        // hasActedInRound should remain false if action was invalid (not marked as acted)
        boolean[] hasActedInRound = getHasActedInRoundArray();
        assertFalse(hasActedInRound[3], "P4 should NOT have acted for invalid raise"); // P4 is index 3
        assertEquals(3, game.getCurrentPlayerIndex(), "Current player should NOT move for invalid raise"); // Still P4
        assertTrue(game.needsPlayerAction(), "Game should still need player action");
    }

    @Test
    @DisplayName("Test `isBettingRoundComplete` - all active players call/check")
    void testIsBettingRoundCompleteAllCallOrCheck() {
        // Setup a scenario where all active players will call/check
        startBettingRoundWithPlayersReady(); // P4 is UTG, currentBet is 100 (BB)

        // P4 calls (currentBet is 100)
        game.performAction(PokerGame.PlayerAction.CALL, 0);
        // Current player is P1 (Dealer, index 0)
        game.performAction(PokerGame.PlayerAction.CALL, 0);
        // Current player is P2 (SB, index 1)
        game.performAction(PokerGame.PlayerAction.CALL, 0);
        // Current player is P3 (BB, index 2)
        game.performAction(PokerGame.PlayerAction.CHECK, 0); // P3 checks (already 100 in)

        // All players have now acted and matched the bet (or checked at 0)
        assertTrue(game.isBettingRoundComplete(), "Betting round should be complete");
        assertFalse(game.needsPlayerAction(), "Needs player action should be false after round completion");
    }

    @Test
    @DisplayName("Test `isBettingRoundComplete` - raise resets flags and round continues")
    void testIsBettingRoundCompleteRaiseResets() {
        startBettingRoundWithPlayersReady(); // P4 is UTG, currentBet is 100 (BB)

        // P4 raises to 250
        game.performAction(PokerGame.PlayerAction.RAISE, 250); // P4 has acted, others' flags reset
        assertFalse(game.isBettingRoundComplete(), "Betting round should NOT be complete after a raise");
        assertTrue(game.needsPlayerAction(), "Needs player action should be true after a raise");
        assertNotEquals(3, game.getCurrentPlayerIndex(), "Current player should move from P4 after P4's raise");

        // P1 calls 250
        game.performAction(PokerGame.PlayerAction.CALL, 0); // P1 has acted
        assertFalse(game.isBettingRoundComplete(), "Betting round should NOT be complete yet");

        // P2 calls 250
        game.performAction(PokerGame.PlayerAction.CALL, 0); // P2 has acted
        assertFalse(game.isBettingRoundComplete(), "Betting round should NOT be complete yet");

        // P3 calls 250 (Big Blind)
        game.performAction(PokerGame.PlayerAction.CALL, 0); // P3 has acted. Now all active players have matched the bet.
        assertTrue(game.isBettingRoundComplete(), "Betting round should be complete after everyone calls the raise");
        assertFalse(game.needsPlayerAction(), "Needs player action should be false");
    }

    @Test
    @DisplayName("Test `isBettingRoundComplete` - all but one fold")
    void testIsBettingRoundCompleteAllFold() {
        startBettingRoundWithPlayersReady(); // P4 is UTG, currentBet is 100 (BB)

        game.performAction(PokerGame.PlayerAction.FOLD, 0); // P4 folds
        assertFalse(game.isBettingRoundComplete(), "Not complete yet, P1-P3 still active");

        game.performAction(PokerGame.PlayerAction.FOLD, 0); // P1 folds
        assertFalse(game.isBettingRoundComplete(), "Not complete yet, P2-P3 still active");

        game.performAction(PokerGame.PlayerAction.FOLD, 0); // P2 folds
        assertTrue(game.isBettingRoundComplete(), "Round should be complete as only P3 remains active");
        // hasWinnerByFold is checked by isBettingRoundComplete, but it's also called by update.
        // The game state will transition to showdown via update, not directly here.
        // We can assert on the side effect of hasWinnerByFold (chips awarded, pot reset).
        Player p3 = game.getPlayers().get(2);
        // This assertion checks the side effect of hasWinnerByFold being called
        // by isBettingRoundComplete when it returns true.
        assertEquals(1000 - 100 + 150, p3.getChips(), "P3 should win the pot (150 from initial blinds + P4,P1,P2's initial blind/bet)");
        assertEquals(0, game.getPot(), "Pot should be zero after winner by fold");
        assertFalse(game.needsPlayerAction(), "No action needed, round complete");
    }

    @Test
    @DisplayName("Test game progression through betting rounds")
    void testGameProgression() {
        // Mock deck for all stages
        when(mockDeck.drawCard())
            .thenReturn(c(Card.Suit.HEARTS, Card.Rank.ACE), c(Card.Suit.HEARTS, Card.Rank.KING), c(Card.Suit.HEARTS, Card.Rank.QUEEN), c(Card.Suit.HEARTS, Card.Rank.JACK)) // P1-P4 hole cards (1st card)
            .thenReturn(c(Card.Suit.DIAMONDS, Card.Rank.ACE), c(Card.Suit.DIAMONDS, Card.Rank.KING), c(Card.Suit.DIAMONDS, Card.Rank.QUEEN), c(Card.Suit.DIAMONDS, Card.Rank.JACK)) // P1-P4 hole cards (2nd card)
            .thenReturn(c(Card.Suit.CLUBS, Card.Rank.TWO)) // Burn for Flop
            .thenReturn(c(Card.Suit.CLUBS, Card.Rank.THREE), c(Card.Suit.CLUBS, Card.Rank.FOUR), c(Card.Suit.CLUBS, Card.Rank.FIVE)) // Flop cards
            .thenReturn(c(Card.Suit.SPADES, Card.Rank.SIX)) // Burn for Turn
            .thenReturn(c(Card.Suit.SPADES, Card.Rank.SEVEN)) // Turn card
            .thenReturn(c(Card.Suit.HEARTS, Card.Rank.EIGHT)) // Burn for River
            .thenReturn(c(Card.Suit.HEARTS, Card.Rank.NINE)); // River card

        // Start hand (pre-flop)
        game.startNewHand(); // P4 is UTG
        assertEquals(PokerGame.GameState.BETTING_PRE_FLOP, PokerGame.getGameState());
        assertEquals(0, game.getCommunityCards().size());

        // Simulate pre-flop completion (all call)
        game.performAction(PokerGame.PlayerAction.CALL, 0); // P4 calls 100
        game.performAction(PokerGame.PlayerAction.CALL, 0); // P1 calls 100
        game.performAction(PokerGame.PlayerAction.CALL, 0); // P2 calls 100 (50 from SB + 50 call)
        game.performAction(PokerGame.PlayerAction.CHECK, 0); // P3 checks (already 100 in)

        // Update to progress to Flop
        game.update(0.1f);
        assertEquals(PokerGame.GameState.BETTING_FLOP, PokerGame.getGameState());
        assertEquals(3, game.getCommunityCards().size());
        assertEquals(0, game.getCurrentBet(), "Current bet should reset for new round");
        assertEquals(0, game.getPlayers().get(0).getCurrentBet(), "Player current bets should reset for new round");
        assertTrue(game.needsPlayerAction(), "Needs player action should be true for new round");
        assertEquals(1, game.getCurrentPlayerIndex(), "Current player should be P2 (SB) for new round"); // After dealer (P1), SB is P2

        // Simulate flop completion (all check)
        game.performAction(PokerGame.PlayerAction.CHECK, 0); // P2 checks
        game.performAction(PokerGame.PlayerAction.CHECK, 0); // P3 checks
        game.performAction(PokerGame.PlayerAction.CHECK, 0); // P4 checks
        game.performAction(PokerGame.PlayerAction.CHECK, 0); // P1 checks

        // Update to progress to Turn
        game.update(0.1f);
        assertEquals(PokerGame.GameState.BETTING_TURN, PokerGame.getGameState());
        assertEquals(4, game.getCommunityCards().size());

        // Simulate turn completion (all check)
        game.performAction(PokerGame.PlayerAction.CHECK, 0); // P2 checks
        game.performAction(PokerGame.PlayerAction.CHECK, 0); // P3 checks
        game.performAction(PokerGame.PlayerAction.CHECK, 0); // P4 checks
        game.performAction(PokerGame.PlayerAction.CHECK, 0); // P1 checks

        // Update to progress to River
        game.update(0.1f);
        assertEquals(PokerGame.GameState.BETTING_RIVER, PokerGame.getGameState());
        assertEquals(5, game.getCommunityCards().size());

        // Simulate river completion (all check)
        game.performAction(PokerGame.PlayerAction.CHECK, 0); // P2 checks
        game.performAction(PokerGame.PlayerAction.CHECK, 0); // P3 checks
        game.performAction(PokerGame.PlayerAction.CHECK, 0); // P4 checks
        game.performAction(PokerGame.PlayerAction.CHECK, 0); // P1 checks

        // Update to progress to Showdown
        game.update(0.1f);
        assertEquals(PokerGame.GameState.SHOWDOWN, PokerGame.getGameState());
        assertFalse(game.needsPlayerAction(), "Needs player action should be false in showdown");

        // Simulate showdown timer expiring
        // This will trigger distributeWinnings() and startNewHand() internally
        game.update(10.0f); // Advance timer past SHOWDOWN_DURATION
        // After this, a new hand should start
        assertEquals(PokerGame.GameState.BETTING_PRE_FLOP, PokerGame.getGameState(), "Game should start new hand after showdown duration");
        assertEquals(0, game.getCommunityCards().size(), "Community cards should be cleared for new hand");
        assertTrue(game.getPot() > 0, "Pot should have blinds for new hand");
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

    @Test
    @DisplayName("Test `distributeWinnings` - indirect testing via `update` and `goToShowdown`")
    void testDistributeWinningsIndirect() {
        // Since distributeWinnings is parameter-less in the provided PokerGame.java,
        // we must trigger it via the game loop (update calls goToShowdown, which then calls distributeWinnings after timer).

        // Setup a scenario where P1 will be the winner at showdown.
        // P1 will have a higher hand than others.
        // Set pot directly for testing distribution.
        setGamePot(500); // Set pot to be distributed

        // Ensure P1 has a winning hand
        // P1: Full House (Aces full of Kings)
        // Others: Low hands
        List<Card> community = Arrays.asList(
            c(Card.Suit.HEARTS, Card.Rank.ACE),
            c(Card.Suit.DIAMONDS, Card.Rank.ACE),
            c(Card.Suit.CLUBS, Card.Rank.KING),
            c(Card.Suit.SPADES, Card.Rank.TWO),
            c(Card.Suit.DIAMONDS, Card.Rank.THREE)
        );
        game.getCommunityCards().addAll(community);

        game.getPlayers().get(0).receiveCard(c(Card.Suit.SPADES, Card.Rank.ACE)); // P1: Three Aces
        game.getPlayers().get(0).receiveCard(c(Card.Suit.HEARTS, Card.Rank.KING)); // P1: Two Kings (Full House)

        game.getPlayers().get(1).receiveCard(c(Card.Suit.CLUBS, Card.Rank.FIVE)); // P2: Random low cards
        game.getPlayers().get(1).receiveCard(c(Card.Suit.DIAMONDS, Card.Rank.SIX));

        game.getPlayers().get(2).receiveCard(c(Card.Suit.HEARTS, Card.Rank.SEVEN)); // P3: Random low cards
        game.getPlayers().get(2).receiveCard(c(Card.Suit.CLUBS, Card.Rank.EIGHT));

        game.getPlayers().get(3).receiveCard(c(Card.Suit.SPADES, Card.Rank.NINE)); // P4: Random low cards
        game.getPlayers().get(3).receiveCard(c(Card.Suit.HEARTS, Card.Rank.TEN));

        // Ensure all players are active for showdown
        for (Player p : game.getPlayers()) {
            p.setActive(true);
        }

        Player p1 = game.getPlayers().get(0);
        int p1InitialChips = p1.getChips();

        // Simulate game state progression to SHOWDOWN
        game.goToShowdown();
        assertEquals(PokerGame.GameState.SHOWDOWN, PokerGame.getGameState(), "Game state should be SHOWDOWN");

        // Simulate showdown timer expiring, which triggers distributeWinnings()
        game.update(10.0f); // Advance timer past SHOWDOWN_DURATION

        // Verify chips for the winner
        assertEquals(p1InitialChips + 500, p1.getChips(), "P1 should get all 500 chips after showdown");
        assertEquals(0, game.getPot(), "Pot should be 0 after distribution");

        // Now, test chop pot indirectly
        setGamePot(300);
        game.getCommunityCards().clear(); // Reset community cards for new scenario
        game.getPlayers().forEach(Player::clearHand); // Clear hands
        game.getPlayers().forEach(p -> p.addChips(1000 - p.getChips())); // Reset chips for simplicity
        game.getPlayers().forEach(p -> p.setActive(true)); // Reset active state

        // P1 & P2 will have identical winning hands (straight A-10)
        List<Card> chopCommunity = Arrays.asList(
            c(Card.Suit.CLUBS, Card.Rank.QUEEN),
            c(Card.Suit.SPADES, Card.Rank.JACK),
            c(Card.Suit.HEARTS, Card.Rank.TEN),
            c(Card.Suit.DIAMONDS, Card.Rank.NINE),
            c(Card.Suit.SPADES, Card.Rank.FIVE) // Kicker doesn't matter here
        );
        game.getCommunityCards().addAll(chopCommunity);

        game.getPlayers().get(0).receiveCard(c(Card.Suit.CLUBS, Card.Rank.ACE)); // P1: Straight (A-K-Q-J-10)
        game.getPlayers().get(0).receiveCard(c(Card.Suit.DIAMONDS, Card.Rank.KING));

        game.getPlayers().get(1).receiveCard(c(Card.Suit.HEARTS, Card.Rank.ACE)); // P2: Straight (A-K-Q-J-10)
        game.getPlayers().get(1).receiveCard(c(Card.Suit.CLUBS, Card.Rank.KING));

        game.getPlayers().get(2).receiveCard(c(Card.Suit.HEARTS, Card.Rank.TWO)); // P3: Low hand
        game.getPlayers().get(2).receiveCard(c(Card.Suit.DIAMONDS, Card.Rank.THREE));

        game.getPlayers().get(3).receiveCard(c(Card.Suit.CLUBS, Card.Rank.FOUR)); // P4: Low hand
        game.getPlayers().get(3).receiveCard(c(Card.Suit.SPADES, Card.Rank.SIX));


        p1 = game.getPlayers().get(0);
        Player p2 = game.getPlayers().get(1);
        Player p3 = game.getPlayers().get(2);
        int p1InitialChipsChop = p1.getChips();
        int p2InitialChipsChop = p2.getChips();
        int p3InitialChipsChop = p3.getChips();


        game.goToShowdown();
        game.update(10.0f); // Trigger distributeWinnings again

        assertEquals(p1InitialChipsChop + 150, p1.getChips(), "P1 should get 150 chips from chop pot");
        assertEquals(p2InitialChipsChop + 150, p2.getChips(), "P2 should get 150 chips from chop pot");
        // Ensure other players didn't get chips
        assertEquals(p3InitialChipsChop, p3.getChips(), "P3 should not get chips");
        assertEquals(0, game.getPot(), "Pot should be 0 after chop distribution");


        // Test with remainder indirectly
        setGamePot(301);
        game.getCommunityCards().clear();
        game.getPlayers().forEach(Player::clearHand);
        game.getPlayers().forEach(p -> p.addChips(1000 - p.getChips())); // Reset chips
        game.getPlayers().forEach(p -> p.setActive(true));

        // P1, P2, P3 will have identical winning hands (straight 9-5)
        List<Card> remainderCommunity = Arrays.asList(
            c(Card.Suit.CLUBS, Card.Rank.EIGHT),
            c(Card.Suit.SPADES, Card.Rank.SEVEN),
            c(Card.Suit.HEARTS, Card.Rank.SIX),
            c(Card.Suit.DIAMONDS, Card.Rank.FIVE),
            c(Card.Suit.SPADES, Card.Rank.TWO) // Kicker doesn't matter here
        );
        game.getCommunityCards().addAll(remainderCommunity);

        game.getPlayers().get(0).receiveCard(c(Card.Suit.CLUBS, Card.Rank.NINE)); // P1: Straight
        game.getPlayers().get(0).receiveCard(c(Card.Suit.DIAMONDS, Card.Rank.FOUR));

        game.getPlayers().get(1).receiveCard(c(Card.Suit.HEARTS, Card.Rank.NINE)); // P2: Straight
        game.getPlayers().get(1).receiveCard(c(Card.Suit.CLUBS, Card.Rank.FOUR));

        game.getPlayers().get(2).receiveCard(c(Card.Suit.SPADES, Card.Rank.NINE)); // P3: Straight
        game.getPlayers().get(2).receiveCard(c(Card.Suit.HEARTS, Card.Rank.FOUR));

        game.getPlayers().get(3).receiveCard(c(Card.Suit.DIAMONDS, Card.Rank.TEN)); // P4: Low hand
        game.getPlayers().get(3).receiveCard(c(Card.Suit.CLUBS, Card.Rank.THREE));


        p1 = game.getPlayers().get(0);
        p2 = game.getPlayers().get(1);
        p3 = game.getPlayers().get(2);
        int p1InitialChipsRemainder = p1.getChips();
        int p2InitialChipsRemainder = p2.getChips();
        int p3InitialChipsRemainder = p3.getChips();

        game.goToShowdown();
        game.update(10.0f); // Trigger distributeWinnings again

        assertEquals(p1InitialChipsRemainder + 101, p1.getChips(), "P1 should get 101 chips (including remainder)");
        assertEquals(p2InitialChipsRemainder + 100, p2.getChips(), "P2 should get 100 chips");
        assertEquals(p3InitialChipsRemainder + 100, p3.getChips(), "P3 should get 100 chips");
        assertEquals(0, game.getPot(), "Pot should be 0 after remainder distribution");
    }


    // This helper method is crucial for starting a betting round in a consistent state for tests.
    // It will ensure players have cards, blinds are posted, and it's UTG's turn to act.
    private void startBettingRoundWithPlayersReady() {
        // Mock deck behavior for dealing initial cards (2 per player)
        when(mockDeck.drawCard())
            .thenReturn(c(Card.Suit.HEARTS, Card.Rank.ACE)) // P1 Card 1
            .thenReturn(c(Card.Suit.HEARTS, Card.Rank.KING)) // P2 Card 1
            .thenReturn(c(Card.Suit.HEARTS, Card.Rank.QUEEN)) // P3 Card 1
            .thenReturn(c(Card.Suit.HEARTS, Card.Rank.JACK)) // P4 Card 1
            .thenReturn(c(Card.Suit.DIAMONDS, Card.Rank.ACE)) // P1 Card 2
            .thenReturn(c(Card.Suit.DIAMONDS, Card.Rank.KING)) // P2 Card 2
            .thenReturn(c(Card.Suit.DIAMONDS, Card.Rank.QUEEN)) // P3 Card 2
            .thenReturn(c(Card.Suit.DIAMONDS, Card.Rank.JACK)); // P4 Card 2
        doNothing().when(mockDeck).reset(); // Mock reset call

        // Ensure dealer position is predictable for testing (set using reflection)
        setStaticDealerPosition(game.getPlayers().size() - 1); // Set dealer to P4 initially, so P1 becomes dealer after rotation

        game.startNewHand(); // This sets up the blinds, currentBet, currentPlayerIndex etc.
        assertEquals(PokerGame.GameState.BETTING_PRE_FLOP, PokerGame.getGameState());
        assertEquals(100, game.getCurrentBet());
        assertEquals(3, game.getCurrentPlayerIndex()); // P4 (index 3) is UTG
        assertTrue(game.needsPlayerAction());
    }

}
