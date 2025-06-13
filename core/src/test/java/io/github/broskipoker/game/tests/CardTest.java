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
