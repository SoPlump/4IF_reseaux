package fr.reseaux.client;

import fr.reseaux.common.Message;
import fr.reseaux.common.ServerRequest;
import fr.reseaux.common.ServerResponse;
import fr.reseaux.common.User;
import javafx.application.Platform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.graalvm.compiler.lir.gen.LIRGenerator_OptionDescriptors;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Client extends Thread {

    private static final Logger LOGGER = LogManager.getLogger(Client.class);

    private String username;

    private Message lastMsg;

    private boolean isConnected;

    private String currentGroup;

    private ObjectOutputStream outputStream;

    private ObjectInputStream inputStream;

    private Inet4Address multicastAddress;

    private int multicastPort;

    private Vector<Message> messageList = new Vector<>();

    private MulticastSocket multicastSocket;

    private Inet4Address currentAddress = null;

    private boolean noGroupJoined = true;

    Runnable updater;

    private Socket echoSocket = null;

    private BufferedReader socIn = null;


    /**
     * main method accepts a connection, receives a message from client then
     * sends an echo to the client
     */
    public Client(String ipAddress, String port) throws IOException {
        LOGGER.info("Creating Client ...");

        try {
            this.isConnected = false;
            this.echoSocket = new Socket(ipAddress, Integer.parseInt(port));
            this.socIn = new BufferedReader(
                    new InputStreamReader(echoSocket.getInputStream()));

            if (echoSocket.getInputStream() != null) {
                this.outputStream = new ObjectOutputStream(echoSocket.getOutputStream());
                this.inputStream = new ObjectInputStream(echoSocket.getInputStream());
            }

        } catch (UnknownHostException e) {
            System.err.println("Don't know about host:" + ipAddress);
            throw new UnknownHostException();
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for "
                    + "the connection to:" + ipAddress);
            throw new IOException();
        }
    }

    public void run() {
        // used to update the UI
        updater = new Runnable() {
            @Override
            public void run() {
                Controller.printMessage();
            }
        };

        joinGroup("Global Chat");
        currentGroup = "Global Chat";
        Controller.printStatus("Connected as " + username);
        readGroups();

        String line;
        byte[] buffer = new byte[1000];
        DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
        try {
            String content;
            while (true) {
                multicastSocket.receive(datagramPacket);
                content = new String(
                        datagramPacket.getData(),
                        datagramPacket.getOffset(),
                        datagramPacket.getLength(),
                        StandardCharsets.UTF_8
                );
                lastMsg = new Message(content);
                messageList.add(lastMsg);

                // update the UI
                Platform.runLater(updater);
            }

        } catch (Exception e) {
            LOGGER.error(e);
        }

        try {
            LOGGER.info("CLOSING ALL");
            socIn.close();
            echoSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Get what the user writes
    public void doWrite(Message msg) throws IOException {
        msg.setContent(msg.getContent().replace("\n", "").replace("\r", ""));
        LOGGER.info("send message in Client " + msg.toString());
        try {

            if (msg.getContent().startsWith("/")) {

                if (msg.getContent().equals("/quit")) {
                    close();
                } else if (msg.getContent().equals("/clear")) {
                    Controller.clearArea();
                } else if (msg.getContent().startsWith("/add")) {
                    addUserToGroup(msg);
                } else if (msg.getContent().startsWith("/join")) {
                    joinGroup(msg);
                } else if (msg.getContent().startsWith("/create")) {
                    createGroup(msg);
                } else if (msg.getContent().startsWith("/leave")) {
                    disconnect();
                }
            } else {
                this.outputStream.writeObject(new ServerRequest("message", "-content:{" + msg.getContent() + "}-username:{" + msg.getUsername() + "}"));
            }
        } catch (Exception e) {
            LOGGER.error(e);
        }
    }

    public Message getMessage() {
        return this.lastMsg;
    }

    public void clearMessageList() {
        messageList.clear();
    }

    public Vector<Message> getMessageList() {
        return this.messageList;
    }

    public void joinGroup(String groupName) {
        try {
            Controller.printStatus("Connection requested to " + groupName);
            ServerRequest connectRequest = new ServerRequest("connectToGroup", "-username:{" + username + "}-groupName:{" + groupName + "}");
            outputStream.writeObject(connectRequest);
            ServerResponse response = (ServerResponse) this.inputStream.readObject();

            if (response.isSuccess()) {
                String address = response.getRequestAttribute("groupAddress").replace("/", "");
                LOGGER.info("Adresse : " + address);
                String port = response.getRequestAttribute("groupPort");
                LOGGER.info("Port : " + port);

                multicastPort = Integer.parseInt(port);
                multicastAddress = (Inet4Address) Inet4Address.getByName(address);

                if (currentAddress != null) {
                    multicastSocket.leaveGroup(currentAddress);
                } else {
                    multicastSocket = new MulticastSocket(multicastPort);
                }
                currentAddress = multicastAddress;
                multicastSocket.joinGroup(multicastAddress);
                Message leaveMessage = new Message(username + " vient de se connecter.", "server");
                doWrite(leaveMessage);
                currentGroup = groupName;
                Controller.changeGroupName(groupName);
                messageList.add(new Message("/clear", "server"));
                LOGGER.info("Sending a request to get the story");
                readUsers(groupName);

                Platform.runLater(updater);

                // Print the story of the group

                ServerRequest storyRequest = new ServerRequest("getStory", "");
                this.outputStream.writeObject(storyRequest);

                Vector<Message> storyList = (Vector<Message>) this.inputStream.readObject();
                if (storyList.size() != 0) {
                    for (Message message : storyList) {
                        this.lastMsg = new Message(message.getContent(), message.getUsername());
                        messageList.add(lastMsg);
                    }
                    Platform.runLater(updater);
                }
                if (noGroupJoined) noGroupJoined = false;
                Controller.printStatus("Joined group " + groupName);
            } else {
                Controller.printError(response.getContent());
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public boolean connectUser(User user) {
        try {
            LOGGER.info("Trying to connect user");
            ServerRequest request = new ServerRequest("login", "-username:{" + user.getUsername() + "}-password:{" + user.getPassword() + "}");
            outputStream.writeObject(request);
            ServerResponse response = (ServerResponse) this.inputStream.readObject();

            if (response.isSuccess()) {
                LOGGER.debug("Connection acquired");
                this.isConnected = true;
                LOGGER.info(isConnected);
                this.username = user.getUsername();
                joinGroup("Global Chat");
                return true;
            }
        } catch (Exception e) {
            LOGGER.error(e);
        }
        return false;
    }

    public boolean registerUser(User user) {
        try {
            LOGGER.info("Trying to register user");
            ServerRequest request = new ServerRequest("register", "-username:{" + user.getUsername() + "}-password:{" + user.getPassword() + "}");
            outputStream.writeObject(request);
            ServerResponse response = (ServerResponse) this.inputStream.readObject();

            if (response.isSuccess()) {
                this.username = user.getUsername();
                joinGroup("Global Chat");
                return true;
            }
        } catch (Exception e) {
            LOGGER.error(e);
        }
        return false;
    }

    public String getUsername() {
        return username;
    }

    public void addUserToGroup(Message addMessage) {
        try {
            Pattern patternAdd = Pattern.compile("/add ([a-zA-Z0-9]+)");
            Matcher matcherAdd = patternAdd.matcher(addMessage.getContent());
            if (matcherAdd.matches()) {
                String userToAdd = matcherAdd.group(1);
                ServerRequest addRequest = new ServerRequest("addUser", "-user:{" + username + "}-username:{"
                        + userToAdd + "}");
                this.outputStream.writeObject(addRequest);
                ServerResponse response = (ServerResponse) this.inputStream.readObject();
                if (response.isSuccess()) {
                    doWrite(new Message("addUser:" + userToAdd, "server"));
                    Controller.printStatus(response.getContent());
                } else {
                    Controller.printError(response.getContent());
                }
            } else {
                Controller.printError("Enter a username to add to the group.");
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void joinGroup(Message msg) {
        Pattern patternGroup = Pattern.compile("/join (.+)");
        Matcher matcherGroup = patternGroup.matcher(msg.getContent());
        if (matcherGroup.matches()) {
            String group = matcherGroup.group(1);
            joinGroup(group);
        } else {
            Controller.printError("Enter a group name to join.");
        }
    }

    public void createGroup(Message msg) {
        try {
            Pattern patternGroup = Pattern.compile("/create (.+)");
            Matcher matcherGroup = patternGroup.matcher(msg.getContent());
            if (matcherGroup.matches()) {
                String group = matcherGroup.group(1);
                ServerRequest requestCreate = new ServerRequest("createGroup", "-groupName:{" + group + "}-username:{" + username + "}");
                outputStream.writeObject(requestCreate);
                ServerResponse response = (ServerResponse) inputStream.readObject();
                if (response.isSuccess()) {
                    Controller.addGroup(group);
                    Controller.printStatus(response.getContent());
                } else {
                    Controller.printError(response.getContent());
                }
            } else {
                Controller.printError("Enter a group name to create");
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public String getCurrentGroup() {
        return currentGroup;
    }

    public void setCurrentGroup(String currentGroup) {
        this.currentGroup = currentGroup;
    }

    // get all the users of the app
    public void readUsers(String groupName) {
        try {
            ServerRequest userlistRequest = new ServerRequest("userList", "-groupName:{" + groupName + "}");
            outputStream.writeObject(userlistRequest);
            Set<String> whitelist = (Set<String>) inputStream.readObject();
            LOGGER.debug(whitelist);
            Controller.clearUsersArea();
            for (String username : whitelist) {
                Controller.addUsers(username);
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // get all the groups of the app
    public void readGroups() {
        ServerRequest getGroupList = new ServerRequest("groupList", "");
        try {
            outputStream.writeObject(getGroupList);
            List<String> groupList = (List<String>) inputStream.readObject();
            for (String groupName : groupList) {
                Controller.addGroup(groupName);
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // disconnect and close the client socket
    public void close() {
        try {
            this.outputStream.writeObject(new ServerRequest("disconnect", "-username:{" + username + "}"));
            ServerResponse response = (ServerResponse) inputStream.readObject();
            if (response.isSuccess()) {
                Controller.closeApp();
            }
        } catch (IOException | ClassNotFoundException e) {
            Controller.printError("Couldn't disconnect from server.");
            e.printStackTrace();
        }
    }

    // disconnect the client
    public void disconnect() {
        try {
            this.outputStream.writeObject(new ServerRequest("disconnect", "-username:{" + username + "}"));
            ServerResponse response = (ServerResponse) inputStream.readObject();
            if (response.isSuccess()) {
                Controller.disconnect();
            }
        } catch (IOException | ClassNotFoundException e) {
            Controller.printError("Couldn't disconnect from server.");
            e.printStackTrace();
        }
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }
}

