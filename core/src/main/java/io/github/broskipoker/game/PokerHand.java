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

    public PokerHand(List<Card> holeCards, List<Card> communityCards) {
        cards = new ArrayList<>();
        cards.addAll(holeCards);
        cards.addAll(communityCards);

        cards.sort((c1, c2) -> c2.getRank().getValue() - c1.getRank().getValue());

        evaluateHand();
    }

    private void evaluateHand() {
        tieBreakers = new ArrayList<>();

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
        if (!isStraightFlush()) return false;

        // Find suited cards
        Map<Card.Suit, List<Card>> suitedCards = getSuitedCards();
        for (List<Card> suited : suitedCards.values()) {
            if (suited.size() >= 5) {
                suited.sort((c1, c2) -> c2.getRank().getValue() - c1.getRank().getValue());
                if (suited.get(0).getRank() == Card.Rank.ACE && isStraightFlushInList(suited)) {
                    // Add Ace as a tiebreaker, even if Royal Flush has no tiebreaker
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
                if (isStraightFlushInList(suited)) {
                    // Tiebreaker is the highest card in the straight flush
                    int highestValue = getHighestStraightValue(suited);
                    for (Card card : suited) {
                        if (card.getRank().getValue() == highestValue) {
                            tieBreakers.add(card.getRank());
                            break;
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isFourOfAKind() {
        Map<Card.Rank, Integer> rankCount = getRankCount();
        for (Map.Entry<Card.Rank, Integer> entry : rankCount.entrySet()) {
            if (entry.getValue() == 4) {
                // Tiebreaker is the rank of the four of a kind
                tieBreakers.add(entry.getKey());

                // Second tiebreaker is the second-highest card in the hand
                for (Card card : cards) {
                    if (card.getRank() != entry.getKey()) {
                        tieBreakers.add(card.getRank());
                        break;
                    }
                }
                return true;
            }
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
            return true;
        }
        return false;
    }

    private boolean isFlush() {
        Map<Card.Suit, List<Card>> suitedCards = getSuitedCards();
        for (Map.Entry<Card.Suit, List<Card>> entry : suitedCards.entrySet()) {
            if (entry.getValue().size() >= 5) {
                // Sort cards by rank if flush found
                List<Card> flushCards = entry.getValue();
                flushCards.sort((c1, c2) -> c2.getRank().getValue() - c1.getRank().getValue());

                // Tiebreakers consists of the top 5 cards in the flush
                for (int i = 0; i < 5 && i < flushCards.size(); i++) {
                    tieBreakers.add(flushCards.get(i).getRank());
                }
                return true;
            }
        }
        return false;
    }

    private boolean isStraight() {
        Set<Integer> values = new HashSet<>();
        for (Card card : cards) {
            values.add(card.getRank().getValue());
        }

        // Check for A-5 straight
        if (values.contains(14) && values.contains(2) && values.contains(3) &&
            values.contains(4) && values.contains(5)) {
            tieBreakers.add(Card.Rank.FIVE);
            return true;
        }

        // Check for other straight flushes
        for (int i = 14; i >= 5; i--) {
            if (values.contains(i) && values.contains(i - 1) && values.contains(i - 2) &&
                values.contains(i - 3) && values.contains(i - 4)) {
                // Find the card with the highest value
                for (Card card : cards) {
                    if (card.getRank().getValue() == i) {
                        tieBreakers.add(card.getRank());
                        break;
                    }
                }
                return true;
            }
        }
        return false;
	}

    private boolean isThreeOfAKind() {
        Map<Card.Rank, Integer> rankCount = getRankCount();
        for (Map.Entry<Card.Rank, Integer> entry : rankCount.entrySet()) {
            if (entry.getValue() == 3) {
                //  Three of a kind tiebreaker is the actual rank of the hand
                tieBreakers.add(entry.getKey());

                // The 2 remainig cards are added to the tiebreaker
                int added = 0;
                for (Card card : cards) {
                    if (card.getRank() != entry.getKey()) {
                        tieBreakers.add(card.getRank());
                        added++;
                        if (added == 2) break;
                    }
                }
                return true;
            }
        }
        return false;
    }

    private boolean isTwoPair() {
        Map<Card.Rank, Integer> rankCount = getRankCount();
        List<Card.Rank> pairs = new ArrayList<>();

        // Get pairs
        for (Map.Entry<Card.Rank, Integer> entry : rankCount.entrySet()) {
            if (entry.getValue() == 2) {
                pairs.add(entry.getKey());
            }
        }

        if (pairs.size() >= 2) {
            // Sort pairs by rank
            pairs.sort((r1, r2) -> r2.getValue() - r1.getValue());
            // Tiebreakers are the two highest pairs
            tieBreakers.add(pairs.get(0));
            tieBreakers.add(pairs.get(1));
            // Additional tiebreaker consist of the remaining 5th card
            for (Card card : cards) {
                if (card.getRank() != pairs.get(0) && card.getRank() != pairs.get(1)) {
                    tieBreakers.add(card.getRank());
                    break;
                }
            }
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
            // The first tiebreaker is the rank of the pair
            tieBreakers.add(pairRank);
            // Additional tiebreakers are the 3 remaining cards
            int added = 0;
            for (Card card : cards) {
                if (card.getRank() != pairRank) {
                    tieBreakers.add(card.getRank());
                    added++;
                    if (added == 3) break;
                }
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
}
