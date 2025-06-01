package io.github.broskipoker.server;

import com.esotericsoftware.kryonet.Connection;
import io.github.broskipoker.game.PokerGame;
import io.github.broskipoker.game.Player;
import java.util.*;

public class Table {
    private final String code;
    private final PokerGame pokerGame;
    private final List<Connection> connections = new ArrayList<>();

    public Table(String code, int smallBlind, int bigBlind) {
        this.code = code;
        this.pokerGame = new PokerGame(smallBlind, bigBlind);
    }

    public synchronized void addPlayer(Connection conn, String username, int chips) {
        if (!connections.contains(conn)) {
            pokerGame.addPlayer(username, chips);
            connections.add(conn);
        }
    }

    public synchronized void removePlayer(Connection conn) {
        int idx = connections.indexOf(conn);
        if (idx != -1) {
            // Mark player as inactive (keep index stable like in original PokerServer)
            if (idx < pokerGame.getPlayers().size()) {
                Player player = pokerGame.getPlayers().get(idx);
                player.setActive(false);
            }
            connections.remove(idx);
        }
    }

    public String getCode() {
        return code;
    }

    public PokerGame getPokerGame() {
        return pokerGame;
    }

    public List<Connection> getConnections() {
        return new ArrayList<>(connections); // Return copy for thread safety
    }
}
