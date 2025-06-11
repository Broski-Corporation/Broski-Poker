package io.github.broskipoker.game.tests;

import io.github.broskipoker.game.Card;
import io.github.broskipoker.game.Deck;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

public class DeckTest {

    @Test
    public void testDeckInitialization() {
        Deck deck = new Deck();
        assertEquals(52, deck.cardsRemaining(), "A newly initialized deck should contain 52 cards.");

        // Test if all cards are unique (no duplicates)
        Set<String> uniqueCards = new HashSet<>();
        for (int i = 0; i < 52; i++) {
            Card card = deck.drawCard();
            assertNotNull(card, "Should be able to draw a card until the deck is empty.");
            assertTrue(uniqueCards.add(card.toString()), "All cards in the deck should be unique.");
        }
        assertEquals(0, deck.cardsRemaining(), "After drawing all cards, the deck should be empty.");
    }

    @Test
    public void testDrawCard() {
        Deck deck = new Deck();
        int initialCards = deck.cardsRemaining();

        Card drawnCard = deck.drawCard();
        assertNotNull(drawnCard, "Should be able to draw a card from a non-empty deck.");
        assertEquals(initialCards - 1, deck.cardsRemaining(), "Drawing a card should decrease the deck size by one.");

        // Draw all cards and check for null
        for (int i = 0; i < initialCards - 1; i++) {
            deck.drawCard();
        }
        assertEquals(0, deck.cardsRemaining(), "Deck should be empty after drawing all cards.");
        assertNull(deck.drawCard(), "Drawing from an empty deck should return null.");
    }

    @Test
    public void testResetDeck() {
        Deck deck = new Deck();
        deck.drawCard(); // Draw a card to change the state
        deck.drawCard();
        assertEquals(50, deck.cardsRemaining(), "Deck should have 50 cards after drawing two.");

        deck.reset();
        assertEquals(52, deck.cardsRemaining(), "After reset, the deck should be back to 52 cards.");

        // Ensure that a reset deck is also properly shuffled (though not easily testable for randomness,
        // we can check if it's not in the original sorted order if we assume the initial state is sorted,
        // which it isn't, as the constructor shuffles. A better check is to ensure it's not the same
        // order as a previously drawn deck if possible).
        // For simplicity, we'll just check if cards can be drawn and are unique.
        Set<String> uniqueCards = new HashSet<>();
        for (int i = 0; i < 52; i++) {
            Card card = deck.drawCard();
            assertNotNull(card, "Should be able to draw cards from a reset deck.");
            assertTrue(uniqueCards.add(card.toString()), "All cards in a reset deck should be unique.");
        }
    }

    @Test
    public void testShuffleEffectiveness() {
        Deck deck1 = new Deck();
        Deck deck2 = new Deck(); // Two separate decks to compare initial shuffles

        List<Card> initialOrder1 = new ArrayList<>();
        for(int i = 0; i < 52; i++) {
            initialOrder1.add(deck1.drawCard());
        }

        List<Card> initialOrder2 = new ArrayList<>();
        for(int i = 0; i < 52; i++) {
            initialOrder2.add(deck2.drawCard());
        }

        // It's highly improbable for two randomly shuffled decks to have the exact same order.
        // This is a probabilistic test, so it might rarely fail, but it's a good indicator of shuffling.
        assertNotEquals(initialOrder1, initialOrder2, "Two newly initialized decks should be in different shuffled orders (highly probable).");

        // Test that resetting also shuffles
        Deck deckToReset = new Deck();
        List<Card> originalResetOrder = new ArrayList<>();
        for(int i = 0; i < 52; i++) {
            originalResetOrder.add(deckToReset.drawCard());
        }

        deckToReset.reset();
        List<Card> newResetOrder = new ArrayList<>();
        for(int i = 0; i < 52; i++) {
            newResetOrder.add(deckToReset.drawCard());
        }
        assertNotEquals(originalResetOrder, newResetOrder, "A reset deck should be in a new shuffled order.");
    }
}
