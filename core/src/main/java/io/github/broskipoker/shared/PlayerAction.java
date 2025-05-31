package io.github.broskipoker.shared;

public class PlayerAction {
    public int playerId;
    public String action; // "CHECK", "CALL", "RAISE", "FOLD"
    public int amount; // Only for raise/bet
}
