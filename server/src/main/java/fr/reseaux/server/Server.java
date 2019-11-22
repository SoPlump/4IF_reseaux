package fr.reseaux.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private static final Logger LOGGER = LogManager.getLogger(Server.class);

    private static ServerListener sl;

    public static void main(String[] args) {
        // launch javafx app
        LOGGER.info(Runtime.getRuntime().maxMemory());

        ServerSocket listenSocket;

        if (args.length != 1) {
            System.out.println("Usage: java EchoServer <EchoServer port>");
            System.exit(1);
        }

        try {
            listenSocket = new ServerSocket(Integer.parseInt(args[0])); //port
            sl = new ServerListener();
            sl.start();
            System.out.println("Server ready...");
            int i = 0;
            while (true) {
                Socket clientSocket = listenSocket.accept();
                System.out.println("Connexion from:" + clientSocket.getInetAddress());
                ClientThread ct = new ClientThread(clientSocket);
                ct.start();
                sl.addClient(clientSocket);
            }
        } catch (Exception e) {
            System.err.println("Error in Server:" + e);
        }
    }

    static ServerListener getListener() {
        return sl;
    }
}
