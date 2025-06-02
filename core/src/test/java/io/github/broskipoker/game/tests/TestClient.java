package io.github.broskipoker.game.tests;

import com.esotericsoftware.kryonet.*;
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

    public TestClient(String username) {
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
            client.connect(10000, host, port); // triggers TCP registration

            tempUpdateThread.join(); // wait for update thread to finish

            if (client.isConnected()) {
                connected = true;
                System.out.println("✅ Connected to server as: " + username + " at " + host + ":" + port);

                // Start the regular update thread
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
                    client.update(50); // Update every 50ms - less frequent but more stable
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
            }

            @Override
            public void received(Connection connection, Object object) {
                System.out.println("📨 Received: " + object.getClass().getSimpleName());
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
            displayGameState(update);
        }
        else if (object instanceof LoginResponse) {
            LoginResponse resp = (LoginResponse) object;
            if (resp.success) {
                System.out.println("✅ Login successful: " + resp.message);
            } else {
                System.out.println("❌ Login failed: " + resp.message);
            }
        }
        else {
            System.out.println("📨 Unknown message type: " + object.getClass().getSimpleName());
        }
    }

    private void displayGameState(GameStateUpdate update) {
        System.out.println("\n=== GAME STATE UPDATE ===");
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

        if (updateThread != null) {
            try {
                updateThread.join(1000); // Wait max 1 second for thread to finish
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

    // Main method pentru testare
    public static void main(String[] args) {
        System.out.println("=== Poker Test Client ===");
        System.out.println("Choose test mode:");
        System.out.println("1. Automatic test (2 clients)");
        System.out.println("2. Interactive test");

        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter choice (1 or 2): ");

        try {
            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            if (choice == 1) {
                testCreateAndJoinTable();
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

    private static void testCreateAndJoinTable() {
        TestClient client1 = new TestClient("Alice");
        TestClient client2 = new TestClient("Bob");

        try {
            System.out.println("=== Testing Table Creation and Join ===\n");

            String serverHost = "104.248.45.171";
            int serverPort = 8080;

            // Connect Alice
            System.out.println("🔗 Connecting Alice to " + serverHost + ":" + serverPort);
            client1.connect(serverHost, serverPort);
            Thread.sleep(3000); // Wait longer for stable connection

            if (!client1.isConnected()) {
                System.out.println("❌ Alice failed to connect!");
                return;
            }

            // Alice creates table
            System.out.println("🎯 Alice creating table...");
            client1.createTable(50, 100, 10000);
            Thread.sleep(4000); // Wait for table creation

            String tableCode = client1.getTableCode();
            if (tableCode != null) {
                System.out.println("✅ Table created with code: " + tableCode);

                // Connect Bob
                System.out.println("🔗 Connecting Bob to " + serverHost + ":" + serverPort);
                client2.connect(serverHost, serverPort);
                Thread.sleep(3000);

                if (!client2.isConnected()) {
                    System.out.println("❌ Bob failed to connect!");
                    return;
                }

                // Bob joins table
                System.out.println("🎯 Bob joining table: " + tableCode);
                client2.joinTable(tableCode, 10000);
                Thread.sleep(4000);

                // Test some actions
                System.out.println("\n=== Testing Game Actions ===");
                Thread.sleep(2000);

                System.out.println("🎲 Alice performing action...");
                client1.performAction(PokerGame.PlayerAction.CALL, 0);
                Thread.sleep(3000);

                System.out.println("🎲 Bob performing action...");
                client2.performAction(PokerGame.PlayerAction.CALL, 0);
                Thread.sleep(3000);

            } else {
                System.out.println("❌ Table code not received from server!");
            }

            // Keep running to see final updates
            System.out.println("⏳ Waiting for final updates...");
            Thread.sleep(5000);

        } catch (InterruptedException e) {
            System.err.println("Test interrupted: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Test error: " + e.getMessage());
        } finally {
            System.out.println("🧹 Cleaning up...");
            client1.disconnect();
            client2.disconnect();
            System.out.println("✅ Test completed");
        }
    }

    public static void interactiveTest() {
        TestClient client = new TestClient("TestUser");
        Scanner scanner = new Scanner(System.in);

        try {
            System.out.print("Enter server host (104.248.45.171): ");
            String host = scanner.nextLine().trim();
            if (host.isEmpty()) host = "104.248.45.171";

            System.out.print("Enter server port (8080): ");
            String portStr = scanner.nextLine().trim();
            int port = portStr.isEmpty() ? 8080 : Integer.parseInt(portStr);

            System.out.println("🔗 Connecting to " + host + ":" + port);
            client.connect(host, port);
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

                client.createTable(smallBlind, bigBlind, chips);
                Thread.sleep(4000);

                if (client.getTableCode() != null) {
                    System.out.println("✅ Your table code is: " + client.getTableCode());
                } else {
                    System.out.println("❌ Table creation failed or timed out");
                }

            } else if (choice == 2) {
                System.out.print("Enter table code: ");
                String code = scanner.nextLine().trim();
                System.out.print("Enter your chips: ");
                int chips = scanner.nextInt();

                client.joinTable(code, chips);
                Thread.sleep(4000);
            }

            System.out.println("\n⏳ Listening for updates... Press Enter to exit");
            scanner.nextLine();

        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
        } finally {
            client.disconnect();
            scanner.close();
        }
    }
}
