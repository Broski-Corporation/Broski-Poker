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
