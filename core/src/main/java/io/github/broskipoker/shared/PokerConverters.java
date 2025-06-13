package io.github.broskipoker.shared;

import io.github.broskipoker.game.Card;
import io.github.broskipoker.game.Player;
import io.github.broskipoker.game.PokerGame;

import java.util.ArrayList;
import java.util.List;

public class PokerConverters {

    // Card -> CardInfo
    public static CardInfo toCardInfo(Card card) {
        if (card == null) return null;
        CardInfo info = new CardInfo();
        info.suit = card.getSuit();
        info.rank = card.getRank();
        return info;
    }

    // CardInfo -> Card
    public static Card fromCardInfo(CardInfo info) {
        if (info == null) return null;
        return new Card(info.suit, info.rank);
    }

    // List<Card> -> List<CardInfo>
    public static List<CardInfo> toCardInfoList(List<Card> cards) {
        if (cards == null) return null;
        List<CardInfo> infos = new ArrayList<>();
        for (Card c : cards) {
            infos.add(toCardInfo(c));
        }
        return infos;
    }

    // Player -> PlayerInfo
    // sendHoleCards: true if this PlayerInfo is for the actual client player
    public static PlayerInfo toPlayerInfo(Player player, boolean sendHoleCards) {
        PlayerInfo info = new PlayerInfo();
        info.name = player.getName();
        info.chips = player.getChips();
        info.currentBet = player.getCurrentBet();
        info.isActive = player.isActive();
        if (sendHoleCards && player.getHoleCards() != null) {
            info.holeCards = toCardInfoList(player.getHoleCards());
        } else {
            info.holeCards = null; // or Collections.emptyList()
        }
        return info;
    }

    // List<Player> -> List<PlayerInfo>
    // myPlayer: the player object for whom this update is being generated
    // showAllCards: whether to show all players' cards (for showdown)
    public static List<PlayerInfo> toPlayerInfoList(List<Player> players, Player myPlayer, boolean showAllCards) {
        List<PlayerInfo> infos = new ArrayList<>();
        for (Player player : players) {
            boolean sendHole = player == myPlayer || showAllCards;
            infos.add(toPlayerInfo(player, sendHole));
        }
        return infos;
    }

    // Overload for backward compatibility
    public static List<PlayerInfo> toPlayerInfoList(List<Player> players, Player myPlayer) {
        return toPlayerInfoList(players, myPlayer, false);
    }

    // PokerGame -> GameStateUpdate
    // myPlayer: the player object for whom this update is being generated
    public static GameStateUpdate toGameStateUpdate(PokerGame game, Player myPlayer) {
        GameStateUpdate update = new GameStateUpdate();
        update.communityCards = toCardInfoList(game.getCommunityCards());

        // Determine if we should show all cards (during showdown)
        boolean isShowdown = game.getGameState() == PokerGame.GameState.SHOWDOWN;
        update.showAllCards = isShowdown;

        // Use the showAllCards parameter for player info conversion
        update.players = toPlayerInfoList(game.getPlayers(), myPlayer, isShowdown);

        update.pot = game.getPot();
        update.smallBlind = game.getSmallBlind();
        update.bigBlind = game.getBigBlind();
        update.currentBet = game.getCurrentBet();
        update.currentPlayerIndex = game.getCurrentPlayerIndex();
        update.lastRaisePlayerIndex = game.getLastRaisePlayerIndex();
        update.dealerPosition = PokerGame.getDealerPosition();
        update.needsPlayerAction = game.needsPlayerAction();
        update.gameState = PokerGame.getGameState();
        update.hasActedInRound = game.getHasActedInRound();

        // Add winner information if we're in showdown
        if (isShowdown) {
            // Get the list of winners (simplest approach is to use the server's calculation)
            List<Player> winners = game.determineWinners();

            // Store winner indices
            update.winnerIndices = new ArrayList<>();
            for (Player winner : winners) {
                update.winnerIndices.add(game.getPlayers().indexOf(winner));
            }

            // Store the best hand for display
            if (!winners.isEmpty()) {
                try {
                    // Get the first winner's best hand to display
                    Player firstWinner = winners.get(0);

                    // If PokerHand has a getBestHand method
                    io.github.broskipoker.game.PokerHand hand =
                            new io.github.broskipoker.game.PokerHand(firstWinner.getHoleCards(), game.getCommunityCards());
                    List<Card> bestHand = hand.getBestHand();

                    // Convert to CardInfo list
                    update.winningCards = toCardInfoList(bestHand);
                } catch (Exception e) {
                    System.err.println("Error calculating best hand: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }

        return update;
    }
}
