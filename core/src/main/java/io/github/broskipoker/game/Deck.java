package io.github.broskipoker.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.security.SecureRandom;

public class Deck {
    private final List<Card> cards;

    public Deck() {
        cards = new ArrayList<>();
        initializeDeck();
        shuffle();
    }

    private void initializeDeck() {
        for (Card.Suit suit : Card.Suit.values()) {
            for (Card.Rank rank : Card.Rank.values()) {
                cards.add(new Card(suit, rank));
            }
        }
    }

    private void shuffle() {
        Collections.shuffle(cards, new SecureRandom());
    }

    public Card drawCard() {
        if (cards.isEmpty()) return null;
        return cards.removeFirst();
    }

    public int cardsRemaining() {
        return cards.size();
    }

    public void reset() {
        cards.clear();
        initializeDeck();
        shuffle();
    }
}
