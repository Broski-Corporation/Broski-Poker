package io.github.broskipoker.server;

import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import io.github.broskipoker.game.PokerGame;
import io.github.broskipoker.shared.*;
import io.github.broskipoker.ui.LobbyPanel;
import java.util.List;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.function.Consumer;

public class ClientConnection {

    private Client client;
    private LobbyPanel lobbyPanel;
    private String username;
    private String tableCode;
    private volatile boolean connected = false;
    private volatile boolean shouldUpdate = true;
    private Thread updateThread;

    // Turn-based gameplay fields
    private volatile boolean isMyTurn = false;
    private volatile boolean waitingForAction = false;
    private String currentPlayer = "";
    private Scanner actionScanner;
    private Thread actionThread;

    // Automatic gameplay fields
    private boolean automaticMode = false;
    private Random random = new Random();

    private List<Consumer<GameStateUpdate>> gameStateListeners = new ArrayList<>();

    public ClientConnection(String username) {
        this.username = username;
        this.client = new Client();
        NetworkRegistration.register(client.getKryo());
    }

    public void connect() {
        connect("104.248.45.171", 8080);
    }

    public void connect(String host, int port) {
        try {
            setupListener();
            client.start();

            // Start a temporary update thread to run during connect()
            Thread tempUpdateThread = new Thread(() -> {
                long startTime = System.currentTimeMillis();
                while (!client.isConnected() && System.currentTimeMillis() - startTime < 10000) {
                    try {
                        client.update(10);
                    } catch (IOException e) {
                        System.err.println("Temporary update error: " + e.getMessage());
                    }
                }
            });
            tempUpdateThread.setDaemon(true);
            tempUpdateThread.start();

            System.out.println("Attempting to connect to " + host + ":" + port + "...");
            client.connect(10000, host, port);

            tempUpdateThread.join();

            if (client.isConnected()) {
                connected = true;
                System.out.println("✅ Connected to server as: " + username + " at " + host + ":" + port);
                startUpdateThread();
            } else {
                System.out.println("❌ Failed to establish connection");
                connected = false;
            }

        } catch (IOException e) {
            System.err.println("❌ IO Exception connecting to server: " + e.getMessage());
            connected = false;
        } catch (Exception e) {
            System.err.println("❌ Failed to connect to server: " + e.getMessage());
            connected = false;
        }
    }
    private void startUpdateThread() {
        shouldUpdate = true;
        updateThread = new Thread(() -> {
            System.out.println("Update thread started for " + username);
            while (shouldUpdate) {
                try {
                    client.update(50);
                } catch (IOException e) {
                    if (shouldUpdate) {
                        System.err.println("Update thread IO error for " + username + ": " + e.getMessage());
                    }
                    break;
                } catch (Exception e) {
                    if (shouldUpdate) {
                        System.err.println("Update thread error for " + username + ": " + e.getMessage());
                    }
                    break;
                }
            }
            System.out.println("Update thread stopped for " + username);
        });
        updateThread.setDaemon(true);
        updateThread.start();
    }

    public void addGameStateListener(Consumer<GameStateUpdate> listener) {
        gameStateListeners.add(listener);
    }

    public void requestGameStateUpdate() {
        if (isConnected()) {
            GameStateRequest request = new GameStateRequest();
            request.tableCode = tableCode;
            client.sendTCP(request);
            System.out.println("📤 " + username + " requesting game state update for table: " + tableCode);
        }
    }

    public void requestStartGame() {
        if (isConnected() && tableCode != null) {
            StartGameRequest request = new StartGameRequest();
            request.tableCode = tableCode;
            client.sendTCP(request);
            System.out.println("📤 " + username + " requesting to start game for table: " + tableCode);
        } else {
            System.out.println("❌ Not connected or no table code - can't start game");
        }
    }

    private void setupListener() {
        client.addListener(new Listener() {
            @Override
            public void connected(Connection connection) {
                System.out.println("✅ Client listener: " + username + " connected to server");
                connected = true;
            }

            @Override
            public void disconnected(Connection connection) {
                System.out.println("❌ Client listener: " + username + " disconnected from server");
                connected = false;
                isMyTurn = false;
                waitingForAction = false;
            }

            @Override
            public void received(Connection connection, Object object) {
                handleServerMessage(object);
            }
        });
    }


    private void handleServerMessage(Object object) {
        if (object instanceof CreateTableResponse) {
            CreateTableResponse resp = (CreateTableResponse) object;
            if (resp.success) {
                tableCode = resp.code;
                System.out.println("✅ " + username + " created table successfully! Code: " + tableCode);
            } else {
                System.out.println("❌ " + username + " failed to create table: " + resp.failReason);
            }
        }
        else if (object instanceof GameStateUpdate) {
            GameStateUpdate update = (GameStateUpdate) object;
            System.out.println("📥 Received GameStateUpdate with " + update.players.size() + " players");

            // notify all registered listeners about the game state update
            for(Consumer<GameStateUpdate> listener : gameStateListeners) {
                listener.accept(update);
            }

            // Always update the lobby panel regardless of player count
            if (lobbyPanel != null) {
                lobbyPanel.onGameStateUpdate(update);
            }
        }
        else if (object instanceof JoinTableResponse) {
            JoinTableResponse resp = (JoinTableResponse) object;
            if (resp.success) {
                System.out.println("✅ " + username + " successfully joined table: " + resp.code);
            } else {
                System.out.println("❌ " + username + " failed to join table: " + resp.failReason);
            }
        }
//        else if (object instanceof GameStateUpdate) {
//            GameStateUpdate update = (GameStateUpdate) object;
//            if (update.players.size() > 1 ) {
////                TODO:
//            }
//        }
        else if (object instanceof LoginResponse) {
            LoginResponse resp = (LoginResponse) object;
            if (resp.success) {
                System.out.println("✅ " + username + " login successful: " + resp.message);
            } else {
                System.out.println("❌ " + username + " login failed: " + resp.message);
            }
        }
        else if (object instanceof StartGameResponse) {
            StartGameResponse resp = (StartGameResponse) object;
            if (resp.success) {
                System.out.println("✅ Game started successfully: " + resp.message);
                // Notify the game that it should transition to gameplay
                if (lobbyPanel != null) {
                    Gdx.app.postRunnable(() -> {
                       lobbyPanel.onGameStarted();
                    });
                }
            } else {
                System.out.println("❌ Failed to start game: " + resp.message);
                // Optionally show an error message in the UI
            }
        }
    }

    public void setLobbyPanel(LobbyPanel lobbyPanel)
    {
        this.lobbyPanel = lobbyPanel;
    }

    public void createTable(int smallBlind, int bigBlind, int chips) {
        if (!connected || !client.isConnected()) {
            System.out.println("❌ " + username + " not connected to server!");
            return;
        }

        CreateTableRequest request = new CreateTableRequest();
        request.username = username;
        request.smallBlind = smallBlind;
        request.bigBlind = bigBlind;
        request.chips = chips;

        System.out.println("📤 " + username + " creating table with blinds: " + smallBlind + "/" + bigBlind);
        client.sendTCP(request);
    }

    public void joinTable(String code, int chips) {
        if (!connected || !client.isConnected()) {
            System.out.println("❌ " + username + " not connected to server!");
            return;
        }

        JoinTableRequest request = new JoinTableRequest();
        request.code = code.toUpperCase();
        request.username = username;
        request.chips = chips;

        System.out.println("📤 " + username + " joining table with code: " + code);
        client.sendTCP(request);
    }

    public void sendAction(PlayerAction action) {
        if (isConnected()) {
            System.out.println("📤 " + username + " sending action: " + action.action);
            client.sendTCP(action);
        } else {
            System.out.println("❌ Not connected - can't send action");
        }
    }

    public void disconnect() {
        System.out.println("Disconnecting " + username + "...");
        connected = false;
        shouldUpdate = false;
        waitingForAction = false;
        isMyTurn = false;

        if (actionThread != null) {
            actionThread.interrupt();
        }

        if (updateThread != null) {
            try {
                updateThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        if (client != null) {
            try {
                client.close();
                System.out.println("✅ " + username + " disconnected from server");
            } catch (Exception e) {
                System.err.println("❌ Error during disconnect for " + username + ": " + e.getMessage());
            }
        }
    }

    public String getTableCode() {
        return tableCode;
    }

    public boolean isConnected() {
        return connected && client != null && client.isConnected();
    }

    public void setTableCode(String tableCode) {
        this.tableCode = tableCode;
    }
}

