package io.github.broskipoker.shared;

import io.github.broskipoker.game.PokerGame;

public class PlayerAction {
    public int playerId;
    public PokerGame.PlayerAction action; // "CHECK", "CALL", "RAISE", "FOLD"
    public int amount; // Only for raise/bet
}
