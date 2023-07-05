package com.serversocket;

import java.net.InetAddress;
import java.net.ServerSocket;

public class Main {
    /**
     * Main method to run the server socket.
     */
    public static void main(String[] args) {
        try {
            // Create a configuration service instance
            ConfigService configService = new ConfigService();

            // Get the IP address from the configuration service
            InetAddress address = InetAddress.getByName(configService.getIP());

            // Create a server socket using the IP address and port from the configuration service
            int port = configService.getPort();
            try (ServerSocket serverSocket = new ServerSocket(port, 50, address)) {
                // Print the server's address and port
                System.out.println("Server started: http://" + configService.getIP() + ":" + port);

                // Continuously listen for incoming client connections
                while (true) {
                    // Accept a new client connection
                    ClientServer client = new ClientServer(serverSocket.accept(), configService);

                    // Create a new thread to handle the client's requests
                    Thread thread = new Thread(client);
                    thread.start();
                }
            }

        } catch (Exception e) {
            // Handle configuration errors
            System.err.println("Configuration error: " + e.getMessage());
        }
    }
}
