package io.github.broskipoker.server;

public class PokerServer {
    public static void main(String[] args) {
        System.out.println("=== Broski Poker Server ===");
        System.out.println("Server starting at: " + new java.util.Date());
        System.out.println("Version: 1.0.0");
        System.out.println("Port: 8080 (ready for networking)");
        System.out.println("Server is running...");

        // Simple heartbeat to keep server alive
        try {
            int heartbeat = 0;
            while (true) {
                Thread.sleep(30000); // 30 seconds
                heartbeat++;
                System.out.println("[" + new java.util.Date() + "] Heartbeat #" + heartbeat + " - Server running");

                // Every 10 heartbeats (5 minutes), show status
                if (heartbeat % 10 == 0) {
                    System.out.println("=== Server Status ===");
                    System.out.println("Uptime: " + (heartbeat * 30) + " seconds");
                    System.out.println("Ready for player connections (networking coming soon)");
                    System.out.println("==================");
                }
            }
        } catch (InterruptedException e) {
            System.out.println("Server shutdown signal received");
            System.out.println("Shutting down gracefully...");
        }

        System.out.println("Broski Poker Server stopped at: " + new java.util.Date());
    }
}
