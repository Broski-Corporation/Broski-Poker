package io.github.broskipoker.game;

import java.util.*;

public class PokerHand {
    public enum HandRank {
        HIGH_CARD(1), PAIR(2), TWO_PAIR(3), THREE_OF_A_KIND(4), STRAIGHT(5), FLUSH(6),
        FULL_HOUSE(7), FOUR_OF_A_KIND(8), STRAIGHT_FLUSH(9), ROYAL_FLUSH(10);

        private final int value;

        HandRank(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

    }

    private final List<Card> cards;
    private HandRank rank;
    private List<Card.Rank> tieBreakers;
    private List<Card> bestHand; // Add this field

    public PokerHand(List<Card> holeCards, List<Card> communityCards) {
        cards = new ArrayList<>();
        cards.addAll(holeCards);
        cards.addAll(communityCards);

        cards.sort((c1, c2) -> c2.getRank().getValue() - c1.getRank().getValue());

        bestHand = new ArrayList<>();
        evaluateHand();
    }

    public List<Card> getBestHand() { // Add this getter
        return bestHand;
    }

    private void evaluateHand() {
        tieBreakers = new ArrayList<>();
        bestHand = new ArrayList<>();

        if (isRoyalFlush()) {
            rank = HandRank.ROYAL_FLUSH;
        } else if (isStraightFlush()) {
            rank = HandRank.STRAIGHT_FLUSH;
        } else if (isFourOfAKind()) {
            rank = HandRank.FOUR_OF_A_KIND;
        } else if (isFullHouse()) {
            rank = HandRank.FULL_HOUSE;
        } else if (isFlush()) {
            rank = HandRank.FLUSH;
        } else if (isStraight()) {
            rank = HandRank.STRAIGHT;
        } else if (isThreeOfAKind()) {
            rank = HandRank.THREE_OF_A_KIND;
        } else if (isTwoPair()) {
            rank = HandRank.TWO_PAIR;
        } else if (isPair()) {
            rank = HandRank.PAIR;
        } else {
            rank = HandRank.HIGH_CARD;
            // Tiebreakers consists of the actual hand (in the case of high card)
            for (Card card : cards) {
                tieBreakers.add(card.getRank());
                bestHand.add(card);
                if (tieBreakers.size() == 5) break;
            }
        }
    }

    public HandRank getRank() {
        return rank;
    }

    public List<Card.Rank> getTieBreakers() {
        return tieBreakers;
    }

    public int compareTo(PokerHand other) {
        // Compare hand ranks
        if (this.rank.getValue() != other.rank.getValue()) {
            return this.rank.getValue() - other.rank.getValue();
        }

        // Compare tiebreakers
        List<Card.Rank> otherTieBreakers = other.getTieBreakers();
        for (int i = 0; i < Math.min(tieBreakers.size(), otherTieBreakers.size()); i++) {
            int diff = tieBreakers.get(i).getValue() - otherTieBreakers.get(i).getValue();
            if (diff != 0) return diff;
        }

        // If hand is the same, return a tie
        return 0;
    }

    @Override
    public String toString() {
        return rank.toString();
    }

    private boolean isRoyalFlush() {
        Map<Card.Suit, List<Card>> suitedCards = getSuitedCards();
        for (List<Card> suited : suitedCards.values()) {
            if (suited.size() >= 5) {
                suited.sort((c1, c2) -> c2.getRank().getValue() - c1.getRank().getValue());
                // Royal Flush is 10, J, Q, K, A
                List<Card> rf = new ArrayList<>();
                Set<Card.Rank> needed = EnumSet.of(Card.Rank.TEN, Card.Rank.JACK, Card.Rank.QUEEN, Card.Rank.KING, Card.Rank.ACE);
                for (Card card : suited) {
                    if (needed.contains(card.getRank())) {
                        rf.add(card);
                    }
                }
                if (rf.size() == 5) {
                    bestHand = new ArrayList<>(rf);
                    tieBreakers.add(Card.Rank.ACE);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isStraightFlush() {
        Map<Card.Suit, List<Card>> suitedCards = getSuitedCards();
        for (List<Card> suited : suitedCards.values()) {
            if (suited.size() >= 5) {
                suited.sort((c1, c2) -> c2.getRank().getValue() - c1.getRank().getValue());
                List<Card> straightFlush = getBestStraight(suited);
                if (straightFlush != null) {
                    bestHand = new ArrayList<>(straightFlush);
                    tieBreakers.add(bestHand.get(0).getRank());
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isFourOfAKind() {
        Map<Card.Rank, Integer> rankCount = getRankCount();
        Card.Rank quadRank = null;
        for (Map.Entry<Card.Rank, Integer> entry : rankCount.entrySet()) {
            if (entry.getValue() == 4) {
                quadRank = entry.getKey();
                break;
            }
        }
        if (quadRank != null) {
            tieBreakers.add(quadRank);
            List<Card> quads = new ArrayList<>();
            Card kicker = null;
            for (Card card : cards) {
                if (card.getRank() == quadRank) {
                    quads.add(card);
                } else if (kicker == null) {
                    kicker = card;
                }
            }
            bestHand = new ArrayList<>(quads);
            if (kicker != null) bestHand.add(kicker);
            if (kicker != null) tieBreakers.add(kicker.getRank());
            return true;
        }
        return false;
    }

    private boolean isFullHouse() {
        Map<Card.Rank, Integer> rankCount = getRankCount();
        Card.Rank threeOfAKindRank = null;
        Card.Rank pairRank = null;

        // Find three of a kind
        for (Map.Entry<Card.Rank, Integer> entry : rankCount.entrySet()) {
            if (entry.getValue() >= 3) {
                if (threeOfAKindRank == null || entry.getKey().getValue() > threeOfAKindRank.getValue()) {
                    threeOfAKindRank = entry.getKey();
                }
            }
        }

        // Find a pair
        for (Map.Entry<Card.Rank, Integer> entry : rankCount.entrySet()) {
            if (entry.getValue() >= 2 && entry.getKey() != threeOfAKindRank) {
                if (pairRank == null || entry.getKey().getValue() > pairRank.getValue()) {
                    pairRank = entry.getKey();
                }
            }
        }
        if (threeOfAKindRank != null && pairRank != null) {
            tieBreakers.add(threeOfAKindRank);
            tieBreakers.add(pairRank);
            List<Card> trips = new ArrayList<>();
            List<Card> pair = new ArrayList<>();
            for (Card card : cards) {
                if (card.getRank() == threeOfAKindRank && trips.size() < 3) {
                    trips.add(card);
                } else if (card.getRank() == pairRank && pair.size() < 2) {
                    pair.add(card);
                }
            }
            bestHand = new ArrayList<>();
            bestHand.addAll(trips);
            bestHand.addAll(pair);
            return true;
        }
        return false;
    }

    private boolean isFlush() {
        Map<Card.Suit, List<Card>> suitedCards = getSuitedCards();
        for (Map.Entry<Card.Suit, List<Card>> entry : suitedCards.entrySet()) {
            if (entry.getValue().size() >= 5) {
                List<Card> flushCards = entry.getValue();
                flushCards.sort((c1, c2) -> c2.getRank().getValue() - c1.getRank().getValue());
                bestHand = new ArrayList<>(flushCards.subList(0, 5));
                for (int i = 0; i < 5 && i < flushCards.size(); i++) {
                    tieBreakers.add(flushCards.get(i).getRank());
                }
                return true;
            }
        }
        return false;
    }

    private boolean isStraight() {
        List<Card> straight = getBestStraight(cards);
        if (straight != null) {
            bestHand = new ArrayList<>(straight);
            tieBreakers.add(straight.get(0).getRank());
            return true;
        }
        return false;
    }

    private boolean isThreeOfAKind() {
        Map<Card.Rank, Integer> rankCount = getRankCount();
        Card.Rank tripsRank = null;
        for (Map.Entry<Card.Rank, Integer> entry : rankCount.entrySet()) {
            if (entry.getValue() == 3) {
                tripsRank = entry.getKey();
                break;
            }
        }
        if (tripsRank != null) {
            tieBreakers.add(tripsRank);
            List<Card> trips = new ArrayList<>();
            List<Card> kickers = new ArrayList<>();
            for (Card card : cards) {
                if (card.getRank() == tripsRank && trips.size() < 3) {
                    trips.add(card);
                } else if (kickers.size() < 2) {
                    kickers.add(card);
                }
            }
            bestHand = new ArrayList<>();
            bestHand.addAll(trips);
            bestHand.addAll(kickers);
            for (Card kicker : kickers) {
                tieBreakers.add(kicker.getRank());
            }
            return true;
        }
        return false;
    }

    private boolean isTwoPair() {
        Map<Card.Rank, Integer> rankCount = getRankCount();
        List<Card.Rank> pairs = new ArrayList<>();

        for (Map.Entry<Card.Rank, Integer> entry : rankCount.entrySet()) {
            if (entry.getValue() == 2) {
                pairs.add(entry.getKey());
            }
        }

        if (pairs.size() >= 2) {
            pairs.sort((r1, r2) -> r2.getValue() - r1.getValue());
            tieBreakers.add(pairs.get(0));
            tieBreakers.add(pairs.get(1));
            List<Card> pair1 = new ArrayList<>();
            List<Card> pair2 = new ArrayList<>();
            Card kicker = null;
            for (Card card : cards) {
                if (card.getRank() == pairs.get(0) && pair1.size() < 2) {
                    pair1.add(card);
                } else if (card.getRank() == pairs.get(1) && pair2.size() < 2) {
                    pair2.add(card);
                } else if (kicker == null) {
                    kicker = card;
                }
            }
            bestHand = new ArrayList<>();
            bestHand.addAll(pair1);
            bestHand.addAll(pair2);
            if (kicker != null) bestHand.add(kicker);
            if (kicker != null) tieBreakers.add(kicker.getRank());
            return true;
        }
        return false;
    }

    private boolean isPair() {
        Map<Card.Rank, Integer> rankCount = getRankCount();
        Card.Rank pairRank = null;

        for (Map.Entry<Card.Rank, Integer> entry : rankCount.entrySet()) {
            if (entry.getValue() == 2) {
                pairRank = entry.getKey();
                break;
            }
        }

        if (pairRank != null) {
            tieBreakers.add(pairRank);
            List<Card> pair = new ArrayList<>();
            List<Card> kickers = new ArrayList<>();
            for (Card card : cards) {
                if (card.getRank() == pairRank && pair.size() < 2) {
                    pair.add(card);
                } else if (kickers.size() < 3) {
                    kickers.add(card);
                }
            }
            bestHand = new ArrayList<>();
            bestHand.addAll(pair);
            bestHand.addAll(kickers);
            for (Card kicker : kickers) {
                tieBreakers.add(kicker.getRank());
            }
            return true;
        }
        return false;
    }

    // Helper function
    private Map<Card.Suit, List<Card>> getSuitedCards() {
        Map<Card.Suit, List<Card>> suitedCards = new HashMap<>();
        for (Card card : cards) {
            if (!suitedCards.containsKey(card.getSuit())) {
                suitedCards.put(card.getSuit(), new ArrayList<>());
            }
            suitedCards.get(card.getSuit()).add(card);
        }
        return suitedCards;
    }

    // Helper function
    private boolean isStraightFlushInList(List<Card> cards) {
        Set<Integer> values = new HashSet<>();
        for (Card card : cards) {
            values.add(card.getRank().getValue());
        }

        // Check for A-5 straight flush
        if (values.contains(14) && values.contains(2) && values.contains(3) &&
                values.contains(4) && values.contains(5)) {
            return true;
        }

        // Check for other straight flushes
        for(int i = 14; i >= 5; i--) {
            if (values.contains(i) && values.contains(i - 1) && values.contains(i - 2) &&
                    values.contains(i - 3) && values.contains(i - 4)) {
                return true;
            }
        }
        return false;
    }

    // Helper function
    private int getHighestStraightValue(List<Card> cards) {
        Set<Integer> values = new HashSet<>();
        for (Card card : cards) {
            values.add(card.getRank().getValue());
        }

        // Check for A-5 straight flush
        if (values.contains(14) && values.contains(2) && values.contains(3) &&
                values.contains(4) && values.contains(5)) {
            return 5;
        }

        // Check for other straight flushes
        for(int i = 14; i >= 5; i--) {
            if (values.contains(i) && values.contains(i - 1) && values.contains(i - 2) &&
                    values.contains(i - 3) && values.contains(i - 4)) {
                return i;
            }
        }
        return 0;
    }

    // Helper function
    private Map<Card.Rank, Integer> getRankCount() {
        Map<Card.Rank, Integer> rankCount = new HashMap<>();
        for (Card card : cards) {
            rankCount.put(card.getRank(), rankCount.getOrDefault(card.getRank(), 0) + 1);
        }
        return rankCount;
    }

    // Helper to find the best straight in a list of cards (returns 5 cards or null)
    private List<Card> getBestStraight(List<Card> cardList) {
        Map<Integer, Card> rankToCard = new HashMap<>();
        for (Card card : cardList) {
            int value = card.getRank().getValue();
            if (!rankToCard.containsKey(value)) {
                rankToCard.put(value, card);
            }
        }
        List<Integer> values = new ArrayList<>(rankToCard.keySet());
        Collections.sort(values, Collections.reverseOrder());

        // Check for high straight (A-K-Q-J-10 down to 5-4-3-2-A)
        for (int i = 0; i < values.size(); i++) {
            int start = values.get(i);
            List<Card> straight = new ArrayList<>();
            straight.add(rankToCard.get(start));
            int needed = 4;
            int next = start - 1;
            while (needed > 0 && rankToCard.containsKey(next)) {
                straight.add(rankToCard.get(next));
                next--;
                needed--;
            }
            if (straight.size() == 5) {
                return straight;
            }
        }
        // Special case: A-2-3-4-5
        if (rankToCard.containsKey(14) && rankToCard.containsKey(2) && rankToCard.containsKey(3)
                && rankToCard.containsKey(4) && rankToCard.containsKey(5)) {
            List<Card> straight = new ArrayList<>();
            straight.add(rankToCard.get(5));
            straight.add(rankToCard.get(4));
            straight.add(rankToCard.get(3));
            straight.add(rankToCard.get(2));
            straight.add(rankToCard.get(14));
            return straight;
        }
        return null;
    }
}
