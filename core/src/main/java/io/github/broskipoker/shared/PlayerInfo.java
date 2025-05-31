package io.github.broskipoker.shared;

import java.util.List;

public class PlayerInfo {
    public String name;
    public int chips;
    public List<CardInfo> holeCards; // only for the client, null for the other players
    public boolean isActive;
    public int currentBet;
}
