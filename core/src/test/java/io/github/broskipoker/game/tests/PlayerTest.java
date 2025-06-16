package io.github.broskipoker.game.tests;

import io.github.broskipoker.game.Card;
import io.github.broskipoker.game.Player;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.ArrayList;

public class PlayerTest {

    @Test
    public void testPlayerInitialization() {
        Player player = new Player("Alice", 500);
        assertEquals("Alice", player.getName(), "Player name should be initialized correctly.");
        assertEquals(500, player.getChips(), "Player chips should be initialized correctly.");
        assertTrue(player.getHoleCards().isEmpty(), "Hole cards should be empty on initialization.");
        assertTrue(player.isActive(), "Player should be active on initialization.");
        assertEquals(0, player.getCurrentBet(), "Current bet should be 0 on initialization.");
    }

    @Test
    public void testPlayerBetting() {
        Player player = new Player("Test Player", 1000);
        assertEquals(1000, player.getChips(), "Player should start with 1000 chips");

        int betAmount1 = 300;
        int actualBet1 = player.bet(betAmount1);
        assertEquals(betAmount1, actualBet1, "Bet amount should be 300 if chips are sufficient.");
        assertEquals(700, player.getChips(), "Player should have 700 chips after betting 300.");
        assertEquals(300, player.getCurrentBet(), "Current bet should be 300 after first bet.");

        int betAmount2 = 500;
        int actualBet2 = player.bet(betAmount2);
        assertEquals(betAmount2, actualBet2, "Bet amount should be 500 if chips are sufficient for the second bet.");
        assertEquals(200, player.getChips(), "Player should have 200 chips after betting another 500.");
        assertEquals(800, player.getCurrentBet(), "Current bet should be 800 after two bets.");

        // Test betting more than available chips (all-in)
        Player playerAllIn = new Player("All-in Player", 150);
        int betAmountAllIn = 200;
        int actualBetAllIn = playerAllIn.bet(betAmountAllIn);
        assertEquals(150, actualBetAllIn, "Bet amount should be limited by available chips (all-in).");
        assertEquals(0, playerAllIn.getChips(), "Player should have 0 chips after going all-in.");
        assertEquals(150, playerAllIn.getCurrentBet(), "Current bet should reflect the all-in amount.");

        // Test betting with 0 chips
        Player playerNoChips = new Player("No Chips Player", 0);
        int betAmountZero = 100;
        int actualBetZero = playerNoChips.bet(betAmountZero);
        assertEquals(0, actualBetZero, "Bet amount should be 0 if player has no chips.");
        assertEquals(0, playerNoChips.getChips(), "Player should still have 0 chips.");
        assertEquals(0, playerNoChips.getCurrentBet(), "Current bet should remain 0.");
    }

    @Test
    public void testReceiveAndClearCards() {
        Player player = new Player("Card Player", 1000);
        assertTrue(player.getHoleCards().isEmpty(), "Hand should be empty initially.");

        Card card1 = new Card(Card.Suit.HEARTS, Card.Rank.ACE);
        player.receiveCard(card1);
        assertEquals(1, player.getHoleCards().size(), "Player should have 1 card after receiving one.");
        assertTrue(player.getHoleCards().contains(card1), "Received card should be in the hand.");

        Card card2 = new Card(Card.Suit.CLUBS, Card.Rank.KING);
        player.receiveCard(card2);
        assertEquals(2, player.getHoleCards().size(), "Player should have 2 cards after receiving another.");
        assertTrue(player.getHoleCards().contains(card2), "Second received card should be in the hand.");

        player.clearHand();
        assertTrue(player.getHoleCards().isEmpty(), "Hand should be empty after clearing.");
    }

    @Test
    public void testAddChips() {
        Player player = new Player("Chip Adder", 500);
        player.addChips(200);
        assertEquals(700, player.getChips(), "Chips should increase after adding.");

        player.addChips(0);
        assertEquals(700, player.getChips(), "Adding 0 chips should not change chip count.");

        player.addChips(-100); // Test adding negative chips (though usually not desired in poker logic)
        assertEquals(600, player.getChips(), "Adding negative chips should decrease chip count.");
    }

    @Test
    public void testPlayerActiveStatus() {
        Player player = new Player("Active Player", 500);
        assertTrue(player.isActive(), "Player should be active by default.");

        player.setActive(false);
        assertFalse(player.isActive(), "Player should be inactive after setting to false.");

        player.setActive(true);
        assertTrue(player.isActive(), "Player should be active after setting to true.");
    }

    @Test
    public void testResetBet() {
        Player player = new Player("Reset Player", 1000);
        player.bet(200);
        assertEquals(200, player.getCurrentBet(), "Current bet should be 200.");

        player.resetBet();
        assertEquals(0, player.getCurrentBet(), "Current bet should be 0 after resetting.");

        player.bet(50);
        assertEquals(50, player.getCurrentBet(), "Current bet should be 50 after new bet.");
        player.resetBet();
        assertEquals(0, player.getCurrentBet(), "Current bet should be 0 after second reset.");
    }

    @Test
    public void testCopyConstructor() {
        Player originalPlayer = new Player("Original", 1000);
        originalPlayer.receiveCard(new Card(Card.Suit.DIAMONDS, Card.Rank.TEN));
        originalPlayer.bet(200);
        originalPlayer.setActive(false);

        Player copiedPlayer = new Player(originalPlayer);

        // Verify that all properties are copied correctly
        assertEquals(originalPlayer.getName(), copiedPlayer.getName(), "Copied player name should match original.");
        assertEquals(originalPlayer.getChips(), copiedPlayer.getChips(), "Copied player chips should match original.");
        assertEquals(originalPlayer.isActive(), copiedPlayer.isActive(), "Copied player active status should match original.");
        assertEquals(originalPlayer.getCurrentBet(), copiedPlayer.getCurrentBet(), "Copied player current bet should match original.");

        // Verify that hole cards list is a deep copy (changes to original's list don't affect copy's)
        assertEquals(originalPlayer.getHoleCards().size(), copiedPlayer.getHoleCards().size(), "Copied hand size should match original.");
        assertEquals(originalPlayer.getHoleCards().get(0), copiedPlayer.getHoleCards().get(0), "Copied card should match original card.");

        originalPlayer.receiveCard(new Card(Card.Suit.SPADES, Card.Rank.QUEEN));
        assertEquals(2, originalPlayer.getHoleCards().size(), "Original player should have 2 cards.");
        assertEquals(1, copiedPlayer.getHoleCards().size(), "Copied player should still have 1 card (deep copy check).");

        // Verify that changing copy's properties doesn't affect original
        copiedPlayer.addChips(500);
        assertNotEquals(originalPlayer.getChips(), copiedPlayer.getChips(), "Changing copy's chips should not affect original.");

        copiedPlayer.setActive(true);
        assertNotEquals(originalPlayer.isActive(), copiedPlayer.isActive(), "Changing copy's active status should not affect original.");
    }
}
