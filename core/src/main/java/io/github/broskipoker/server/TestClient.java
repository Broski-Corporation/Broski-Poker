package io.github.broskipoker.server;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Connection;

public class TestClient {
    public static void main(String[] args) throws Exception {
        Client client = new Client();
        client.start();
        client.connect(5000, "104.248.45.171", 8080);
        System.out.println("Connected!");

        client.addListener(new Listener() {
            public void connected(Connection connection) {
                System.out.println("Connected to server!");
            }
            public void disconnected(Connection connection) {
                System.out.println("Disconnected from server!");
            }
            public void received(Connection connection, Object object) {
                System.out.println("Received: " + object);
            }
        });

        // Keep alive for test
        Thread.sleep(10000);
        client.stop();
    }
}
