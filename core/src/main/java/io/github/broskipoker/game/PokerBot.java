package io.github.broskipoker.game;
import io.github.broskipoker.game.PokerGame.PlayerAction;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PokerBot extends Player {
    private final Random random = new Random();
    private final BotStrategy strategy;

    public enum BotStrategy {
        CONSERVATIVE, // Plays tight, only good hands
        AGGRESSIVE,   // Raises frequently
        BALANCED     // Mix of strategies
    }

    public PokerBot(Player player, BotStrategy strategy) {
        super(player);
        this.strategy = strategy;
    }

    public PlayerAction decideAction(PokerGame game, List<Card> communityCards) {
        // Evaluate hand strength (0-1 scale)
        double handStrength = evaluateHandStrength(communityCards);

        // Get game state information
        int currentBet = game.getCurrentBet();
        int myBet = getCurrentBet();
        int callAmount = currentBet - myBet;
        int potSize = game.getPot();

        // Use strategy and hand evaluation to decide action
        switch(strategy) {
            case CONSERVATIVE:
                return conservativeStrategy(handStrength, callAmount, potSize);
            case AGGRESSIVE:
                return aggressiveStrategy(handStrength, callAmount, potSize);
            case BALANCED:
                System.out.println("debug: Using balanced strategy");
                return balancedStrategy(handStrength, callAmount, potSize);
            default:
                return PlayerAction.FOLD;
        }
    }

    private PlayerAction conservativeStrategy(double handStrength, int callAmount, int potSize) {
        if (handStrength > 0.8) {
            return PlayerAction.RAISE;
        } else if (handStrength > 0.5) {
            return PlayerAction.CALL;
        } else if (handStrength > 0.3 && callAmount == 0) {
            return PlayerAction.CHECK;
        } else {
            return PlayerAction.FOLD;
        }
    }

    private PlayerAction aggressiveStrategy(double handStrength, int callAmount, int potSize) {
        if (handStrength > 0.6) {
            return PlayerAction.RAISE;
        } else if (handStrength > 0.3) {
            return PlayerAction.CALL;
        } else if (callAmount == 0) {
            return PlayerAction.CHECK;
        } else if (handStrength > 0.2 && callAmount < getChips() / 10) {
            // Call with weak hands if the cost is low relative to stack
            return PlayerAction.CALL;
        } else {
            return PlayerAction.FOLD;
        }
    }

    private PlayerAction balancedStrategy(double handStrength, int callAmount, int potSize) {
        // Occasionally bluff or play unpredictably
        double randomFactor = random.nextDouble() * 0.2; // Random adjustment ±0.2
        double adjustedStrength = Math.min(1.0, Math.max(0.0, handStrength + randomFactor - 0.1));

        // Calculate pot odds
        double potOdds = callAmount > 0 ? (double) callAmount / (potSize + callAmount) : 0;

        System.out.println(potOdds+" "+ adjustedStrength+" "+callAmount);

        if (adjustedStrength > 0.7) {
            return PlayerAction.RAISE;
        } else if (adjustedStrength > 0.4) {
            return PlayerAction.CALL;
        } else if (callAmount == 0) {
            return PlayerAction.CHECK;
        } else if (adjustedStrength > potOdds) {
            // Call if hand strength justifies the pot odds
            return PlayerAction.CALL;
        } else {
            return PlayerAction.FOLD;
        }
    }

    public double evaluateHandStrength(List<Card> communityCards) {
        // Create temporary list with hole cards and community cards
        List<Card> allCards = new ArrayList<>(getHoleCards());
        allCards.addAll(communityCards);

        // Early game - with few community cards, evaluate based on hole cards and made hands
        if (communityCards.size() <= 3) {
            return evaluateEarlyGameStrength(communityCards);
        }

        // Create a hand evaluator with all available cards
        PokerHand currentHand = new PokerHand(getHoleCards(), communityCards);

        // Base value depending on hand rank
        double baseStrength = getBaseHandStrength(currentHand.getRank());

        // Adjust for relative strength of the specific hand within its rank
        double adjustedStrength = baseStrength;

        // Reduce strength slightly if we're at turn (4 community cards) vs river (5)
        if (communityCards.size() == 4) {
            adjustedStrength *= 0.95;
        }

        return Math.min(1.0, Math.max(0.0, adjustedStrength));
    }

    private double evaluateEarlyGameStrength(List<Card> communityCards) {
        List<Card> holeCards = getHoleCards();

        // Evaluate pocket pairs
        if (holeCards.size() == 2 && holeCards.get(0).getRank() == holeCards.get(1).getRank()) {
            Card.Rank pairRank = holeCards.get(0).getRank();
            // Scale from low pairs (0.5) to high pairs (0.85)
            return 0.5 + (pairRank.ordinal() * 0.35 / 12.0);
        }

        // High cards (A, K, Q, J)
        boolean hasHighCard = holeCards.stream().anyMatch(c ->
            c.getRank() == Card.Rank.ACE ||
                c.getRank() == Card.Rank.KING ||
                c.getRank() == Card.Rank.QUEEN ||
                c.getRank() == Card.Rank.JACK);

        // Suited cards
        boolean isSuited = holeCards.size() == 2 &&
            holeCards.get(0).getSuit() == holeCards.get(1).getSuit();

        // Connected cards (for straight potential)
        boolean isConnected = holeCards.size() == 2 &&
            Math.abs(holeCards.get(0).getRank().ordinal() -
                holeCards.get(1).getRank().ordinal()) <= 2;

        // Calculate pre-flop hand strength
        double strength = 0.2;  // Base value
        if (hasHighCard) strength += 0.2;
        if (isSuited) strength += 0.1;
        if (isConnected) strength += 0.1;

        // If we have community cards, check for made hands
        if (!communityCards.isEmpty()) {
            PokerHand hand = new PokerHand(holeCards, communityCards);
            double madeHandStrength = getBaseHandStrength(hand.getRank());
            return Math.max(strength, madeHandStrength);
        }

        return strength;
    }

    private double getBaseHandStrength(PokerHand.HandRank rank) {
        return switch (rank) {
            case HIGH_CARD -> 0.1;
            case PAIR -> 0.3;
            case TWO_PAIR -> 0.5;
            case THREE_OF_A_KIND -> 0.6;
            case STRAIGHT -> 0.7;
            case FLUSH -> 0.75;
            case FULL_HOUSE -> 0.85;
            case FOUR_OF_A_KIND -> 0.95;
            case STRAIGHT_FLUSH, ROYAL_FLUSH -> 1.0;
        };
    }
    // Calculate a reasonable bet amount based on pot and hand strength
    public int calculateBetAmount(int potSize, double handStrength) {
        // Simple formula: bet between 50-100% of pot based on hand strength
        return (int)(potSize * (0.5 + handStrength * 0.5));
    }
}
