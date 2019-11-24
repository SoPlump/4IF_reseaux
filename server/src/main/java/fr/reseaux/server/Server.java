package fr.reseaux.server;

import fr.reseaux.common.ServerResponse;
import fr.reseaux.common.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Vector;

public class Server {

    private static final Logger LOGGER = LogManager.getLogger(Server.class);

    //private static MulticastThread multicastThread;

    private static int multicastPort = 6789;

    private static Vector<MulticastThread> multicastList = new Vector<>();

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

            /*
            MulticastThread globalChat = new MulticastThread(multicastPort, (Inet4Address) Inet4Address.getByName("225.225.225.225"), "Global Chat");
            multicastList.add(globalChat);
            globalChat.start();

            MulticastThread secondChat = new MulticastThread(multicastPort, (Inet4Address) Inet4Address.getByName("225.225.225.226"), "Secondary Chat");
            multicastList.add(secondChat);
            secondChat.start();
            */

            loadGroups();
            for (MulticastThread thread : multicastList) {
                LOGGER.debug(thread.getGroupName());
                LOGGER.debug(thread.retrieveInfos());
            }

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

    static MulticastThread getMulticastThreadByName(String groupName) {
        for (MulticastThread thread : multicastList) {
            if (thread.getGroupName().equals(groupName)) {
                LOGGER.debug("Thread returned : " + thread.getGroupName());
                return thread;
            }
        }
        return null;
    }


    static ServerResponse connectUser(User user) {
        LOGGER.debug(userList);
        LOGGER.debug(user);
        if (userList.contains(user)) {
            LOGGER.debug("Server : connection successful");
            return new ServerResponse(true, "");
        }
        else {
            LOGGER.debug("Server : connection not successful");
            return new ServerResponse(false, "");
        }
    }

    private static void loadGroups() {
        try {
            MulticastThreadFactory groupFactory = new MulticastThreadFactory();
            multicastList = groupFactory.createGroupsFromXML(new File("./src/main/resources/groups.xml"), multicastPort);
            for(MulticastThread group : multicastList) {
                group.start();
            }
        } catch (IOException | SAXException | ParserConfigurationException e) {
            e.printStackTrace();
        }

    }

    public static boolean addGroup(String groupName, String username) {
        for (MulticastThread group : multicastList) {
            if (group.getGroupName().equals(groupName)) return false;
        }
        MulticastThreadFactory multicastThreadFactory = new MulticastThreadFactory();
        String ip = grantIp();
        if (multicastThreadFactory.addGroup(new File("./src/main/resources/groups.xml"), groupName, ip, multicastPort, multicastList)) {
            return getMulticastThreadByName(groupName).addUser(username);
        }
        return false;
    }

    private static String grantIp() {
        int lastNumber = (multicastList.size() + 1) % 255;
        int thirdNumber = 225 + (multicastList.size() + 1) / 255;
        String newIp = "225.225."+thirdNumber+"."+lastNumber;
        return newIp;
    }
}


