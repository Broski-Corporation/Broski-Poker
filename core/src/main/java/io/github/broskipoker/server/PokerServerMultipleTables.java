package io.github.broskipoker.server;

import com.esotericsoftware.kryonet.*;
import io.github.broskipoker.shared.*;
import io.github.broskipoker.game.PokerGame;
import io.github.broskipoker.game.Player;

public class PokerServerMultipleTables {
    private static final TableManager tableManager = new TableManager();

    public static void main(String[] args) throws Exception {
        Server server = new Server();
        NetworkRegistration.register(server.getKryo());
        server.start();
        server.bind(8080);

        System.out.println("PokerServer with multi-table support running on port 8080!");

        server.addListener(new Listener() {
            @Override
            public void connected(Connection connection) {
                System.out.println("New client connected: " + connection.getID());
            }

            @Override
            public void disconnected(Connection connection) {
                System.out.println("Client disconnected: " + connection.getID());
                Table table = tableManager.getTableByConnection(connection);
                if (table != null) {
                    tableManager.leaveTable(connection);
                    broadcastGameStateToTable(table);
                }
            }

            @Override
            public void received(Connection connection, Object object) {
                // Handle table creation
                if (object instanceof CreateTableRequest) {
                    CreateTableRequest req = (CreateTableRequest) object;
                    Table table = tableManager.createTable(req.smallBlind, req.bigBlind);
                    table.addPlayer(connection, req.username, req.chips);

                    CreateTableResponse resp = new CreateTableResponse();
                    resp.code = table.getCode();
                    resp.success = true;
                    connection.sendTCP(resp);

                    // Send initial game state to this player
                    sendGameStateToPlayer(table, connection);
                    return;
                }

                // Handle join by code
                if (object instanceof JoinTableRequest) {
                    JoinTableRequest req = (JoinTableRequest) object;
                    Table table = tableManager.joinTableByCode(connection, req.code, req.username, req.chips);
                    JoinTableResponse resp = new JoinTableResponse();
                    if (table != null) {
                        resp.success = true;
                        resp.code = req.code;

                        // If enough players, start the game/hand
                        PokerGame pokerGame = table.getPokerGame();
                        if (pokerGame.getPlayers().size() >= 2 &&
                            pokerGame.getGameState() == PokerGame.GameState.WAITING_FOR_PLAYERS) {
                            pokerGame.startNewHand();
                        }

                        // Send initial game state to this player
                        sendGameStateToPlayer(table, connection);

                        // Broadcast new game state to all players at this table
                        broadcastGameStateToTable(table);
                    } else {
                        resp.success = false;
                        resp.failReason = "Table not found or full";
                    }
                    connection.sendTCP(resp);
                    return;
                }

                // Handle login request (for backward compatibility with single table)
                if (object instanceof LoginRequest) {
                    LoginRequest login = (LoginRequest) object;
                    System.out.println("Login from: " + login.username);

                    // Create a default table or join an existing one
                    Table table = findOrCreateDefaultTable();
                    table.addPlayer(connection, login.username, 10000);
                    tableManager.joinExistingTable(connection, table);

                    // Send login response
                    LoginResponse resp = new LoginResponse();
                    resp.success = true;
                    resp.message = "Welcome, " + login.username + "!";
                    connection.sendTCP(resp);

                    // If enough players, start the game/hand
                    PokerGame pokerGame = table.getPokerGame();
                    if (pokerGame.getPlayers().size() >= 2 &&
                        pokerGame.getGameState() == PokerGame.GameState.WAITING_FOR_PLAYERS) {
                        pokerGame.startNewHand();
                    }

                    // Send initial game state to this player
                    sendGameStateToPlayer(table, connection);

                    // Broadcast new game state to all players at this table
                    broadcastGameStateToTable(table);
                    return;
                }

                // Handle player actions (route to correct table)
                if (object instanceof PlayerAction) {
                    PlayerAction action = (PlayerAction) object;
                    Table table = tableManager.getTableByConnection(connection);
                    if (table == null) {
                        System.out.println("Unknown player action from connection: " + connection.getID());
                        return;
                    }

                    PokerGame pokerGame = table.getPokerGame();
                    int playerIndex = getPlayerIndexInTable(table, connection);

                    if (playerIndex == -1) {
                        System.out.println("Player not found in table for connection: " + connection.getID());
                        return;
                    }

                    Player player = pokerGame.getPlayers().get(playerIndex);

                    PokerGame.PlayerAction act;
                    try {
                        act = action.action;
                    } catch (Exception e) {
                        System.out.println("Invalid action string: " + action.action);
                        return;
                    }

                    // Only allow action if this is the current player
                    if (pokerGame.getCurrentPlayerIndex() != playerIndex) {
                        System.out.println("Player acted out of turn: " + player.getName());
                        return;
                    }

                    pokerGame.performAction(act, action.amount);

                    // If round/game needs progressing, do so
                    if (!pokerGame.needsPlayerAction()) {
                        // You may want to call pokerGame.update(0) here to move the game forward.
                        // For now, just broadcast state.
                    }

                    // Broadcast updated game state to all players at this table
                    broadcastGameStateToTable(table);
                    return;
                }

                // Handle game state refresh requests
                if (object instanceof GameStateRequest) {
                    GameStateRequest req = (GameStateRequest) object;
                    System.out.println("Server received GameStateRequest for table: " + req.tableCode);

                    // Find the table by code
                    Table table = tableManager.getTableByCode(req.tableCode);

                    if (table != null) {
                        // Send game state to the requesting player
                        sendGameStateToPlayer(table, connection);
                        System.out.println("Server sent GameStateUpdate for table: " + req.tableCode);
                    } else {
                        System.out.println("Table not found for code: " + req.tableCode);
                    }
                    return;
                }

                if (object instanceof StartGameRequest) {
                    StartGameRequest req = (StartGameRequest) object;
                    Table table = tableManager.getTableByCode(req.tableCode);

                    StartGameResponse resp = new StartGameResponse();
                    if (table != null) {
                        // Check if this is the host (first player in the table)
                        if (table.getConnections().get(0) == connection) {
                            // Start the game
                            PokerGame pokerGame = table.getPokerGame();
//                            if (pokerGame.getGameState() == PokerGame.GameState.WAITING_FOR_PLAYERS &&
//                                pokerGame.getPlayers().size() >= 2) {
                            if (true) { // igore for now, debug TODO: add back later

                                pokerGame.startNewHand();
                                resp.success = true;
                                resp.message = "Game started successfully";

                                // send startgame response to all players
                                for (Connection playerConnection : table.getConnections()) {
                                    System.out.println("Sending StartGameResponse to player: " + playerConnection.getID()
                                        + " table has " + table.getConnections().size() + " connected players");
                                    StartGameResponse playerResponse = new StartGameResponse();
                                    playerResponse.success = true;
                                    playerResponse.message = "Game started successfully";
                                    playerConnection.sendTCP(playerResponse);
                                }

                                // Broadcast the updated game state to all players
                                broadcastGameStateToTable(table);
                            } else {
                                resp.success = false;
                                resp.message = "Need at least 2 players to start the game";
                            }
                        } else {
                            resp.success = false;
                            resp.message = "Only the host can start the game";
                        }
                    } else {
                        resp.success = false;
                        resp.message = "Table not found";
                    }

                    connection.sendTCP(resp);
                    return;
                }
            }
        });

        while (true) Thread.sleep(10000);
    }

    private static void broadcastGameStateToTable(Table table) {
        PokerGame pokerGame = table.getPokerGame();
        for (int i = 0; i < table.getConnections().size(); i++) {
            Connection connection = table.getConnections().get(i);
            if (i < pokerGame.getPlayers().size()) {
                Player player = pokerGame.getPlayers().get(i);
                GameStateUpdate update = PokerConverters.toGameStateUpdate(pokerGame, player);
                connection.sendTCP(update);
            }
        }
    }

    private static void sendGameStateToPlayer(Table table, Connection connection) {
        PokerGame pokerGame = table.getPokerGame();
        int playerIndex = getPlayerIndexInTable(table, connection);
        if (playerIndex != -1 && playerIndex < pokerGame.getPlayers().size()) {
            Player player = pokerGame.getPlayers().get(playerIndex);
            GameStateUpdate update = PokerConverters.toGameStateUpdate(pokerGame, player);
            connection.sendTCP(update);
        }
    }

    private static int getPlayerIndexInTable(Table table, Connection connection) {
        return table.getConnections().indexOf(connection);
    }

    private static Table findOrCreateDefaultTable() {
        // For backward compatibility, create a default table with standard blinds
        return tableManager.createTable(50, 100);
    }
}
