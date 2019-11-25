/**
 * ClientThread
 * Example of a TCP server
 * Date: 14/12/08
 * Authors:
 */
package fr.reseaux.server;

import fr.reseaux.common.Message;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MulticastThread
        extends Thread {

    private static final Logger LOGGER = LogManager.getLogger(MulticastThread.class);

    private MulticastSocket multicastSocket;

    private int multicastPort;

    private Inet4Address multicastAddress;

    private Queue<Message> messageQueue = new ConcurrentLinkedQueue<>();

    Set<String> whitelist = new ConcurrentSkipListSet<>();

    public String getGroupName() {
        return groupName;
    }

    private String groupName;

    public MulticastThread(int multicastPort, Inet4Address multicastAddress, String groupName) {
        try {
            this.multicastSocket = new MulticastSocket(multicastPort);
            this.multicastPort = multicastPort;
            this.multicastAddress = multicastAddress;
            this.groupName = groupName;
            this.multicastSocket.joinGroup(multicastAddress);

            String directoryName = "files/" + groupName;
            File directory = new File(directoryName);
            if (!directory.exists()) {
                directory.mkdir();
            }
            File storyFile = new File(directoryName + "/story.txt");
            File whitelistFile = new File(directoryName + "/whitelist.txt");
            if (!storyFile.exists()) {
                FileWriter fw = new FileWriter(storyFile.getAbsoluteFile());
            }
            if (!whitelistFile.exists()) {
                FileWriter fw = new FileWriter(whitelistFile.getAbsoluteFile());
            }

            this.loadUsers();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            Message msg;
            while (true) {
                if (!messageQueue.isEmpty()) {
                    msg = messageQueue.poll();
                    if(!msg.getContent().contains("null vient de se connecter")) {
                        addMessageToStory(msg);
                        DatagramPacket datagramPacket = new DatagramPacket(msg.toString().getBytes("UTF-8"), msg.toString().getBytes().length, multicastAddress, multicastPort);
                        multicastSocket.send(datagramPacket);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error in ClientThread:" + e);
        }
    }

    public void addMessage(Message msg) {
        this.messageQueue.add(msg);
    }

    public void addMessageToStory(Message message) {
        if ("server".equals(message.getUsername())) {
            return;
        }
        HistoryFactory historyFactory = new HistoryFactory();
        historyFactory.addMessageToStory(message, new File("files/" + groupName + "/story.txt"));
    }

    public List<Message> loadStory() {
        HistoryFactory historyFactory = new HistoryFactory();
        Vector<Message> messageList = historyFactory.loadStory(new File("files/" + groupName + "/story.txt"));
        return messageList;
    }

    public String retrieveInfos() {
        return "-groupPort:{" + multicastPort + "}-groupAddress:{" + multicastAddress + "}";
    }

    public boolean accept(String usernameRequest) {
        LOGGER.info(usernameRequest);
        for (String username : whitelist) {
            if (username.replace("\r", "").replace("\r", "").equals(usernameRequest)) {
                return true;
            }
        }
        return false;
    }

    public boolean addUser(String username) {
        WhitelistFactory whitelistFactory = new WhitelistFactory();
        return whitelistFactory.addUser(whitelist, new File("files/" + groupName + "/whitelist.txt"), username);
    }

    public void loadUsers() {
        WhitelistFactory whitelistFactory = new WhitelistFactory();
        whitelist = whitelistFactory.loadUsers(new File("files/" + groupName + "/whitelist.txt"));
    }
}
