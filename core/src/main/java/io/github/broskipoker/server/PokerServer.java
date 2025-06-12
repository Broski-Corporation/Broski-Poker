package io.github.broskipoker.server;

import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import io.github.broskipoker.shared.*;
import io.github.broskipoker.game.PokerGame;
import io.github.broskipoker.game.Player;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PokerServer {

    // Initialize game with blinds (customize as needed)
    private static final PokerGame pokerGame = new PokerGame(50, 100);
    // Map connection -> player index in PokerGame.players
    private static final Map<Connection, Integer> playerConnections = new HashMap<>();

    public static void main(String[] args) throws IOException {
        Server server = new Server();
        NetworkRegistration.register(server.getKryo());
        server.start();
        server.bind(8080);

        System.out.println("PokerServer with KryoNet is running on port 8080!");

        server.addListener(new Listener() {
            public void connected(Connection connection) {
                System.out.println("New client connected: " + connection.getID());
            }

            public void disconnected(Connection connection) {
                System.out.println("Client disconnected: " + connection.getID());
                Integer idx = playerConnections.remove(connection);
                if (idx != null) {
                    // Just mark player as inactive (do not remove to keep indices stable)
                    Player player = pokerGame.getPlayers().get(idx);
                    player.setActive(false);
                    broadcastGameState(server);
                }
            }

            public void received(Connection connection, Object object) {
                // Handle login/join
                if (object instanceof LoginRequest) {
                    LoginRequest login = (LoginRequest) object;
                    System.out.println("Login from: " + login.username);

                    // Add player to game (if not already present)
                    int playerIndex = pokerGame.getPlayers().size();
                    pokerGame.addPlayer(login.username, 10000);
                    playerConnections.put(connection, playerIndex);

                    // Send login response
                    LoginResponse resp = new LoginResponse();
                    resp.success = true;
                    resp.message = "Welcome, " + login.username + "!";
                    connection.sendTCP(resp);

                    // If enough players, start the game/hand
                    if (pokerGame.getPlayers().size() >= 2 && pokerGame.getGameState() == PokerGame.GameState.WAITING_FOR_PLAYERS) {
                        pokerGame.startNewHand();
                    }

                    // Send initial game state to this player
                    Player myPlayer = pokerGame.getPlayers().get(playerIndex);
                    GameStateUpdate update = PokerConverters.toGameStateUpdate(pokerGame, myPlayer);
                    connection.sendTCP(update);

                    // Broadcast new game state to all players
                    broadcastGameState(server);
                    return;
                }

                // Handle player actions (bet, fold, etc)
                if (object instanceof PlayerAction) {
                    PlayerAction action = (PlayerAction) object;
                    Integer idx = playerConnections.get(connection);

                    if (idx == null) {
                        System.out.println("Unknown player action from connection: " + connection.getID());
                        return;
                    }
                    Player player = pokerGame.getPlayers().get(idx);

                    PokerGame.PlayerAction act;
                    try {
                        act = action.action;
                    } catch (Exception e) {
                        System.out.println("Invalid action string: " + action.action);
                        return;
                    }

                    // Only allow action if this is the current player
                    if (pokerGame.getCurrentPlayerIndex() != idx) {
                        System.out.println("Player acted out of turn: " + player.getName());
                        return;
                    }
                    pokerGame.performAction(act, action.amount);

                    // If round/game needs progressing, do so
                    if (!pokerGame.needsPlayerAction()) {
                        // You may want to call pokerGame.update(0) here to move the game forward.
                        // For now, just broadcast state.
                    }

                    // Broadcast updated game state to everyone
                    broadcastGameState(server);
                    return;
                }
            }
        });

        // Keep server running
        while (true) {
            try { Thread.sleep(10000); } catch (InterruptedException ignored) {}
        }
    }

    private static void broadcastGameState(Server server) {
        for (Map.Entry<Connection, Integer> entry : playerConnections.entrySet()) {
            Connection connection = entry.getKey();
            int idx = entry.getValue();
            Player player = pokerGame.getPlayers().get(idx);
            GameStateUpdate update = PokerConverters.toGameStateUpdate(pokerGame, player);
            connection.sendTCP(update);
        }
    }
}
