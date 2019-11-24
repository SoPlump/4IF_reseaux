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
import java.util.Objects;
import java.util.Vector;
import java.util.stream.Collectors;

public class Server {

    private static final Logger LOGGER = LogManager.getLogger(Server.class);

    //private static MulticastThread multicastThread;

    private static int multicastPort = 6789;

    private static Vector<MulticastThread> multicastList = new Vector<>();

    private static List<User> userList;

    private static UserFactory userFactory;

    private static final String USERFILE = "./src/main/resources/users.xml";

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
            userFactory = new UserFactory();
            userList = userFactory.createUsersFromXML(new File(USERFILE));

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
        List<User> result = userList.stream()
                .filter(a -> Objects.equals(a, user))
                .collect(Collectors.toList());

        if (!result.isEmpty()) {
            User userToConnect = result.get(0);
            if (!userToConnect.isConnected()) {
                LOGGER.debug("Server : connection successful");
                userToConnect.setConnected(true);
                return new ServerResponse(true, "");
            }
        }
        LOGGER.debug("Server : connection not successful");
        return new ServerResponse(false, "");
    }

    static ServerResponse registerUser(User user) {
        List<User> result = userList.stream()
                .filter(a -> Objects.equals(a.getUsername(), user.getUsername()))
                .collect(Collectors.toList());

        if (result.isEmpty()) {
            LOGGER.debug("Server : register successful");

            userFactory.addUserToXML(user, new File(USERFILE));
            userList.add(user);
            user.setConnected(true);

            LOGGER.debug("Liste des utilisateurs server" + userList);
            return new ServerResponse(true, "");
        } else {
            LOGGER.debug("Server : register not successful");
            return new ServerResponse(false, "User already exists");
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

    public static boolean userExists(String username) {
        for (User user: userList) {
            if (user.getUsername().equals(username)) return true;
        }
        return false;
    }

    public static boolean disconnect(String username) {
        List<User> result = userList.stream()
                .filter(a -> Objects.equals(a.getUsername(), username))
                .collect(Collectors.toList());
        if (result.isEmpty()) return false;
        result.get(0).setConnected(false);
        return true;

    }
}


