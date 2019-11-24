package fr.reseaux.server;

import fr.reseaux.common.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.List;

public class Server {

    private static final Logger LOGGER = LogManager.getLogger(Server.class);

    private static MulticastThread multicastThread;

    private static int multicastPort = 6789;

    private static List<User> userList;

    public static void main(String[] args) {
        // launch javafx app
        LOGGER.info(Runtime.getRuntime().maxMemory());

        ServerSocket listenSocket;

        if (args.length != 1) {
            System.out.println("Usage: java EchoServer <EchoServer port>");
            System.exit(1);
        }

        try {
            // Create users
            UserFactory userFactory = new UserFactory();
            userList = userFactory.createUsersFromXML(new File("./src/main/resources/users.xml"));
            LOGGER.debug("LISTE DES UTILISATEURS : " + userList);


            listenSocket = new ServerSocket(Integer.parseInt(args[0])); //port
            multicastThread = new MulticastThread(multicastPort, (Inet4Address)Inet4Address.getByName("225.225.225.225"));
            multicastThread.start();
            System.out.println("Server ready...");
            int i = 0;
            while (true) {
                Socket clientSocket = listenSocket.accept();
                System.out.println("Connexion from:" + clientSocket.getInetAddress());
                ClientThread ct = new ClientThread(clientSocket);
                ct.start();
            }
        } catch (Exception e) {
            System.err.println("Error in Server:" + e);
        }
    }

    static void connectUser(User user) {
        if (userList.contains(user)) {
        }
    }

    static MulticastThread getMulticastThread() {
        return multicastThread;
    }
}


