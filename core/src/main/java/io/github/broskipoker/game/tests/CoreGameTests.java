package io.github.broskipoker.game.tests;

import io.github.broskipoker.game.*;

import java.util.Arrays;
import java.util.List;

public class CoreGameTests {

    public static void main(String[] args) {
        runConsoleTests();
    }

    public static void runConsoleTests() {
        System.out.println("=== Running Basic Poker Game Tests ===");

        testDeck();
        testPlayer();
        testCardBasics();

        // New tests
        testPokerHandEvaluation();
        testHandComparison();
        testGameBasics();
        testBettingLogic();

        System.out.println("\nAll console tests completed!");
    }

    private static void testDeck() {
        // Existing method
        System.out.println("\nTesting Deck...");

        Deck deck = new Deck();
        assert deck.cardsRemaining() == 52 : "New deck should have 52 cards";
        System.out.println("✓ New deck has 52 cards");

        Card card = deck.drawCard();
        assert card != null : "Should be able to draw a card";
        assert deck.cardsRemaining() == 51 : "Deck should have 51 cards after drawing";
        System.out.println("✓ Drawing card reduces deck size to 51");

        deck.reset();
        assert deck.cardsRemaining() == 52 : "Reset deck should have 52 cards";
        System.out.println("✓ Reset deck returns to 52 cards");
    }

    private static void testPlayer() {
        // Existing method
        System.out.println("\nTesting Player...");

        Player player = new Player("Test Player", 1000);
        assert player.getChips() == 1000 : "Player should start with 1000 chips";
        System.out.println("✓ Player initialized with correct chips");

        int bet = player.bet(300);
        assert bet == 300 : "Bet amount should be 300";
        assert player.getChips() == 700 : "Player should have 700 chips after betting";
        System.out.println("✓ Player betting works correctly");

        player.clearHand();
        assert player.getHoleCards().isEmpty() : "Hand should be empty after clearing";
        System.out.println("✓ Player hand clears correctly");

        Card card1 = new Card(Card.Suit.HEARTS, Card.Rank.ACE);
        player.receiveCard(card1);
        assert player.getHoleCards().size() == 1 : "Player should have 1 card";
        System.out.println("✓ Player receives cards correctly");
    }

    private static void testCardBasics() {
        // Existing method
        System.out.println("\nTesting Card basics...");

        Card aceHearts = new Card(Card.Suit.HEARTS, Card.Rank.ACE);
        assert aceHearts.getSuit() == Card.Suit.HEARTS : "Card suit should be hearts";
        assert aceHearts.getRank() == Card.Rank.ACE : "Card rank should be ace";
        System.out.println("✓ Card creation works correctly");
    }

    private static void testPokerHandEvaluation() {
        System.out.println("\nTesting Poker Hand Evaluation...");

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
        assert royalFlush.getRank() == PokerHand.HandRank.ROYAL_FLUSH : "Should be royal flush";
        System.out.println("✓ Royal flush detection works");

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
        assert straightFlush.getRank() == PokerHand.HandRank.STRAIGHT_FLUSH : "Should be straight flush";
        System.out.println("✓ Straight flush detection works");

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
        assert fourOfAKind.getRank() == PokerHand.HandRank.FOUR_OF_A_KIND : "Should be four of a kind";
        System.out.println("✓ Four of a kind detection works");

        // Test full house
        List<Card> fullHouseHole = Arrays.asList(
            new Card(Card.Suit.HEARTS, Card.Rank.KING),
            new Card(Card.Suit.DIAMONDS, Card.Rank.KING)
        );
        List<Card> fullHouseCommunity = Arrays.asList(
            new Card(Card.Suit.CLUBS, Card.Rank.KING),
            new Card(Card.Suit.HEARTS, Card.Rank.QUEEN),
            new Card(Card.Suit.DIAMONDS, Card.Rank.QUEEN),
            new Card(Card.Suit.CLUBS, Card.Rank.TWO),
            new Card(Card.Suit.DIAMONDS, Card.Rank.THREE)
        );
        PokerHand fullHouse = new PokerHand(fullHouseHole, fullHouseCommunity);
        assert fullHouse.getRank() == PokerHand.HandRank.FULL_HOUSE : "Should be full house";
        System.out.println("✓ Full house detection works");

        // Test flush
        List<Card> flushHole = Arrays.asList(
            new Card(Card.Suit.HEARTS, Card.Rank.ACE),
            new Card(Card.Suit.HEARTS, Card.Rank.JACK)
        );
        List<Card> flushCommunity = Arrays.asList(
            new Card(Card.Suit.HEARTS, Card.Rank.NINE),
            new Card(Card.Suit.HEARTS, Card.Rank.FIVE),
            new Card(Card.Suit.HEARTS, Card.Rank.THREE),
            new Card(Card.Suit.CLUBS, Card.Rank.TWO),
            new Card(Card.Suit.DIAMONDS, Card.Rank.THREE)
        );
        PokerHand flush = new PokerHand(flushHole, flushCommunity);
        assert flush.getRank() == PokerHand.HandRank.FLUSH : "Should be flush";
        System.out.println("✓ Flush detection works");

        // Test straight
        List<Card> straightHole = Arrays.asList(
            new Card(Card.Suit.HEARTS, Card.Rank.NINE),
            new Card(Card.Suit.CLUBS, Card.Rank.EIGHT)
        );
        List<Card> straightCommunity = Arrays.asList(
            new Card(Card.Suit.DIAMONDS, Card.Rank.SEVEN),
            new Card(Card.Suit.HEARTS, Card.Rank.SIX),
            new Card(Card.Suit.CLUBS, Card.Rank.FIVE),
            new Card(Card.Suit.CLUBS, Card.Rank.TWO),
            new Card(Card.Suit.DIAMONDS, Card.Rank.THREE)
        );
        PokerHand straight = new PokerHand(straightHole, straightCommunity);
        assert straight.getRank() == PokerHand.HandRank.STRAIGHT : "Should be straight";
        System.out.println("✓ Straight detection works");

        // Test wheel straight (A-5)
        List<Card> wheelHole = Arrays.asList(
            new Card(Card.Suit.HEARTS, Card.Rank.ACE),
            new Card(Card.Suit.CLUBS, Card.Rank.TWO)
        );
        List<Card> wheelCommunity = Arrays.asList(
            new Card(Card.Suit.DIAMONDS, Card.Rank.THREE),
            new Card(Card.Suit.HEARTS, Card.Rank.FOUR),
            new Card(Card.Suit.CLUBS, Card.Rank.FIVE),
            new Card(Card.Suit.CLUBS, Card.Rank.KING),
            new Card(Card.Suit.DIAMONDS, Card.Rank.QUEEN)
        );
        PokerHand wheel = new PokerHand(wheelHole, wheelCommunity);
        assert wheel.getRank() == PokerHand.HandRank.STRAIGHT : "Should be wheel straight";
        System.out.println("✓ Wheel straight (A-5) detection works");
    }

    private static void testHandComparison() {
        System.out.println("\nTesting Hand Comparisons...");

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

        assert royalFlush.compareTo(straightFlush) > 0 : "Royal flush should beat straight flush";
        System.out.println("✓ Royal flush beats straight flush");

        // Compare same hand ranks (pair vs pair)
        List<Card> pairAcesHole = Arrays.asList(
            new Card(Card.Suit.HEARTS, Card.Rank.ACE),
            new Card(Card.Suit.DIAMONDS, Card.Rank.ACE)
        );
        List<Card> pairAcesCommunity = Arrays.asList(
            new Card(Card.Suit.CLUBS, Card.Rank.KING),
            new Card(Card.Suit.SPADES, Card.Rank.QUEEN),
            new Card(Card.Suit.HEARTS, Card.Rank.TEN),
            new Card(Card.Suit.CLUBS, Card.Rank.FIVE),
            new Card(Card.Suit.DIAMONDS, Card.Rank.THREE)
        );
        PokerHand pairAces = new PokerHand(pairAcesHole, pairAcesCommunity);

        List<Card> pairKingsHole = Arrays.asList(
            new Card(Card.Suit.HEARTS, Card.Rank.KING),
            new Card(Card.Suit.DIAMONDS, Card.Rank.KING)
        );
        List<Card> pairKingsCommunity = Arrays.asList(
            new Card(Card.Suit.CLUBS, Card.Rank.ACE),
            new Card(Card.Suit.SPADES, Card.Rank.QUEEN),
            new Card(Card.Suit.HEARTS, Card.Rank.TEN),
            new Card(Card.Suit.CLUBS, Card.Rank.FIVE),
            new Card(Card.Suit.DIAMONDS, Card.Rank.THREE)
        );
        PokerHand pairKings = new PokerHand(pairKingsHole, pairKingsCommunity);

        assert pairAces.compareTo(pairKings) > 0 : "Pair of aces should beat pair of kings";
        System.out.println("✓ Pair of aces beats pair of kings");
    }

    private static void testGameBasics() {
        System.out.println("\nTesting Game Basics...");

        PokerGame game = new PokerGame(10, 20); // Small blind 10, big blind 20
        game.addPlayer("Player1", 1000);
        game.addPlayer("Player2", 1000);
        game.addPlayer("Player3", 1000);

        game.startNewHand();
        assert game.getGameState() == PokerGame.GameState.BETTING_PRE_FLOP : "Game should start in pre-flop state";
        assert game.getPot() == 30 : "Pot should have blinds";
        assert game.getPlayers().get(0).getHoleCards().size() == 2 : "Player should have 2 hole cards";
        System.out.println("✓ Game initialization works");

        // Simulate all players calling
        while (game.needsPlayerAction()) {
            game.performAction(PokerGame.PlayerAction.CALL, game.getCurrentBet());
        }

        game.update(0.1f); // Advance game state
        assert game.getGameState() == PokerGame.GameState.BETTING_FLOP : "Game should advance to flop";
        assert game.getCommunityCards().size() == 3 : "Should have 3 community cards";
        System.out.println("✓ Game advances to flop correctly");
    }

    private static void testBettingLogic() {
        System.out.println("\nTesting Betting Logic...");

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

        assert currentPlayer.getChips() == initialChips - (50 - currentPlayer.getCurrentBet()) :
               "Player chips should be reduced by raise amount";
        assert game.getCurrentBet() == 50 : "Current bet should be updated";
        assert game.getPot() > initialPot : "Pot should increase after raise";
        System.out.println("✓ Raise action works correctly");

        // Next player folds
        Player nextPlayer = game.getCurrentPlayer();
        game.performAction(PokerGame.PlayerAction.FOLD, 0);
        assert !nextPlayer.isActive() : "Player should be inactive after folding";
        System.out.println("✓ Fold action works correctly");
    }
}
