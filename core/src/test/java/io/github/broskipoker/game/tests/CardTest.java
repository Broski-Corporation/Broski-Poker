package io.github.broskipoker.game.tests;

import io.github.broskipoker.game.Card;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CardTest {

    @Test
    public void testCardProperties() {
        Card aceHearts = new Card(Card.Suit.HEARTS, Card.Rank.ACE);
        assertEquals(Card.Suit.HEARTS, aceHearts.getSuit(), "Card suit should be hearts");
        assertEquals(Card.Rank.ACE, aceHearts.getRank(), "Card rank should be ace");
    }

    @Test
    public void testEqualsAndHashCode() {
        Card card1 = new Card(Card.Suit.HEARTS, Card.Rank.ACE);
        Card card2 = new Card(Card.Suit.HEARTS, Card.Rank.ACE);
        Card card3 = new Card(Card.Suit.DIAMONDS, Card.Rank.ACE);
        Card card4 = new Card(Card.Suit.HEARTS, Card.Rank.KING);

        // Test equality
        assertEquals(card1, card2, "Two cards with the same suit and rank should be equal");
        assertNotEquals(card1, card3, "Cards with different suits should not be equal");
        assertNotEquals(card1, card4, "Cards with different ranks should not be equal");
        assertNotEquals(card1, null, "Card should not be equal to null");
        assertNotEquals(card1, "a string", "Card should not be equal to an object of a different type");

        // Test hashCode
        assertEquals(card1.hashCode(), card2.hashCode(), "Hash codes should be equal for equal cards");
    }

    @Test
    public void testToString() {
        Card queenSpades = new Card(Card.Suit.SPADES, Card.Rank.QUEEN);
        assertEquals("QUEEN of SPADES", queenSpades.toString(), "toString should return 'RANK of SUIT'");

        Card twoClubs = new Card(Card.Suit.CLUBS, Card.Rank.TWO);
        assertEquals("TWO of CLUBS", twoClubs.toString(), "toString should return 'RANK of SUIT'");
    }

    @Test
    public void testRankValue() {
        assertEquals(2, Card.Rank.TWO.getValue(), "Value of TWO should be 2");
        assertEquals(10, Card.Rank.TEN.getValue(), "Value of TEN should be 10");
        assertEquals(11, Card.Rank.JACK.getValue(), "Value of JACK should be 11");
        assertEquals(12, Card.Rank.QUEEN.getValue(), "Value of QUEEN should be 12");
        assertEquals(13, Card.Rank.KING.getValue(), "Value of KING should be 13");
        assertEquals(14, Card.Rank.ACE.getValue(), "Value of ACE should be 14");
    }
}
