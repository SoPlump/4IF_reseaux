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
                    addMessageToStory(msg);
                    DatagramPacket datagramPacket = new DatagramPacket(msg.toString().getBytes(), msg.toString().getBytes().length, multicastAddress, multicastPort);
                    multicastSocket.send(datagramPacket);
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
        try {
            if (message.getContent().startsWith("/")) return;
            System.out.println(message);
            //File file = new File("files/story.txt");
            //System.out.println(file.exists());
            //System.out.println(file.canWrite());
            //PrintStream printStream = new PrintStream(file);
            BufferedWriter writer = new BufferedWriter(new FileWriter("files/" + groupName + "/story.txt", true));
            writer.append(message.toString() + '\n');
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Message> loadStory() {
        try {
            Vector<Message> messageList = new Vector<>();
            BufferedReader bufferedReader = new BufferedReader(new FileReader("files/" + groupName + "/story.txt"));
            String line;
            Pattern messagePattern = Pattern.compile("([0-9a-zA-Z]+?) : (.*)");
            Matcher messageMatcher;
            while ((line = bufferedReader.readLine()) != null) {
                System.out.println(line);
                messageMatcher = messagePattern.matcher(line);
                if (messageMatcher.matches()) {
                    System.out.println(line);
                    Message message = new Message(messageMatcher.group(2), messageMatcher.group(1));
                    messageList.add(message);
                }
            }
            System.out.println(messageList);
            return messageList;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String retrieveInfos() {
        return "-groupPort:{" + multicastPort + "}-groupAddress:{" + multicastAddress + "}";
    }

    public boolean accept(String usernameRequest) {
        LOGGER.info(usernameRequest);
        for (String username : whitelist) {
            if (username.equals(usernameRequest)) {
                return true;
            }
        }
        return false;
    }

    public boolean addUser(String username) {
        try {
            LOGGER.info("Name : " + username);
            LOGGER.info("Set : " + whitelist);
            if (this.whitelist.add(username)) {
                BufferedWriter writer = new BufferedWriter(new FileWriter("files/" + groupName + "/whitelist.txt", true));
                writer.append(username + '\n');
                writer.close();
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }


    }

    public void loadUsers() {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader("files/" + groupName + "/whitelist.txt"));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                whitelist.add(line);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
