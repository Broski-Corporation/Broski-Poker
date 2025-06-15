package io.github.broskipoker.game.tests;

import com.esotericsoftware.kryonet.*;
import io.github.broskipoker.game.Player;
import io.github.broskipoker.shared.*;
import io.github.broskipoker.game.PokerGame;
import java.util.Scanner;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class TestClient {
    private Client client;
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

    public TestClient(String username) {
        this.username = username;
        this.client = new Client();
        this.actionScanner = new Scanner(System.in);
        NetworkRegistration.register(client.getKryo());
    }

    public TestClient(String username, boolean automaticMode) {
        this(username);
        this.automaticMode = automaticMode;
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
        else if (object instanceof JoinTableResponse) {
            JoinTableResponse resp = (JoinTableResponse) object;
            if (resp.success) {
                System.out.println("✅ " + username + " successfully joined table: " + resp.code);
            } else {
                System.out.println("❌ " + username + " failed to join table: " + resp.failReason);
            }
        }
        else if (object instanceof GameStateUpdate) {
            GameStateUpdate update = (GameStateUpdate) object;
            if (update.players.size() > 1 ) {
                displayGameState(update);
                checkIfMyTurn(update);
            }
        }
        else if (object instanceof LoginResponse) {
            LoginResponse resp = (LoginResponse) object;
            if (resp.success) {
                System.out.println("✅ " + username + " login successful: " + resp.message);
            } else {
                System.out.println("❌ " + username + " login failed: " + resp.message);
            }
        }
    }

    private void checkIfMyTurn(GameStateUpdate update) {
        // Assuming the update contains current player info
        // You might need to adjust this based on your actual GameStateUpdate structure
        boolean wasMyTurn = isMyTurn;

        // Check if it's my turn (adjust this logic based on your server's implementation)
        if (update.players.get(update.currentPlayerIndex).name != null) {
            currentPlayer = update.players.get(update.currentPlayerIndex).name;
            isMyTurn = currentPlayer.equals(username);
        } else {
            isMyTurn = false;
        }

        // If it just became my turn, start action input
        if (isMyTurn && !wasMyTurn && !waitingForAction) {
            if (automaticMode) {
                makeAutomaticMove();
            } else {
                startActionInput();
            }
        } else if (!isMyTurn && waitingForAction) {
            stopActionInput();
        }
    }

    private void makeAutomaticMove() {
        System.out.println("\n🤖 " + username + " making automatic move...");

        // Simulate thinking time
        try {
            Thread.sleep(1000 + random.nextInt(2000)); // 1-3 seconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        // Make a random decision
        int decision = random.nextInt(100);

        if (decision < 30) { // 30% chance to fold
            performAction(PokerGame.PlayerAction.FOLD, 0);
            System.out.println("🤖 " + username + " folded!");
        } else if (decision < 70) { // 40% chance to call
            performAction(PokerGame.PlayerAction.CALL, 0);
            System.out.println("🤖 " + username + " called!");
        } else if (decision < 95) { // 25% chance to raise
            int raiseAmount = 100 + random.nextInt(500); // Raise between 100-600
            performAction(PokerGame.PlayerAction.RAISE, raiseAmount);
            System.out.println("🤖 " + username + " raised by " + raiseAmount + "!");
        } else { // 5% chance to go all-in
            performAction(PokerGame.PlayerAction.RAISE, 10000);
            System.out.println("🤖 " + username + " went all-in!");
        }

        isMyTurn = false;
    }

    private void startActionInput() {
        waitingForAction = true;
        System.out.println("\n🎯 " + username + " - IT'S YOUR TURN! Choose an action:");
        displayAvailableActions();

        actionThread = new Thread(() -> {
            while (waitingForAction && isMyTurn && connected) {
                try {
                    if (System.in.available() > 0) {
                        String input = actionScanner.nextLine().trim().toLowerCase();
                        System.out.printf("%s received input: %s%n", username, input);
                        handlePlayerInput(input);
                        break;
                    }
                    Thread.sleep(100);
                } catch (Exception e) {
                    if (waitingForAction) {
                        System.err.println("Error in action thread for " + username + ": " + e.getMessage());
                    }
                    break;
                }
            }
        });
        actionThread.setDaemon(true);
        actionThread.start();
    }

    private void stopActionInput() {
        waitingForAction = false;
        if (actionThread != null) {
            actionThread.interrupt();
        }
    }

    private void displayAvailableActions() {
        System.out.println("Available actions:");
        System.out.println("  [c] Call");
        System.out.println("  [r] Raise");
        System.out.println("  [f] Fold");
        System.out.println("  [a] All-in");
        System.out.print("Enter your choice: ");
    }

    private void handlePlayerInput(String input) {
        if (!isMyTurn) {
            System.out.println("❌ It's not your turn!");
            return;
        }

        switch (input) {
            case "c":
                performAction(PokerGame.PlayerAction.CALL, 0);
                System.out.println("🎯 " + username + " called!");
                break;
            case "f":
                performAction(PokerGame.PlayerAction.FOLD, 0);
                System.out.println("🎯 " + username + " folded!");
                break;
            case "r":
                handleRaiseAction();
                return; // Don't stop waiting yet, handleRaiseAction will do it
            case "a":
                performAction(PokerGame.PlayerAction.RAISE, 50);
                System.out.println("🎯 " + username + " went all-in!");
                break;
            default:
                System.out.println("❌ Invalid action! Use: c (call), r (raise), f (fold), a (all-in)");
                displayAvailableActions();
                return; // Keep waiting for valid input
        }

        waitingForAction = false;
        isMyTurn = false; // We made our move
    }

    private void handleRaiseAction() {
        System.out.print("Enter raise amount: ");
        try {
            // Wait for the next line of input
            String amountStr = actionScanner.nextLine().trim();
            int amount = Integer.parseInt(amountStr);
            performAction(PokerGame.PlayerAction.RAISE, amount);
            System.out.println("🎯 " + username + " raised by " + amount + "!");
            waitingForAction = false;
            isMyTurn = false;
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid amount! Please enter a number:");
            handleRaiseAction(); // Try again
        }
    }

    private void displayGameState(GameStateUpdate update) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🎮 POKER GAME STATE (" + username + ")");
        System.out.println("=".repeat(60));
        System.out.println("📊 Game Status: " + update.gameState);

        if (currentPlayer != null && !currentPlayer.isEmpty()) {
            if (currentPlayer.equals(username)) {
                System.out.println("🎯 >>> " + username + " - IT'S YOUR TURN! <<<");
            } else {
                System.out.println("⏳ " + username + " waiting for: " + currentPlayer);
            }
        }

        System.out.println("\n👥 Players:");
        for(int i = 0; i < update.players.size(); i++) {
            PlayerInfo player = update.players.get(i);
            String indicator = player.name.equals(username) ? " (YOU)" : "";
            String turnIndicator = (currentPlayer != null &&
                currentPlayer.equals(player.name)) ? " 🎯" : "";
            System.out.println("  " + (i+1) + ". " + player.name + indicator + turnIndicator);
            // Add more player info if available in your Player class
            // System.out.println("     Chips: " + player.chips);
        }

        // Add more game state info if available
        // if (update.pot > 0) {
        //     System.out.println("\n💰 Pot: " + update.pot);
        // }

        System.out.println("=".repeat(60));

        if (!isMyTurn && currentPlayer != null && !currentPlayer.equals(username)) {
            System.out.println("⏳ " + username + " waiting for " + currentPlayer + " to make a move...");
        }
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

    public void performAction(PokerGame.PlayerAction action, int amount) {
        if (!connected || !client.isConnected()) {
            System.out.println("❌ " + username + " not connected to server!");
            return;
        }

        PlayerAction playerAction = new PlayerAction();
        playerAction.action = action;
        playerAction.amount = amount;

        System.out.println("📤 " + username + " performing action: " + action + " (amount: " + amount + ")");
        client.sendTCP(playerAction);
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

    // NEW: 10-Client Automatic Test
    private static void testWith10Clients() {
        System.out.println("=== 10-Client Automatic Poker Test ===\n");

        List<TestClient> clients = new ArrayList<>();
        String[] playerNames = {
            "Alice", "Bob", "Charlie", "Diana", "Eve",
            "Frank", "Grace", "Henry", "Ivy", "Jack"
        };

        String serverHost = "104.248.45.171";
        int serverPort = 8080;

        try {
            System.out.println("🔗 Creating and connecting 10 clients...");

            // Create all clients with automatic mode enabled
            for (String name : playerNames) {
                TestClient client = new TestClient(name, true); // Enable automatic mode
                clients.add(client);
            }

            // Use CountDownLatch to wait for all connections
            CountDownLatch connectionLatch = new CountDownLatch(clients.size());

            // Connect all clients simultaneously
            for (TestClient client : clients) {
                new Thread(() -> {
                    client.connect(serverHost, serverPort);
                    connectionLatch.countDown();
                }).start();
            }

            // Wait for all clients to connect (max 30 seconds)
            if (!connectionLatch.await(30, TimeUnit.SECONDS)) {
                System.out.println("❌ Timeout waiting for all clients to connect!");
                return;
            }

            Thread.sleep(3000); // Give connections time to stabilize

            // Count successful connections
            int connectedCount = 0;
            for (TestClient client : clients) {
                if (client.isConnected()) {
                    connectedCount++;
                }
            }

            System.out.println("✅ " + connectedCount + " out of " + clients.size() + " clients connected successfully");

            if (connectedCount < 2) {
                System.out.println("❌ Need at least 2 connected clients to start a game!");
                return;
            }

            // First connected client creates the table
            TestClient tableCreator = null;
            for (TestClient client : clients) {
                if (client.isConnected()) {
                    tableCreator = client;
                    break;
                }
            }

            if (tableCreator != null) {
                System.out.println("🎯 " + tableCreator.username + " creating table...");
                tableCreator.createTable(50, 100, 10000);
                Thread.sleep(5000); // Wait for table creation

                String tableCode = tableCreator.getTableCode();
                if (tableCode != null) {
                    System.out.println("✅ Table created with code: " + tableCode);

                    // All other connected clients join the table
                    List<Thread> joinThreads = new ArrayList<>();
                    for (TestClient client : clients) {
                        if (client.isConnected() && client != tableCreator) {
                            Thread joinThread = new Thread(() -> {
                                try {
                                    Thread.sleep(1000 + new Random().nextInt(2000)); // Stagger joins
                                    client.joinTable(tableCode, 10000);
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                }
                            });
                            joinThreads.add(joinThread);
                            joinThread.start();
                        }
                    }

                    // Wait for all joins to complete
                    for (Thread joinThread : joinThreads) {
                        joinThread.join(10000); // Max 10 seconds per join
                    }

                    Thread.sleep(5000); // Wait for all joins to be processed

                    System.out.println("\n🎮 10-Client automatic game started!");
                    System.out.println("🤖 All clients will play automatically using AI decisions");
                    System.out.println("⏳ Game will run for 2 minutes to demonstrate concurrent gameplay...\n");

                    // Let the game run for 2 minutes
                    Thread.sleep(120000);

                } else {
                    System.out.println("❌ Table code not received from server!");
                }
            }

        } catch (InterruptedException e) {
            System.err.println("Test interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.err.println("Test error: " + e.getMessage());
        } finally {
            System.out.println("\n🧹 Cleaning up all clients...");
            for (TestClient client : clients) {
                if (client != null) {
                    client.disconnect();
                }
            }
            System.out.println("✅ 10-Client automatic test completed");
        }
    }

    // IMPROVED MAIN METHOD WITH TURN-BASED GAMEPLAY
    public static void main(String[] args) {
        System.out.println("=== Turn-Based Poker Test Client ===");
        System.out.println("Choose test mode:");
        System.out.println("1. Automatic test (10 clients simultaneous)");
        System.out.println("2. Interactive test (turn-based)");

        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter choice (1 or 2): ");

        try {
            int choice = scanner.nextInt();
            scanner.nextLine();

            if (choice == 1) {
                testWith10Clients();
            } else if (choice == 2) {
                interactiveTest();
            } else {
                System.out.println("Invalid choice!");
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }

    public static void interactiveTest() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter your username: ");
        String username = scanner.nextLine();
        TestClient client = new TestClient(username);

        try {
            System.out.println("🔗 Connecting to server...");
            client.connect();
            Thread.sleep(3000);

            if (!client.isConnected()) {
                System.out.println("❌ Failed to connect to server!");
                return;
            }

            System.out.println("\n✅ Connected! Choose an option:");
            System.out.println("1. Create new table");
            System.out.println("2. Join existing table");

            int choice = scanner.nextInt();
            scanner.nextLine();

            if (choice == 1) {
                System.out.print("Enter small blind: ");
                int smallBlind = scanner.nextInt();
                System.out.print("Enter big blind: ");
                int bigBlind = scanner.nextInt();
                System.out.print("Enter your chips: ");
                int chips = scanner.nextInt();
                scanner.nextLine(); // consume newline

                client.createTable(smallBlind, bigBlind, chips);
                Thread.sleep(4000);

                if (client.getTableCode() != null) {
                    System.out.println("✅ Your table code is: " + client.getTableCode());
                    System.out.println("Share this code with other players to join!");
                } else {
                    System.out.println("❌ Table creation failed or timed out");
                    return;
                }

            } else if (choice == 2) {
                System.out.print("Enter table code: ");
                String code = scanner.nextLine().trim();
                System.out.print("Enter your chips: ");
                int chips = scanner.nextInt();
                scanner.nextLine(); // consume newline

                client.joinTable(code, chips);
                Thread.sleep(4000);
            }

            System.out.println("\n🎮 Game started! The client will automatically handle turns.");
            System.out.println("When it's your turn, you'll see action options and can make your move.");
            System.out.println("Type 'quit' and press Enter to exit the game.\n");

            boolean playing = true;
            while (playing && client.isConnected()) {
                String input = scanner.nextLine().trim();

                if ("quit".equalsIgnoreCase(input)) {
                    playing = false;
                    System.out.println("🚪 Exiting game...");
                }
                // All turn-based input is handled automatically in the background
                // The main thread just waits for 'quit' command
            }

        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
        } finally {
            client.disconnect();
            scanner.close();
        }
    }
}
