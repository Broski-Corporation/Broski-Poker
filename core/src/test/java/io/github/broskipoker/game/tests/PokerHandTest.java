package io.github.broskipoker.game.tests;

import io.github.broskipoker.game.Card;
import io.github.broskipoker.game.PokerHand;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;

public class PokerHandTest {

    // Helper method to create a card for cleaner test readability
    private Card c(Card.Suit suit, Card.Rank rank) {
        return new Card(suit, rank);
    }

    // Helper to sort cards for comparison (useful for bestHand asserts)
    private List<Card> sortCards(List<Card> cards) {
        List<Card> sorted = new ArrayList<>(cards);
        sorted.sort(Comparator.comparing(Card::getRank).reversed().thenComparing(Card::getSuit));
        return sorted;
    }

    // --- Royal Flush Tests ---
    @Test
    public void testRoyalFlush() {
        // Perfect Royal Flush
        List<Card> holeCards1 = Arrays.asList(c(Card.Suit.HEARTS, Card.Rank.ACE), c(Card.Suit.HEARTS, Card.Rank.KING));
        List<Card> communityCards1 = Arrays.asList(c(Card.Suit.HEARTS, Card.Rank.QUEEN), c(Card.Suit.HEARTS, Card.Rank.JACK), c(Card.Suit.HEARTS, Card.Rank.TEN), c(Card.Suit.CLUBS, Card.Rank.TWO), c(Card.Suit.DIAMONDS, Card.Rank.THREE));
        PokerHand royalFlush1 = new PokerHand(holeCards1, communityCards1);
        assertEquals(PokerHand.HandRank.ROYAL_FLUSH, royalFlush1.getRank(), "Should be Royal Flush - perfect");
        assertEquals(1, royalFlush1.getTieBreakers().size(), "Royal Flush should have 1 tiebreaker (ACE)");
        assertEquals(Card.Rank.ACE, royalFlush1.getTieBreakers().get(0), "Royal Flush tiebreaker should be ACE");
        assertEquals(5, royalFlush1.getBestHand().size(), "Royal Flush best hand should have 5 cards");
        assertTrue(royalFlush1.getBestHand().containsAll(Arrays.asList(c(Card.Suit.HEARTS, Card.Rank.ACE), c(Card.Suit.HEARTS, Card.Rank.KING), c(Card.Suit.HEARTS, Card.Rank.QUEEN), c(Card.Suit.HEARTS, Card.Rank.JACK), c(Card.Suit.HEARTS, Card.Rank.TEN))));

        // Royal Flush with extra cards
        List<Card> holeCards2 = Arrays.asList(c(Card.Suit.SPADES, Card.Rank.ACE), c(Card.Suit.SPADES, Card.Rank.QUEEN));
        List<Card> communityCards2 = Arrays.asList(c(Card.Suit.SPADES, Card.Rank.KING), c(Card.Suit.SPADES, Card.Rank.JACK), c(Card.Suit.SPADES, Card.Rank.TEN), c(Card.Suit.CLUBS, Card.Rank.KING), c(Card.Suit.DIAMONDS, Card.Rank.ACE));
        PokerHand royalFlush2 = new PokerHand(holeCards2, communityCards2);
        assertEquals(PokerHand.HandRank.ROYAL_FLUSH, royalFlush2.getRank(), "Should be Royal Flush - extra cards");
    }

    // --- Straight Flush Tests ---
    @Test
    public void testStraightFlush() {
        // High Straight Flush (9-5)
        List<Card> holeCards1 = Arrays.asList(c(Card.Suit.CLUBS, Card.Rank.NINE), c(Card.Suit.CLUBS, Card.Rank.EIGHT));
        List<Card> communityCards1 = Arrays.asList(c(Card.Suit.CLUBS, Card.Rank.SEVEN), c(Card.Suit.CLUBS, Card.Rank.SIX), c(Card.Suit.CLUBS, Card.Rank.FIVE), c(Card.Suit.HEARTS, Card.Rank.TWO), c(Card.Suit.DIAMONDS, Card.Rank.THREE));
        PokerHand straightFlush1 = new PokerHand(holeCards1, communityCards1);
        assertEquals(PokerHand.HandRank.STRAIGHT_FLUSH, straightFlush1.getRank(), "Should be Straight Flush (9-5)");
        assertEquals(1, straightFlush1.getTieBreakers().size(), "Straight Flush should have 1 tiebreaker");
        assertEquals(Card.Rank.NINE, straightFlush1.getTieBreakers().get(0), "Straight Flush tiebreaker should be 9");
        assertEquals(5, straightFlush1.getBestHand().size(), "Straight Flush best hand should have 5 cards");
        assertTrue(straightFlush1.getBestHand().containsAll(Arrays.asList(c(Card.Suit.CLUBS, Card.Rank.NINE), c(Card.Suit.CLUBS, Card.Rank.EIGHT), c(Card.Suit.CLUBS, Card.Rank.SEVEN), c(Card.Suit.CLUBS, Card.Rank.SIX), c(Card.Suit.CLUBS, Card.Rank.FIVE))));

        // Low Straight Flush (A-5)
        List<Card> holeCards2 = Arrays.asList(c(Card.Suit.DIAMONDS, Card.Rank.ACE), c(Card.Suit.DIAMONDS, Card.Rank.TWO));
        List<Card> communityCards2 = Arrays.asList(c(Card.Suit.DIAMONDS, Card.Rank.THREE), c(Card.Suit.DIAMONDS, Card.Rank.FOUR), c(Card.Suit.DIAMONDS, Card.Rank.FIVE), c(Card.Suit.SPADES, Card.Rank.QUEEN), c(Card.Suit.HEARTS, Card.Rank.KING));
        PokerHand straightFlush2 = new PokerHand(holeCards2, communityCards2);
        assertEquals(PokerHand.HandRank.STRAIGHT_FLUSH, straightFlush2.getRank(), "Should be Straight Flush (A-5)");
        assertEquals(1, straightFlush2.getTieBreakers().size(), "Straight Flush should have 1 tiebreaker");
        assertEquals(Card.Rank.FIVE, straightFlush2.getTieBreakers().get(0), "Straight Flush tiebreaker should be 5");
        assertTrue(straightFlush2.getBestHand().containsAll(Arrays.asList(c(Card.Suit.DIAMONDS, Card.Rank.ACE), c(Card.Suit.DIAMONDS, Card.Rank.TWO), c(Card.Suit.DIAMONDS, Card.Rank.THREE), c(Card.Suit.DIAMONDS, Card.Rank.FOUR), c(Card.Suit.DIAMONDS, Card.Rank.FIVE))));

        // Straight Flush vs. Higher Straight Flush
        List<Card> holeCards3 = Arrays.asList(c(Card.Suit.HEARTS, Card.Rank.TEN), c(Card.Suit.HEARTS, Card.Rank.SIX)); // Royal Flush here
        List<Card> communityCards3 = Arrays.asList(c(Card.Suit.HEARTS, Card.Rank.JACK), c(Card.Suit.HEARTS, Card.Rank.QUEEN), c(Card.Suit.HEARTS, Card.Rank.KING), c(Card.Suit.HEARTS, Card.Rank.ACE), c(Card.Suit.CLUBS, Card.Rank.TWO));
        PokerHand royalFlush3 = new PokerHand(holeCards3, communityCards3);
        assertEquals(PokerHand.HandRank.ROYAL_FLUSH, royalFlush3.getRank(), "Should be Royal Flush over Straight Flush");

        List<Card> holeCards4 = Arrays.asList(c(Card.Suit.SPADES, Card.Rank.TEN), c(Card.Suit.SPADES, Card.Rank.NINE));
        List<Card> communityCards4 = Arrays.asList(c(Card.Suit.SPADES, Card.Rank.QUEEN), c(Card.Suit.SPADES, Card.Rank.JACK), c(Card.Suit.SPADES, Card.Rank.EIGHT), c(Card.Suit.CLUBS, Card.Rank.TWO), c(Card.Suit.DIAMONDS, Card.Rank.THREE));
        PokerHand straightFlush4 = new PokerHand(holeCards4, communityCards4);
        assertEquals(PokerHand.HandRank.STRAIGHT_FLUSH, straightFlush4.getRank(), "Should be Straight Flush (Q-8)");
        assertEquals(Card.Rank.QUEEN, straightFlush4.getTieBreakers().get(0), "Straight Flush tiebreaker should be Queen");
    }

    // --- Four of a Kind Tests ---
    @Test
    public void testFourOfAKind() {
        // Four Aces with King kicker
        List<Card> holeCards1 = Arrays.asList(c(Card.Suit.HEARTS, Card.Rank.ACE), c(Card.Suit.DIAMONDS, Card.Rank.ACE));
        List<Card> communityCards1 = Arrays.asList(c(Card.Suit.CLUBS, Card.Rank.ACE), c(Card.Suit.SPADES, Card.Rank.ACE), c(Card.Suit.HEARTS, Card.Rank.KING), c(Card.Suit.CLUBS, Card.Rank.TWO), c(Card.Suit.DIAMONDS, Card.Rank.THREE));
        PokerHand fourOfAKind1 = new PokerHand(holeCards1, communityCards1);
        assertEquals(PokerHand.HandRank.FOUR_OF_A_KIND, fourOfAKind1.getRank(), "Should be Four of a Kind (Aces)");
        assertEquals(2, fourOfAKind1.getTieBreakers().size(), "Four of a Kind should have 2 tiebreakers");
        assertEquals(Card.Rank.ACE, fourOfAKind1.getTieBreakers().get(0), "Quad rank should be Ace");
        assertEquals(Card.Rank.KING, fourOfAKind1.getTieBreakers().get(1), "Kicker should be King");
        assertEquals(5, fourOfAKind1.getBestHand().size(), "Four of a Kind best hand should have 5 cards");
        assertTrue(fourOfAKind1.getBestHand().containsAll(Arrays.asList(c(Card.Suit.HEARTS, Card.Rank.ACE), c(Card.Suit.DIAMONDS, Card.Rank.ACE), c(Card.Suit.CLUBS, Card.Rank.ACE), c(Card.Suit.SPADES, Card.Rank.ACE), c(Card.Suit.HEARTS, Card.Rank.KING))));


        // Four Eights with a Ten kicker (from community)
        List<Card> holeCards2 = Arrays.asList(c(Card.Suit.HEARTS, Card.Rank.EIGHT), c(Card.Suit.DIAMONDS, Card.Rank.EIGHT));
        List<Card> communityCards2 = Arrays.asList(c(Card.Suit.CLUBS, Card.Rank.EIGHT), c(Card.Suit.SPADES, Card.Rank.EIGHT), c(Card.Suit.HEARTS, Card.Rank.QUEEN), c(Card.Suit.CLUBS, Card.Rank.TEN), c(Card.Suit.DIAMONDS, Card.Rank.TWO));
        PokerHand fourOfAKind2 = new PokerHand(holeCards2, communityCards2);
        assertEquals(PokerHand.HandRank.FOUR_OF_A_KIND, fourOfAKind2.getRank(), "Should be Four of a Kind (Eights)");
        assertEquals(Card.Rank.EIGHT, fourOfAKind2.getTieBreakers().get(0), "Quad rank should be Eight");
        assertEquals(Card.Rank.QUEEN, fourOfAKind2.getTieBreakers().get(1), "Kicker should be Queen");
    }

    // --- Full House Tests ---
    @Test
    public void testFullHouse() {
        // Full House Aces over Kings
        List<Card> holeCards1 = Arrays.asList(c(Card.Suit.HEARTS, Card.Rank.ACE), c(Card.Suit.DIAMONDS, Card.Rank.ACE));
        List<Card> communityCards1 = Arrays.asList(c(Card.Suit.CLUBS, Card.Rank.ACE), c(Card.Suit.SPADES, Card.Rank.KING), c(Card.Suit.HEARTS, Card.Rank.KING), c(Card.Suit.CLUBS, Card.Rank.TWO), c(Card.Suit.DIAMONDS, Card.Rank.THREE));
        PokerHand fullHouse1 = new PokerHand(holeCards1, communityCards1);
        assertEquals(PokerHand.HandRank.FULL_HOUSE, fullHouse1.getRank(), "Should be Full House (Aces full of Kings)");
        assertEquals(2, fullHouse1.getTieBreakers().size(), "Full House should have 2 tiebreakers");
        assertEquals(Card.Rank.ACE, fullHouse1.getTieBreakers().get(0), "Trips rank should be Ace");
        assertEquals(Card.Rank.KING, fullHouse1.getTieBreakers().get(1), "Pair rank should be King");
        assertEquals(5, fullHouse1.getBestHand().size(), "Full House best hand should have 5 cards");
        assertTrue(fullHouse1.getBestHand().containsAll(Arrays.asList(c(Card.Suit.HEARTS, Card.Rank.ACE), c(Card.Suit.DIAMONDS, Card.Rank.ACE), c(Card.Suit.CLUBS, Card.Rank.ACE), c(Card.Suit.SPADES, Card.Rank.KING), c(Card.Suit.HEARTS, Card.Rank.KING))));


        // Full House Queens over Fives
        List<Card> holeCards2 = Arrays.asList(c(Card.Suit.CLUBS, Card.Rank.QUEEN), c(Card.Suit.SPADES, Card.Rank.QUEEN));
        List<Card> communityCards2 = Arrays.asList(c(Card.Suit.DIAMONDS, Card.Rank.QUEEN), c(Card.Suit.HEARTS, Card.Rank.FIVE), c(Card.Suit.CLUBS, Card.Rank.FIVE), c(Card.Suit.SPADES, Card.Rank.SEVEN), c(Card.Suit.HEARTS, Card.Rank.TWO));
        PokerHand fullHouse2 = new PokerHand(holeCards2, communityCards2);
        assertEquals(PokerHand.HandRank.FULL_HOUSE, fullHouse2.getRank(), "Should be Full House (Queens full of Fives)");
        assertEquals(Card.Rank.QUEEN, fullHouse2.getTieBreakers().get(0), "Trips rank should be Queen");
        assertEquals(Card.Rank.FIVE, fullHouse2.getTieBreakers().get(1), "Pair rank should be Five");

        // Two possible full houses, pick the highest trips
        List<Card> holeCards3 = Arrays.asList(c(Card.Suit.HEARTS, Card.Rank.TEN), c(Card.Suit.DIAMONDS, Card.Rank.TEN));
        List<Card> communityCards3 = Arrays.asList(c(Card.Suit.CLUBS, Card.Rank.TEN), c(Card.Suit.SPADES, Card.Rank.FIVE), c(Card.Suit.HEARTS, Card.Rank.FIVE), c(Card.Suit.CLUBS, Card.Rank.FIVE), c(Card.Suit.DIAMONDS, Card.Rank.TWO));
        PokerHand fullHouse3 = new PokerHand(holeCards3, communityCards3);
        assertEquals(PokerHand.HandRank.FULL_HOUSE, fullHouse3.getRank(), "Should be Full House (Tens full of Fives)");
        assertEquals(Card.Rank.TEN, fullHouse3.getTieBreakers().get(0), "Trips rank should be Ten (highest)");
        assertEquals(Card.Rank.FIVE, fullHouse3.getTieBreakers().get(1), "Pair rank should be Five");

        // Full house from 4 of a kind (picks trips and highest pair)
        List<Card> holeCards4 = Arrays.asList(c(Card.Suit.HEARTS, Card.Rank.ACE), c(Card.Suit.DIAMONDS, Card.Rank.ACE));
        List<Card> communityCards4 = Arrays.asList(c(Card.Suit.CLUBS, Card.Rank.ACE), c(Card.Suit.SPADES, Card.Rank.ACE), c(Card.Suit.HEARTS, Card.Rank.KING), c(Card.Suit.CLUBS, Card.Rank.KING), c(Card.Suit.DIAMONDS, Card.Rank.TWO));
        PokerHand fullHouse4 = new PokerHand(holeCards4, communityCards4);
        assertEquals(PokerHand.HandRank.FOUR_OF_A_KIND, fullHouse4.getRank(), "Should be Four of a Kind, not Full House, if both exist");

    }

    // --- Flush Tests ---
    @Test
    public void testFlush() {
        // Simple Flush
        List<Card> holeCards1 = Arrays.asList(c(Card.Suit.DIAMONDS, Card.Rank.ACE), c(Card.Suit.DIAMONDS, Card.Rank.TEN));
        List<Card> communityCards1 = Arrays.asList(c(Card.Suit.DIAMONDS, Card.Rank.SEVEN), c(Card.Suit.DIAMONDS, Card.Rank.FIVE), c(Card.Suit.DIAMONDS, Card.Rank.TWO), c(Card.Suit.CLUBS, Card.Rank.KING), c(Card.Suit.HEARTS, Card.Rank.QUEEN));
        PokerHand flush1 = new PokerHand(holeCards1, communityCards1);
        assertEquals(PokerHand.HandRank.FLUSH, flush1.getRank(), "Should be Flush");
        assertEquals(5, flush1.getTieBreakers().size(), "Flush should have 5 tiebreakers");
        assertEquals(Card.Rank.ACE, flush1.getTieBreakers().get(0), "Flush tiebreaker 1 should be Ace");
        assertEquals(Card.Rank.TEN, flush1.getTieBreakers().get(1), "Flush tiebreaker 2 should be Ten");
        assertEquals(Card.Rank.SEVEN, flush1.getTieBreakers().get(2), "Flush tiebreaker 3 should be Seven");
        assertEquals(Card.Rank.FIVE, flush1.getTieBreakers().get(3), "Flush tiebreaker 4 should be Five");
        assertEquals(Card.Rank.TWO, flush1.getTieBreakers().get(4), "Flush tiebreaker 5 should be Two");
        assertEquals(5, flush1.getBestHand().size(), "Flush best hand should have 5 cards");
        assertTrue(flush1.getBestHand().containsAll(Arrays.asList(c(Card.Suit.DIAMONDS, Card.Rank.ACE), c(Card.Suit.DIAMONDS, Card.Rank.TEN), c(Card.Suit.DIAMONDS, Card.Rank.SEVEN), c(Card.Suit.DIAMONDS, Card.Rank.FIVE), c(Card.Suit.DIAMONDS, Card.Rank.TWO))));

        // Flush with more than 5 suited cards, picking highest 5
        List<Card> holeCards2 = Arrays.asList(c(Card.Suit.SPADES, Card.Rank.KING), c(Card.Suit.SPADES, Card.Rank.QUEEN));
        List<Card> communityCards2 = Arrays.asList(c(Card.Suit.SPADES, Card.Rank.TEN), c(Card.Suit.SPADES, Card.Rank.SEVEN), c(Card.Suit.SPADES, Card.Rank.FIVE), c(Card.Suit.SPADES, Card.Rank.THREE), c(Card.Suit.HEARTS, Card.Rank.JACK));
        PokerHand flush2 = new PokerHand(holeCards2, communityCards2);
        assertEquals(PokerHand.HandRank.FLUSH, flush2.getRank(), "Should be Flush with more than 5 suited cards");
        assertEquals(Card.Rank.KING, flush2.getTieBreakers().get(0), "Flush tiebreaker 1 should be King");
        assertEquals(Card.Rank.QUEEN, flush2.getTieBreakers().get(1), "Flush tiebreaker 2 should be Queen");
        assertEquals(Card.Rank.TEN, flush2.getTieBreakers().get(2), "Flush tiebreaker 3 should be Ten");
        assertEquals(Card.Rank.SEVEN, flush2.getTieBreakers().get(3), "Flush tiebreaker 4 should be Seven");
        assertEquals(Card.Rank.FIVE, flush2.getTieBreakers().get(4), "Flush tiebreaker 5 should be Five");
    }

    // --- Straight Tests ---
    @Test
    public void testStraight() {
        // High Straight (A-K-Q-J-10)
        List<Card> holeCards1 = Arrays.asList(c(Card.Suit.HEARTS, Card.Rank.ACE), c(Card.Suit.DIAMONDS, Card.Rank.KING));
        List<Card> communityCards1 = Arrays.asList(c(Card.Suit.CLUBS, Card.Rank.QUEEN), c(Card.Suit.SPADES, Card.Rank.JACK), c(Card.Suit.HEARTS, Card.Rank.TEN), c(Card.Suit.DIAMONDS, Card.Rank.TWO), c(Card.Suit.SPADES, Card.Rank.THREE));
        PokerHand straight1 = new PokerHand(holeCards1, communityCards1);
        assertEquals(PokerHand.HandRank.STRAIGHT, straight1.getRank(), "Should be Straight (A-10)");
        assertEquals(1, straight1.getTieBreakers().size(), "Straight should have 1 tiebreaker");
        assertEquals(Card.Rank.ACE, straight1.getTieBreakers().get(0), "Straight tiebreaker should be Ace");
        assertEquals(5, straight1.getBestHand().size(), "Straight best hand should have 5 cards");
        assertTrue(straight1.getBestHand().containsAll(Arrays.asList(c(Card.Suit.HEARTS, Card.Rank.ACE), c(Card.Suit.DIAMONDS, Card.Rank.KING), c(Card.Suit.CLUBS, Card.Rank.QUEEN), c(Card.Suit.SPADES, Card.Rank.JACK), c(Card.Suit.HEARTS, Card.Rank.TEN))));


        // Low Straight (A-2-3-4-5)
        List<Card> holeCards2 = Arrays.asList(c(Card.Suit.SPADES, Card.Rank.ACE), c(Card.Suit.CLUBS, Card.Rank.TWO));
        List<Card> communityCards2 = Arrays.asList(c(Card.Suit.DIAMONDS, Card.Rank.THREE), c(Card.Suit.HEARTS, Card.Rank.FOUR), c(Card.Suit.SPADES, Card.Rank.FIVE), c(Card.Suit.CLUBS, Card.Rank.NINE), c(Card.Suit.DIAMONDS, Card.Rank.TEN));
        PokerHand straight2 = new PokerHand(holeCards2, communityCards2);
        assertEquals(PokerHand.HandRank.STRAIGHT, straight2.getRank(), "Should be Straight (A-5)");
        assertEquals(1, straight2.getTieBreakers().size(), "Straight should have 1 tiebreaker");
        assertEquals(Card.Rank.FIVE, straight2.getTieBreakers().get(0), "Straight tiebreaker should be Five");
        assertEquals(5, straight2.getBestHand().size(), "Straight best hand should have 5 cards");
        // For A-5 straight, the ace is counted as low, so the highest card in sequence is 5.
        // The bestHand should contain the actual cards that form the A-5 straight.
        assertTrue(sortCards(straight2.getBestHand()).equals(sortCards(Arrays.asList(c(Card.Suit.SPADES, Card.Rank.ACE), c(Card.Suit.CLUBS, Card.Rank.TWO), c(Card.Suit.DIAMONDS, Card.Rank.THREE), c(Card.Suit.HEARTS, Card.Rank.FOUR), c(Card.Suit.SPADES, Card.Rank.FIVE)))));


        // Straight with duplicate ranks
        List<Card> holeCards3 = Arrays.asList(c(Card.Suit.HEARTS, Card.Rank.SIX), c(Card.Suit.DIAMONDS, Card.Rank.SEVEN));
        List<Card> communityCards3 = Arrays.asList(c(Card.Suit.CLUBS, Card.Rank.EIGHT), c(Card.Suit.SPADES, Card.Rank.NINE), c(Card.Suit.HEARTS, Card.Rank.TEN), c(Card.Suit.CLUBS, Card.Rank.SIX), c(Card.Suit.DIAMONDS, Card.Rank.TWO));
        PokerHand straight3 = new PokerHand(holeCards3, communityCards3);
        assertEquals(PokerHand.HandRank.STRAIGHT, straight3.getRank(), "Should be Straight with duplicate ranks");
        assertEquals(Card.Rank.TEN, straight3.getTieBreakers().get(0), "Straight tiebreaker should be Ten");

        // Two possible straights, pick the highest
        List<Card> holeCards4 = Arrays.asList(c(Card.Suit.HEARTS, Card.Rank.SEVEN), c(Card.Suit.DIAMONDS, Card.Rank.EIGHT));
        List<Card> communityCards4 = Arrays.asList(c(Card.Suit.CLUBS, Card.Rank.NINE), c(Card.Suit.SPADES, Card.Rank.TEN), c(Card.Suit.HEARTS, Card.Rank.JACK), c(Card.Suit.CLUBS, Card.Rank.THREE), c(Card.Suit.DIAMONDS, Card.Rank.FOUR));
        PokerHand straight4 = new PokerHand(holeCards4, communityCards4);
        assertEquals(PokerHand.HandRank.STRAIGHT, straight4.getRank(), "Should be Straight (J-7)");
        assertEquals(Card.Rank.JACK, straight4.getTieBreakers().get(0), "Straight tiebreaker should be Jack");
    }

    // --- Three of a Kind Tests ---
    @Test
    public void testThreeOfAKind() {
        // Three Aces with two kickers
        List<Card> holeCards1 = Arrays.asList(c(Card.Suit.HEARTS, Card.Rank.ACE), c(Card.Suit.DIAMONDS, Card.Rank.ACE));
        List<Card> communityCards1 = Arrays.asList(c(Card.Suit.CLUBS, Card.Rank.ACE), c(Card.Suit.SPADES, Card.Rank.KING), c(Card.Suit.HEARTS, Card.Rank.QUEEN), c(Card.Suit.CLUBS, Card.Rank.TWO), c(Card.Suit.DIAMONDS, Card.Rank.THREE));
        PokerHand threeOfAKind1 = new PokerHand(holeCards1, communityCards1);
        assertEquals(PokerHand.HandRank.THREE_OF_A_KIND, threeOfAKind1.getRank(), "Should be Three of a Kind (Aces)");
        assertEquals(3, threeOfAKind1.getTieBreakers().size(), "Three of a Kind should have 3 tiebreakers");
        assertEquals(Card.Rank.ACE, threeOfAKind1.getTieBreakers().get(0), "Trips rank should be Ace");
        assertEquals(Card.Rank.KING, threeOfAKind1.getTieBreakers().get(1), "Kicker 1 should be King");
        assertEquals(Card.Rank.QUEEN, threeOfAKind1.getTieBreakers().get(2), "Kicker 2 should be Queen");
        assertEquals(5, threeOfAKind1.getBestHand().size(), "Three of a Kind best hand should have 5 cards");
        assertTrue(sortCards(threeOfAKind1.getBestHand()).equals(sortCards(Arrays.asList(c(Card.Suit.HEARTS, Card.Rank.ACE), c(Card.Suit.DIAMONDS, Card.Rank.ACE), c(Card.Suit.CLUBS, Card.Rank.ACE), c(Card.Suit.SPADES, Card.Rank.KING), c(Card.Suit.HEARTS, Card.Rank.QUEEN)))));


        // Three Tens with higher kickers available
        List<Card> holeCards2 = Arrays.asList(c(Card.Suit.CLUBS, Card.Rank.TEN), c(Card.Suit.SPADES, Card.Rank.TEN));
        List<Card> communityCards2 = Arrays.asList(c(Card.Suit.DIAMONDS, Card.Rank.TEN), c(Card.Suit.HEARTS, Card.Rank.ACE), c(Card.Suit.CLUBS, Card.Rank.KING), c(Card.Suit.SPADES, Card.Rank.QUEEN), c(Card.Suit.HEARTS, Card.Rank.TWO));
        PokerHand threeOfAKind2 = new PokerHand(holeCards2, communityCards2);
        assertEquals(PokerHand.HandRank.THREE_OF_A_KIND, threeOfAKind2.getRank(), "Should be Three of a Kind (Tens)");
        assertEquals(Card.Rank.TEN, threeOfAKind2.getTieBreakers().get(0), "Trips rank should be Ten");
        assertEquals(Card.Rank.ACE, threeOfAKind2.getTieBreakers().get(1), "Kicker 1 should be Ace");
        assertEquals(Card.Rank.KING, threeOfAKind2.getTieBreakers().get(2), "Kicker 2 should be King");
    }

    // --- Two Pair Tests ---
    @Test
    public void testTwoPair() {
        // Two Pair: Aces and Kings with Queen kicker
        List<Card> holeCards1 = Arrays.asList(c(Card.Suit.HEARTS, Card.Rank.ACE), c(Card.Suit.DIAMONDS, Card.Rank.ACE));
        List<Card> communityCards1 = Arrays.asList(c(Card.Suit.CLUBS, Card.Rank.KING), c(Card.Suit.SPADES, Card.Rank.KING), c(Card.Suit.HEARTS, Card.Rank.QUEEN), c(Card.Suit.CLUBS, Card.Rank.TWO), c(Card.Suit.DIAMONDS, Card.Rank.THREE));
        PokerHand twoPair1 = new PokerHand(holeCards1, communityCards1);
        assertEquals(PokerHand.HandRank.TWO_PAIR, twoPair1.getRank(), "Should be Two Pair (Aces and Kings)");
        assertEquals(3, twoPair1.getTieBreakers().size(), "Two Pair should have 3 tiebreakers");
        assertEquals(Card.Rank.ACE, twoPair1.getTieBreakers().get(0), "Higher pair rank should be Ace");
        assertEquals(Card.Rank.KING, twoPair1.getTieBreakers().get(1), "Lower pair rank should be King");
        assertEquals(Card.Rank.QUEEN, twoPair1.getTieBreakers().get(2), "Kicker should be Queen");
        assertEquals(5, twoPair1.getBestHand().size(), "Two Pair best hand should have 5 cards");
        assertTrue(sortCards(twoPair1.getBestHand()).equals(sortCards(Arrays.asList(c(Card.Suit.HEARTS, Card.Rank.ACE), c(Card.Suit.DIAMONDS, Card.Rank.ACE), c(Card.Suit.CLUBS, Card.Rank.KING), c(Card.Suit.SPADES, Card.Rank.KING), c(Card.Suit.HEARTS, Card.Rank.QUEEN)))));

        // Three pairs, pick highest two
        List<Card> holeCards2 = Arrays.asList(c(Card.Suit.HEARTS, Card.Rank.TEN), c(Card.Suit.DIAMONDS, Card.Rank.TEN));
        List<Card> communityCards2 = Arrays.asList(c(Card.Suit.CLUBS, Card.Rank.EIGHT), c(Card.Suit.SPADES, Card.Rank.EIGHT), c(Card.Suit.HEARTS, Card.Rank.FIVE), c(Card.Suit.CLUBS, Card.Rank.FIVE), c(Card.Suit.DIAMONDS, Card.Rank.QUEEN));
        PokerHand twoPair2 = new PokerHand(holeCards2, communityCards2);
        assertEquals(PokerHand.HandRank.TWO_PAIR, twoPair2.getRank(), "Should be Two Pair (Tens and Eights)");
        assertEquals(Card.Rank.TEN, twoPair2.getTieBreakers().get(0), "Higher pair rank should be Ten");
        assertEquals(Card.Rank.EIGHT, twoPair2.getTieBreakers().get(1), "Lower pair rank should be Eight");
        assertEquals(Card.Rank.QUEEN, twoPair2.getTieBreakers().get(2), "Kicker should be Queen");
    }

    // --- Pair Tests ---
    @Test
    public void testPair() {
        // Pair of Aces with three kickers
        List<Card> holeCards1 = Arrays.asList(c(Card.Suit.HEARTS, Card.Rank.ACE), c(Card.Suit.DIAMONDS, Card.Rank.ACE));
        List<Card> communityCards1 = Arrays.asList(c(Card.Suit.CLUBS, Card.Rank.KING), c(Card.Suit.SPADES, Card.Rank.QUEEN), c(Card.Suit.HEARTS, Card.Rank.TEN), c(Card.Suit.CLUBS, Card.Rank.TWO), c(Card.Suit.DIAMONDS, Card.Rank.THREE));
        PokerHand pair1 = new PokerHand(holeCards1, communityCards1);
        assertEquals(PokerHand.HandRank.PAIR, pair1.getRank(), "Should be Pair (Aces)");
        assertEquals(4, pair1.getTieBreakers().size(), "Pair should have 4 tiebreakers");
        assertEquals(Card.Rank.ACE, pair1.getTieBreakers().get(0), "Pair rank should be Ace");
        assertEquals(Card.Rank.KING, pair1.getTieBreakers().get(1), "Kicker 1 should be King");
        assertEquals(Card.Rank.QUEEN, pair1.getTieBreakers().get(2), "Kicker 2 should be Queen");
        assertEquals(Card.Rank.TEN, pair1.getTieBreakers().get(3), "Kicker 3 should be Ten");
        assertEquals(5, pair1.getBestHand().size(), "Pair best hand should have 5 cards");
        assertTrue(sortCards(pair1.getBestHand()).equals(sortCards(Arrays.asList(c(Card.Suit.HEARTS, Card.Rank.ACE), c(Card.Suit.DIAMONDS, Card.Rank.ACE), c(Card.Suit.CLUBS, Card.Rank.KING), c(Card.Suit.SPADES, Card.Rank.QUEEN), c(Card.Suit.HEARTS, Card.Rank.TEN)))));
    }

    // --- High Card Tests ---
    @Test
    public void testHighCard() {
        List<Card> holeCards1 = Arrays.asList(c(Card.Suit.HEARTS, Card.Rank.ACE), c(Card.Suit.DIAMONDS, Card.Rank.KING));
        List<Card> communityCards1 = Arrays.asList(c(Card.Suit.CLUBS, Card.Rank.NINE), c(Card.Suit.SPADES, Card.Rank.SEVEN), c(Card.Suit.HEARTS, Card.Rank.FIVE), c(Card.Suit.CLUBS, Card.Rank.THREE), c(Card.Suit.DIAMONDS, Card.Rank.TWO));
        PokerHand highCard1 = new PokerHand(holeCards1, communityCards1);
        assertEquals(PokerHand.HandRank.HIGH_CARD, highCard1.getRank(), "Should be High Card");
        assertEquals(5, highCard1.getTieBreakers().size(), "High Card should have 5 tiebreakers");
        assertEquals(Card.Rank.ACE, highCard1.getTieBreakers().get(0), "High Card tiebreaker 1 should be Ace");
        assertEquals(Card.Rank.KING, highCard1.getTieBreakers().get(1), "High Card tiebreaker 2 should be King");
        assertEquals(Card.Rank.NINE, highCard1.getTieBreakers().get(2), "High Card tiebreaker 3 should be Nine");
        assertEquals(Card.Rank.SEVEN, highCard1.getTieBreakers().get(3), "High Card tiebreaker 4 should be Seven");
        assertEquals(Card.Rank.FIVE, highCard1.getTieBreakers().get(4), "High Card tiebreaker 5 should be Five");
        assertEquals(5, highCard1.getBestHand().size(), "High Card best hand should have 5 cards");
        assertTrue(sortCards(highCard1.getBestHand()).equals(sortCards(Arrays.asList(c(Card.Suit.HEARTS, Card.Rank.ACE), c(Card.Suit.DIAMONDS, Card.Rank.KING), c(Card.Suit.CLUBS, Card.Rank.NINE), c(Card.Suit.SPADES, Card.Rank.SEVEN), c(Card.Suit.HEARTS, Card.Rank.FIVE)))));

    }

    // --- Hand Comparison (compareTo) Tests ---
    @Test
    public void testHandComparison() {
        // Different Ranks
        PokerHand royalFlush = new PokerHand(Arrays.asList(c(Card.Suit.HEARTS, Card.Rank.ACE), c(Card.Suit.HEARTS, Card.Rank.KING)), Arrays.asList(c(Card.Suit.HEARTS, Card.Rank.QUEEN), c(Card.Suit.HEARTS, Card.Rank.JACK), c(Card.Suit.HEARTS, Card.Rank.TEN)));
        PokerHand fourOfAKind = new PokerHand(Arrays.asList(c(Card.Suit.CLUBS, Card.Rank.ACE), c(Card.Suit.DIAMONDS, Card.Rank.ACE)), Arrays.asList(c(Card.Suit.HEARTS, Card.Rank.ACE), c(Card.Suit.SPADES, Card.Rank.ACE), c(Card.Suit.HEARTS, Card.Rank.TWO)));
        assertTrue(royalFlush.compareTo(fourOfAKind) > 0, "Royal Flush should beat Four of a Kind");

        // Same Rank, different Tiebreakers (Straight Flush)
        PokerHand sfHigh = new PokerHand(Arrays.asList(c(Card.Suit.CLUBS, Card.Rank.NINE), c(Card.Suit.CLUBS, Card.Rank.EIGHT)), Arrays.asList(c(Card.Suit.CLUBS, Card.Rank.SEVEN), c(Card.Suit.CLUBS, Card.Rank.SIX), c(Card.Suit.CLUBS, Card.Rank.FIVE)));
        PokerHand sfLow = new PokerHand(Arrays.asList(c(Card.Suit.DIAMONDS, Card.Rank.SEVEN), c(Card.Suit.DIAMONDS, Card.Rank.SIX)), Arrays.asList(c(Card.Suit.DIAMONDS, Card.Rank.FIVE), c(Card.Suit.DIAMONDS, Card.Rank.FOUR), c(Card.Suit.DIAMONDS, Card.Rank.THREE)));
        assertTrue(sfHigh.compareTo(sfLow) > 0, "Higher Straight Flush should win");

        // Same Rank, same first Tiebreaker, different second (Full House)
        PokerHand fhAcesKings = new PokerHand(Arrays.asList(c(Card.Suit.HEARTS, Card.Rank.ACE), c(Card.Suit.DIAMONDS, Card.Rank.ACE)), Arrays.asList(c(Card.Suit.CLUBS, Card.Rank.ACE), c(Card.Suit.SPADES, Card.Rank.KING), c(Card.Suit.HEARTS, Card.Rank.KING)));
        PokerHand fhAcesQueens = new PokerHand(Arrays.asList(c(Card.Suit.HEARTS, Card.Rank.ACE), c(Card.Suit.DIAMONDS, Card.Rank.ACE)), Arrays.asList(c(Card.Suit.CLUBS, Card.Rank.ACE), c(Card.Suit.SPADES, Card.Rank.QUEEN), c(Card.Suit.HEARTS, Card.Rank.QUEEN)));
        assertTrue(fhAcesKings.compareTo(fhAcesQueens) > 0, "Full House with higher pair should win");

        // Same Rank, same two pairs, different kicker (Two Pair)
        PokerHand tpAKQ = new PokerHand(Arrays.asList(c(Card.Suit.HEARTS, Card.Rank.ACE), c(Card.Suit.DIAMONDS, Card.Rank.ACE)), Arrays.asList(c(Card.Suit.CLUBS, Card.Rank.KING), c(Card.Suit.SPADES, Card.Rank.KING), c(Card.Suit.HEARTS, Card.Rank.QUEEN)));
        PokerHand tpAKT = new PokerHand(Arrays.asList(c(Card.Suit.HEARTS, Card.Rank.ACE), c(Card.Suit.DIAMONDS, Card.Rank.ACE)), Arrays.asList(c(Card.Suit.CLUBS, Card.Rank.KING), c(Card.Suit.SPADES, Card.Rank.KING), c(Card.Suit.HEARTS, Card.Rank.TEN)));
        assertTrue(tpAKQ.compareTo(tpAKT) > 0, "Two Pair with higher kicker should win");

        // Same Rank, same pair, different kickers (Pair)
        PokerHand pairAKQJ = new PokerHand(Arrays.asList(c(Card.Suit.HEARTS, Card.Rank.ACE), c(Card.Suit.DIAMONDS, Card.Rank.ACE)), Arrays.asList(c(Card.Suit.CLUBS, Card.Rank.KING), c(Card.Suit.SPADES, Card.Rank.QUEEN), c(Card.Suit.HEARTS, Card.Rank.JACK)));
        PokerHand pairAKQT = new PokerHand(Arrays.asList(c(Card.Suit.HEARTS, Card.Rank.ACE), c(Card.Suit.DIAMONDS, Card.Rank.ACE)), Arrays.asList(c(Card.Suit.CLUBS, Card.Rank.KING), c(Card.Suit.SPADES, Card.Rank.QUEEN), c(Card.Suit.HEARTS, Card.Rank.TEN)));
        assertTrue(pairAKQJ.compareTo(pairAKQT) > 0, "Pair with higher kicker should win");

        // Same Rank, same kickers (High Card)
        PokerHand highAKQ97 = new PokerHand(Arrays.asList(c(Card.Suit.HEARTS, Card.Rank.ACE), c(Card.Suit.DIAMONDS, Card.Rank.KING)), Arrays.asList(c(Card.Suit.CLUBS, Card.Rank.QUEEN), c(Card.Suit.SPADES, Card.Rank.NINE), c(Card.Suit.HEARTS, Card.Rank.SEVEN)));
        PokerHand highAKQ96 = new PokerHand(Arrays.asList(c(Card.Suit.HEARTS, Card.Rank.ACE), c(Card.Suit.DIAMONDS, Card.Rank.KING)), Arrays.asList(c(Card.Suit.CLUBS, Card.Rank.QUEEN), c(Card.Suit.SPADES, Card.Rank.NINE), c(Card.Suit.HEARTS, Card.Rank.SIX)));
        assertTrue(highAKQ97.compareTo(highAKQ96) > 0, "High Card with higher kicker should win");

        // Exactly the same hand (tie)
        PokerHand hand1 = new PokerHand(Arrays.asList(c(Card.Suit.HEARTS, Card.Rank.ACE), c(Card.Suit.DIAMONDS, Card.Rank.KING)), Arrays.asList(c(Card.Suit.CLUBS, Card.Rank.QUEEN), c(Card.Suit.SPADES, Card.Rank.JACK), c(Card.Suit.HEARTS, Card.Rank.TEN)));
        PokerHand hand2 = new PokerHand(Arrays.asList(c(Card.Suit.SPADES, Card.Rank.ACE), c(Card.Suit.CLUBS, Card.Rank.KING)), Arrays.asList(c(Card.Suit.DIAMONDS, Card.Rank.QUEEN), c(Card.Suit.HEARTS, Card.Rank.JACK), c(Card.Suit.SPADES, Card.Rank.TEN)));
        assertEquals(0, hand1.compareTo(hand2), "Identical straights should result in a tie");
    }

    // --- Edge Cases and Combinations ---
    @Test
    public void testComplexHandScenarios() {
        // A hand that could be a straight or a flush, but is a Straight Flush
        List<Card> holeCards1 = Arrays.asList(c(Card.Suit.CLUBS, Card.Rank.SIX), c(Card.Suit.CLUBS, Card.Rank.SEVEN));
        List<Card> communityCards1 = Arrays.asList(c(Card.Suit.CLUBS, Card.Rank.EIGHT), c(Card.Suit.CLUBS, Card.Rank.NINE), c(Card.Suit.CLUBS, Card.Rank.TEN), c(Card.Suit.HEARTS, Card.Rank.ACE), c(Card.Suit.DIAMONDS, Card.Rank.KING));
        PokerHand hand1 = new PokerHand(holeCards1, communityCards1);
        assertEquals(PokerHand.HandRank.STRAIGHT_FLUSH, hand1.getRank(), "Should prioritize Straight Flush over Flush/Straight");

        // A hand with a Straight and a Flush, but neither Straight Flush
        List<Card> holeCards2 = Arrays.asList(c(Card.Suit.HEARTS, Card.Rank.ACE), c(Card.Suit.HEARTS, Card.Rank.FIVE));
        List<Card> communityCards2 = Arrays.asList(c(Card.Suit.HEARTS, Card.Rank.SEVEN), c(Card.Suit.HEARTS, Card.Rank.EIGHT), c(Card.Suit.HEARTS, Card.Rank.JACK), c(Card.Suit.CLUBS, Card.Rank.SIX), c(Card.Suit.DIAMONDS, Card.Rank.NINE));
        PokerHand hand2 = new PokerHand(holeCards2, communityCards2);
        assertEquals(PokerHand.HandRank.FLUSH, hand2.getRank(), "Should be Flush (A-J-8-7-5) over Straight (5-9)"); // Flush (A-J-8-7-5) and Straight (5-6-7-8-9)
        assertEquals(Card.Rank.ACE, hand2.getTieBreakers().get(0), "Flush tiebreaker should be Ace");
        assertEquals(Card.Rank.JACK, hand2.getTieBreakers().get(1), "Flush tiebreaker should be Jack");

        // A hand that is a Full House and also has a lower Three of a Kind
        List<Card> holeCards3 = Arrays.asList(c(Card.Suit.HEARTS, Card.Rank.KING), c(Card.Suit.DIAMONDS, Card.Rank.KING));
        List<Card> communityCards3 = Arrays.asList(c(Card.Suit.CLUBS, Card.Rank.KING), c(Card.Suit.SPADES, Card.Rank.QUEEN), c(Card.Suit.HEARTS, Card.Rank.QUEEN), c(Card.Suit.CLUBS, Card.Rank.TEN), c(Card.Suit.DIAMONDS, Card.Rank.TEN));
        PokerHand hand3 = new PokerHand(holeCards3, communityCards3);
        assertEquals(PokerHand.HandRank.FULL_HOUSE, hand3.getRank(), "Should be Full House (Kings full of Queens) over Kings full of Tens");
        assertEquals(Card.Rank.KING, hand3.getTieBreakers().get(0), "Trips rank should be King");
        assertEquals(Card.Rank.QUEEN, hand3.getTieBreakers().get(1), "Pair rank should be Queen");
    }

    @Test
    public void testToString() {
        List<Card> holeCards = Arrays.asList(c(Card.Suit.HEARTS, Card.Rank.ACE), c(Card.Suit.HEARTS, Card.Rank.KING));
        List<Card> communityCards = Arrays.asList(c(Card.Suit.HEARTS, Card.Rank.QUEEN), c(Card.Suit.HEARTS, Card.Rank.JACK), c(Card.Suit.HEARTS, Card.Rank.TEN), c(Card.Suit.CLUBS, Card.Rank.TWO), c(Card.Suit.DIAMONDS, Card.Rank.THREE));
        PokerHand royalFlush = new PokerHand(holeCards, communityCards);
        assertEquals("ROYAL_FLUSH", royalFlush.toString(), "toString should return the hand rank string");
    }
}
