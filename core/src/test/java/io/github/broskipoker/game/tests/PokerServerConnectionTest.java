package io.github.broskipoker.game.tests;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import io.github.broskipoker.shared.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class PokerServerConnectionTest {

    private Client client;
    private Thread updateThread;
    private AtomicBoolean running;
    private static final String HOST = "104.248.45.171"; // Change to your server address
    private static final int PORT = 8080;
    private static final int TIMEOUT = 5000;

    @BeforeEach
    public void setup() throws IOException {
        client = new Client();
        NetworkRegistration.register(client.getKryo());
        client.start();
        running = new AtomicBoolean(true);
    }

    @AfterEach
    public void tearDown() {
        running.set(false);
        if (updateThread != null) {
            try {
                updateThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        if (client != null) {
            client.close();
        }
    }

    private void startUpdateThread(Client clientToUpdate) {
        updateThread = new Thread(() -> {
            System.out.println("Update thread started for client: " + clientToUpdate.getID()); // Added client ID for clarity
            while (running.get()) {
                try {
                    clientToUpdate.update(10);
                } catch (IOException e) {
                    if (clientToUpdate.isConnected() && running.get()) {
                        System.err.println("CLIENT UPDATE THREAD IOException for client " + clientToUpdate.getID() + ": " + e.getMessage());
                        e.printStackTrace(); // It's good to see the stack trace for IOExceptions too
                    }
                } catch (Throwable t) { // Catch KryoException and other RuntimeExceptions
                    System.err.println("CLIENT UPDATE THREAD CRITICAL ERROR for client " + clientToUpdate.getID() + ": " + t.getMessage());
                    t.printStackTrace(); // This will print the stack trace for KryoExceptions
                    // Consider stopping the 'running' loop or re-throwing if appropriate.
                    // For testing, logging it is crucial for diagnosis.
                    // running.set(false); // Optionally stop the thread on critical error, or let it break
                    break; // Exit loop if a critical error occurs
                }
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            System.out.println("Update thread stopped for client: " + clientToUpdate.getID()); // Added client ID
        });
        updateThread.setDaemon(true);
        updateThread.start();
    }

    private boolean connectToServer(Client clientToConnect) {
        try {
            // Start the update thread first
            startUpdateThread(clientToConnect);

            // Give the update thread more time to initialize
            Thread.sleep(300);  // Increased from 100ms

            // Now attempt the connection with more detailed logging
            System.out.println("Attempting to connect to " + HOST + ":" + PORT);
            clientToConnect.connect(TIMEOUT, HOST, PORT);

            // Give more time for the connection to fully establish
            Thread.sleep(500);  // Increased from 200ms

            // Verify the connection is actually established
            if (clientToConnect.isConnected()) {
                System.out.println("Successfully connected to server");
                return true;
            } else {
                System.err.println("Connection appeared to succeed but client is not connected");
                return false;
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Connection failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Test
    public void testCreateTable() throws IOException, InterruptedException {
        // Setup response handler
        CountDownLatch responseLatch = new CountDownLatch(1);
        AtomicReference<CreateTableResponse> responseRef = new AtomicReference<>();

        client.addListener(new Listener() {
            @Override
            public void received(Connection connection, Object object) {
                if (object instanceof CreateTableResponse) {
                    responseRef.set((CreateTableResponse) object);
                    responseLatch.countDown();
                }
            }
        });

        // Connect to server
        assertTrue(connectToServer(client), "Failed to connect to server");

        // Send create table request
        CreateTableRequest request = new CreateTableRequest();
        request.username = "TestPlayer";
        request.chips = 5000;
        request.smallBlind = 25;
        request.bigBlind = 50;
        client.sendTCP(request);

        // Wait for response
        boolean received = responseLatch.await(3, TimeUnit.SECONDS);

        // Assertions
        assertTrue(received, "Did not receive response in time");
        CreateTableResponse response = responseRef.get();
        assertNotNull(response, "Response should not be null");
        assertTrue(response.success, "Table creation should succeed");
        assertNotNull(response.code, "Table code should not be null");
        assertFalse(response.code.isEmpty(), "Table code should not be empty");
    }

    @Test
    public void testJoinTable() throws IOException, InterruptedException {
        // First create a table to join
        CountDownLatch createLatch = new CountDownLatch(1);
        AtomicReference<String> tableCodeRef = new AtomicReference<>();

        client.addListener(new Listener() {
            @Override
            public void received(Connection connection, Object object) {
                if (object instanceof CreateTableResponse) {
                    CreateTableResponse resp = (CreateTableResponse) object;
                    tableCodeRef.set(resp.code);
                    createLatch.countDown();
                }
            }
        });

        // Connect to server
        assertTrue(connectToServer(client), "Failed to connect to server");

        // Create table first
        CreateTableRequest createRequest = new CreateTableRequest();
        createRequest.username = "TableCreator";
        createRequest.chips = 5000;
        createRequest.smallBlind = 25;
        createRequest.bigBlind = 50;
        client.sendTCP(createRequest);

        // Wait for table creation
        assertTrue(createLatch.await(3, TimeUnit.SECONDS), "Table creation timed out");
        String tableCode = tableCodeRef.get();

        // Setup a second client to join the table
        Client secondClient = new Client();
        NetworkRegistration.register(secondClient.getKryo());
        secondClient.start();

        CountDownLatch joinLatch = new CountDownLatch(1);
        AtomicReference<JoinTableResponse> joinResponseRef = new AtomicReference<>();

        secondClient.addListener(new Listener() {
            @Override
            public void received(Connection connection, Object object) {
                if (object instanceof JoinTableResponse) {
                    joinResponseRef.set((JoinTableResponse) object);
                    joinLatch.countDown();
                }
            }
        });

        assertTrue(connectToServer(secondClient), "Failed to connect second client to server");

        // Join the created table
        JoinTableRequest joinRequest = new JoinTableRequest();
        joinRequest.username = "TableJoiner";
        joinRequest.chips = 5000;
        joinRequest.code = tableCode;
        secondClient.sendTCP(joinRequest);

        // Wait for join response
        assertTrue(joinLatch.await(3, TimeUnit.SECONDS), "Join response timed out");
        JoinTableResponse joinResponse = joinResponseRef.get();

        // Assertions
        assertNotNull(joinResponse, "Join response should not be null");
        assertTrue(joinResponse.success, "Table join should succeed");
        assertEquals(tableCode, joinResponse.code, "Table code should match");

        // Clean up
        secondClient.close();
    }
}
