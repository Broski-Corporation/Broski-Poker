package io.github.broskipoker.game.tests;

import com.esotericsoftware.kryonet.*;
import io.github.broskipoker.game.Player;
import io.github.broskipoker.shared.*;
import io.github.broskipoker.game.PokerGame;
import java.util.Scanner;
import java.io.IOException;

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

    public TestClient(String username) {
        this.username = username;
        this.client = new Client();
        this.actionScanner = new Scanner(System.in);
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
            System.out.println("Update thread started");
            while (shouldUpdate) {
                try {
                    client.update(50);
                } catch (IOException e) {
                    if (shouldUpdate) {
                        System.err.println("Update thread IO error: " + e.getMessage());
                    }
                    break;
                } catch (Exception e) {
                    if (shouldUpdate) {
                        System.err.println("Update thread error: " + e.getMessage());
                    }
                    break;
                }
            }
            System.out.println("Update thread stopped");
        });
        updateThread.setDaemon(true);
        updateThread.start();
    }

    private void setupListener() {
        client.addListener(new Listener() {
            @Override
            public void connected(Connection connection) {
                System.out.println("✅ Client listener: Connected to server");
                connected = true;
            }

            @Override
            public void disconnected(Connection connection) {
                System.out.println("❌ Client listener: Disconnected from server");
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
                System.out.println("✅ Table created successfully! Code: " + tableCode);
            } else {
                System.out.println("❌ Failed to create table: " + resp.failReason);
            }
        }
        else if (object instanceof JoinTableResponse) {
            JoinTableResponse resp = (JoinTableResponse) object;
            if (resp.success) {
                System.out.println("✅ Successfully joined table: " + resp.code);
            } else {
                System.out.println("❌ Failed to join table: " + resp.failReason);
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
                System.out.println("✅ Login successful: " + resp.message);
            } else {
                System.out.println("❌ Login failed: " + resp.message);
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
            startActionInput();
        } else if (!isMyTurn && waitingForAction) {
            stopActionInput();
        }
    }

    private void startActionInput() {
        waitingForAction = true;
        System.out.println("\n🎯 IT'S YOUR TURN! Choose an action:");
        displayAvailableActions();

        actionThread = new Thread(() -> {
            while (waitingForAction && isMyTurn && connected) {
                try {
                    if (System.in.available() > 0) {
                        String input = actionScanner.nextLine().trim().toLowerCase();
                        System.out.printf("Received input: %s%n", input);
                        handlePlayerInput(input);
                        break;
                    }
                    Thread.sleep(100);
                } catch (Exception e) {
                    if (waitingForAction) {
                        System.err.println("Error in action thread: " + e.getMessage());
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
                System.out.println("🎯 You called!");
                break;
            case "f":
                performAction(PokerGame.PlayerAction.FOLD, 0);
                System.out.println("🎯 You folded!");
                break;
            case "r":
                handleRaiseAction();
                return; // Don't stop waiting yet, handleRaiseAction will do it
            case "a":
                performAction(PokerGame.PlayerAction.RAISE, 50);
                System.out.println("🎯 You went all-in!");
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
            System.out.println("🎯 You raised by " + amount + "!");
            waitingForAction = false;
            isMyTurn = false;
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid amount! Please enter a number:");
            handleRaiseAction(); // Try again
        }
    }

    private void displayGameState(GameStateUpdate update) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🎮 POKER GAME STATE");
        System.out.println("=".repeat(60));
        System.out.println("📊 Game Status: " + update.gameState);

        if (currentPlayer != null && !currentPlayer.isEmpty()) {
            if (currentPlayer.equals(username)) {
                System.out.println("🎯 >>> IT'S YOUR TURN! <<<");
            } else {
                System.out.println("⏳ Waiting for: " + currentPlayer);
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
            System.out.println("⏳ Waiting for " + currentPlayer + " to make a move...");
        }
    }

    public void createTable(int smallBlind, int bigBlind, int chips) {
        if (!connected || !client.isConnected()) {
            System.out.println("❌ Not connected to server!");
            return;
        }

        CreateTableRequest request = new CreateTableRequest();
        request.username = username;
        request.smallBlind = smallBlind;
        request.bigBlind = bigBlind;
        request.chips = chips;

        System.out.println("📤 Creating table with blinds: " + smallBlind + "/" + bigBlind);
        client.sendTCP(request);
    }

    public void joinTable(String code, int chips) {
        if (!connected || !client.isConnected()) {
            System.out.println("❌ Not connected to server!");
            return;
        }

        JoinTableRequest request = new JoinTableRequest();
        request.code = code.toUpperCase();
        request.username = username;
        request.chips = chips;

        System.out.println("📤 Joining table with code: " + code);
        client.sendTCP(request);
    }

    public void performAction(PokerGame.PlayerAction action, int amount) {
        if (!connected || !client.isConnected()) {
            System.out.println("❌ Not connected to server!");
            return;
        }

        PlayerAction playerAction = new PlayerAction();
        playerAction.action = action;
        playerAction.amount = amount;

        System.out.println("📤 Performing action: " + action + " (amount: " + amount + ")");
        client.sendTCP(playerAction);
    }

    public void disconnect() {
        System.out.println("Disconnecting...");
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
                System.out.println("✅ Disconnected from server");
            } catch (Exception e) {
                System.err.println("❌ Error during disconnect: " + e.getMessage());
            }
        }
    }

    public String getTableCode() {
        return tableCode;
    }

    public boolean isConnected() {
        return connected && client != null && client.isConnected();
    }

    // IMPROVED MAIN METHOD WITH TURN-BASED GAMEPLAY
    public static void main(String[] args) {
        System.out.println("=== Turn-Based Poker Test Client ===");
        System.out.println("Choose test mode:");
        System.out.println("1. Automatic test (2 clients turn-based)");
        System.out.println("2. Interactive test (turn-based)");

        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter choice (1 or 2): ");

        try {
            int choice = scanner.nextInt();
            scanner.nextLine();

            if (choice == 1) {
//                testTurnBasedGameplay();
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

//    private static void testTurnBasedGameplay() {
//        TestClient alice = new TestClient("Alice");
//        TestClient bob = new TestClient("Bob");
//
//        try {
//            System.out.println("=== Turn-Based Poker Test ===\n");
//
//            String serverHost = "104.248.45.171";
//            int serverPort = 8080;
//
//            // Connect Alice
//            System.out.println("🔗 Connecting Alice to " + serverHost + ":" + serverPort);
//            alice.connect(serverHost, serverPort);
//            Thread.sleep(3000);
//
//            if (!alice.isConnected()) {
//                System.out.println("❌ Alice failed to connect!");
//                return;
//            }
//
//            // Alice creates table
//            System.out.println("🎯 Alice creating table...");
//            alice.createTable(50, 100, 10000);
//            Thread.sleep(4000);
//
//            String tableCode = alice.getTableCode();
//            if (tableCode != null) {
//                System.out.println("✅ Table created with code: " + tableCode);
//
//                // Connect Bob
//                System.out.println("🔗 Connecting Bob to " + serverHost + ":" + serverPort);
//                bob.connect(serverHost, serverPort);
//                Thread.sleep(3000);
//
//                if (!bob.isConnected()) {
//                    System.out.println("❌ Bob failed to connect!");
//                    return;
//                }
//
//                // Bob joins table
//                System.out.println("🎯 Bob joining table: " + tableCode);
//                bob.joinTable(tableCode, 10000);
//                Thread.sleep(4000);
//
//                System.out.println("\n🎮 Turn-based game started!");
//                System.out.println("Players will automatically make moves when it's their turn...");
//
//                // Simulate some automatic moves for testing
//                Thread.sleep(5000);
//
//                // Let the game run for a while to see turn-based gameplay
//                System.out.println("⏳ Game running... Watch for turn notifications...");
//                Thread.sleep(30000);
//
//            } else {
//                System.out.println("❌ Table code not received from server!");
//            }
//
//        } catch (InterruptedException e) {
//            System.err.println("Test interrupted: " + e.getMessage());
//        } catch (Exception e) {
//            System.err.println("Test error: " + e.getMessage());
//        } finally {
//            System.out.println("🧹 Cleaning up...");
//            alice.disconnect();
//            bob.disconnect();
//            System.out.println("✅ Turn-based test completed");
//        }
//    }

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
