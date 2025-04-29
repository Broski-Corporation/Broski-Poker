package io.github.broskipoker.game.tests;

import io.github.broskipoker.game.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

public class PokerGameTest {

    @Test
    @DisplayName("Test deck initialization and operations")
    public void testDeck() {
        Deck deck = new Deck();
        assertEquals(52, deck.cardsRemaining(), "New deck should have 52 cards");

        Card card = deck.drawCard();
        assertNotNull(card, "Should be able to draw a card");
        assertEquals(51, deck.cardsRemaining(), "Deck should have 51 cards after drawing");

        deck.reset();
        assertEquals(52, deck.cardsRemaining(), "Reset deck should have 52 cards");
    }

    @Test
    @DisplayName("Test player actions and state")
    public void testPlayer() {
        Player player = new Player("Test Player", 1000);
        assertEquals(1000, player.getChips(), "Player should start with 1000 chips");

        int bet = player.bet(300);
        assertEquals(300, bet, "Bet amount should be 300");
        assertEquals(700, player.getChips(), "Player should have 700 chips after betting");

        player.clearHand();
        assertTrue(player.getHoleCards().isEmpty(), "Hand should be empty after clearing");

        Card card1 = new Card(Card.Suit.HEARTS, Card.Rank.ACE);
        player.receiveCard(card1);
        assertEquals(1, player.getHoleCards().size(), "Player should have 1 card");
    }

    @Test
    @DisplayName("Test card properties")
    public void testCardBasics() {
        Card aceHearts = new Card(Card.Suit.HEARTS, Card.Rank.ACE);
        assertEquals(Card.Suit.HEARTS, aceHearts.getSuit(), "Card suit should be hearts");
        assertEquals(Card.Rank.ACE, aceHearts.getRank(), "Card rank should be ace");
    }

    @Test
    @DisplayName("Test poker hand evaluation")
    public void testPokerHandEvaluation() {
        // Test royal flush
        List<Card> royalHole = Arrays.asList(
            new Card(Card.Suit.HEARTS, Card.Rank.ACE),
            new Card(Card.Suit.HEARTS, Card.Rank.KING)
        );
        List<Card> royalCommunity = Arrays.asList(
            new Card(Card.Suit.HEARTS, Card.Rank.QUEEN),
            new Card(Card.Suit.HEARTS, Card.Rank.JACK),
            new Card(Card.Suit.HEARTS, Card.Rank.TEN),
            new Card(Card.Suit.CLUBS, Card.Rank.TWO),
            new Card(Card.Suit.DIAMONDS, Card.Rank.THREE)
        );
        PokerHand royalFlush = new PokerHand(royalHole, royalCommunity);
        assertEquals(PokerHand.HandRank.ROYAL_FLUSH, royalFlush.getRank(), "Should be royal flush");

        // Test straight flush
        List<Card> straightFlushHole = Arrays.asList(
            new Card(Card.Suit.CLUBS, Card.Rank.NINE),
            new Card(Card.Suit.CLUBS, Card.Rank.EIGHT)
        );
        List<Card> straightFlushCommunity = Arrays.asList(
            new Card(Card.Suit.CLUBS, Card.Rank.SEVEN),
            new Card(Card.Suit.CLUBS, Card.Rank.SIX),
            new Card(Card.Suit.CLUBS, Card.Rank.FIVE),
            new Card(Card.Suit.HEARTS, Card.Rank.TWO),
            new Card(Card.Suit.DIAMONDS, Card.Rank.THREE)
        );
        PokerHand straightFlush = new PokerHand(straightFlushHole, straightFlushCommunity);
        assertEquals(PokerHand.HandRank.STRAIGHT_FLUSH, straightFlush.getRank(), "Should be straight flush");

        // Test four of a kind
        List<Card> fourKindHole = Arrays.asList(
            new Card(Card.Suit.HEARTS, Card.Rank.ACE),
            new Card(Card.Suit.DIAMONDS, Card.Rank.ACE)
        );
        List<Card> fourKindCommunity = Arrays.asList(
            new Card(Card.Suit.CLUBS, Card.Rank.ACE),
            new Card(Card.Suit.SPADES, Card.Rank.ACE),
            new Card(Card.Suit.HEARTS, Card.Rank.TEN),
            new Card(Card.Suit.CLUBS, Card.Rank.TWO),
            new Card(Card.Suit.DIAMONDS, Card.Rank.THREE)
        );
        PokerHand fourOfAKind = new PokerHand(fourKindHole, fourKindCommunity);
        assertEquals(PokerHand.HandRank.FOUR_OF_A_KIND, fourOfAKind.getRank(), "Should be four of a kind");

        // Additional hand type tests (similar structure)
    }

    @Test
    @DisplayName("Test hand comparison")
    public void testHandComparison() {
        // Royal flush vs straight flush
        List<Card> royalHole = Arrays.asList(
            new Card(Card.Suit.HEARTS, Card.Rank.ACE),
            new Card(Card.Suit.HEARTS, Card.Rank.KING)
        );
        List<Card> royalCommunity = Arrays.asList(
            new Card(Card.Suit.HEARTS, Card.Rank.QUEEN),
            new Card(Card.Suit.HEARTS, Card.Rank.JACK),
            new Card(Card.Suit.HEARTS, Card.Rank.TEN),
            new Card(Card.Suit.CLUBS, Card.Rank.TWO),
            new Card(Card.Suit.DIAMONDS, Card.Rank.THREE)
        );
        PokerHand royalFlush = new PokerHand(royalHole, royalCommunity);

        List<Card> straightFlushHole = Arrays.asList(
            new Card(Card.Suit.CLUBS, Card.Rank.NINE),
            new Card(Card.Suit.CLUBS, Card.Rank.EIGHT)
        );
        List<Card> straightFlushCommunity = Arrays.asList(
            new Card(Card.Suit.CLUBS, Card.Rank.SEVEN),
            new Card(Card.Suit.CLUBS, Card.Rank.SIX),
            new Card(Card.Suit.CLUBS, Card.Rank.FIVE),
            new Card(Card.Suit.HEARTS, Card.Rank.TWO),
            new Card(Card.Suit.DIAMONDS, Card.Rank.THREE)
        );
        PokerHand straightFlush = new PokerHand(straightFlushHole, straightFlushCommunity);

        assertTrue(royalFlush.compareTo(straightFlush) > 0, "Royal flush should beat straight flush");

        // Pair comparison test
        List<Card> pairAcesHole = Arrays.asList(
            new Card(Card.Suit.HEARTS, Card.Rank.ACE),
            new Card(Card.Suit.DIAMONDS, Card.Rank.ACE)
        );
        List<Card> pairKingsHole = Arrays.asList(
            new Card(Card.Suit.HEARTS, Card.Rank.KING),
            new Card(Card.Suit.DIAMONDS, Card.Rank.KING)
        );
        List<Card> commonCommunity = Arrays.asList(
            new Card(Card.Suit.CLUBS, Card.Rank.QUEEN),
            new Card(Card.Suit.SPADES, Card.Rank.TEN),
            new Card(Card.Suit.HEARTS, Card.Rank.FIVE),
            new Card(Card.Suit.CLUBS, Card.Rank.THREE),
            new Card(Card.Suit.DIAMONDS, Card.Rank.TWO)
        );

        PokerHand pairAces = new PokerHand(pairAcesHole, commonCommunity);
        PokerHand pairKings = new PokerHand(pairKingsHole, commonCommunity);

        assertTrue(pairAces.compareTo(pairKings) > 0, "Pair of aces should beat pair of kings");
    }

    @Test
    @DisplayName("Test game initialization and state progression")
    public void testGameBasics() {
        PokerGame game = new PokerGame(10, 20); // Small blind 10, big blind 20
        game.addPlayer("Player1", 1000);
        game.addPlayer("Player2", 1000);
        game.addPlayer("Player3", 1000);

        game.startNewHand();
        assertEquals(PokerGame.GameState.BETTING_PRE_FLOP, game.getGameState(), "Game should start in pre-flop state");
        assertEquals(30, game.getPot(), "Pot should have blinds");
        assertEquals(2, game.getPlayers().get(0).getHoleCards().size(), "Player should have 2 hole cards");

        // Simulate all players calling
        while (game.needsPlayerAction()) {
            game.performAction(PokerGame.PlayerAction.CALL, game.getCurrentBet());
        }

        game.update(0.1f); // Advance game state
        assertEquals(PokerGame.GameState.BETTING_FLOP, game.getGameState(), "Game should advance to flop");
        assertEquals(3, game.getCommunityCards().size(), "Should have 3 community cards");
    }

    @Test
    @DisplayName("Test betting actions and pot management")
    public void testBettingLogic() {
        PokerGame game = new PokerGame(10, 20);
        game.addPlayer("Player1", 1000);
        game.addPlayer("Player2", 1000);
        game.addPlayer("Player3", 1000);

        game.startNewHand();
        int initialPot = game.getPot();

        // First player raises
        Player currentPlayer = game.getCurrentPlayer();
        int initialChips = currentPlayer.getChips();
        game.performAction(PokerGame.PlayerAction.RAISE, 50);

        assertEquals(initialChips - (50 - currentPlayer.getCurrentBet()), currentPlayer.getChips(),
               "Player chips should be reduced by raise amount");
        assertEquals(50, game.getCurrentBet(), "Current bet should be updated");
        assertTrue(game.getPot() > initialPot, "Pot should increase after raise");

        // Next player folds
        Player nextPlayer = game.getCurrentPlayer();
        game.performAction(PokerGame.PlayerAction.FOLD, 0);
        assertFalse(nextPlayer.isActive(), "Player should be inactive after folding");
    }
}
