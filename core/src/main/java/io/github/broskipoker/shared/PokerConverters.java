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
    public static List<PlayerInfo> toPlayerInfoList(List<Player> players, Player myPlayer) {
        List<PlayerInfo> infos = new ArrayList<>();
        for (Player player : players) {
            boolean sendHole = player == myPlayer;
            infos.add(toPlayerInfo(player, sendHole));
        }
        return infos;
    }

    // PokerGame -> GameStateUpdate
    // myPlayer: the player object for whom this update is being generated
    public static GameStateUpdate toGameStateUpdate(PokerGame game, Player myPlayer) {
        GameStateUpdate update = new GameStateUpdate();
        update.communityCards = toCardInfoList(game.getCommunityCards());
        update.players = toPlayerInfoList(game.getPlayers(), myPlayer);
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
        return update;
    }
}
