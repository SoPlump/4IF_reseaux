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
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Client extends Thread {

    private static final Logger LOGGER = LogManager.getLogger(Client.class);

    private String username;

    private Message lastMsg;

    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;

    private Inet4Address multicastAddress;

    private int multicastPort;

    private Vector<Message> messageList = new Vector<>();

    private MulticastSocket multicastSocket;

    private Inet4Address currentAddress = null;

    private boolean noGroupJoined = true;

    Runnable updater;

    Socket echoSocket = null;
    //PrintStream socOut = null;
    BufferedReader socIn = null;


    /**
     * main method accepts a connection, receives a message from client then
     * sends an echo to the client
     */
    public Client(String[] args) throws IOException {
        LOGGER.info("Creating Client ...");

        if (args.length != 2) {
            System.out.println("Usage: java Client <EchoServer host> <EchoServer port>");
            System.exit(1);
        }

        try {
            // creation socket ==> connexion
            this.echoSocket = new Socket(args[0], Integer.parseInt(args[1]));
            this.socIn = new BufferedReader(
                    new InputStreamReader(echoSocket.getInputStream()));

            if (echoSocket.getInputStream() != null) {
                this.outputStream = new ObjectOutputStream(echoSocket.getOutputStream());
                this.inputStream = new ObjectInputStream(echoSocket.getInputStream());
            }

        } catch (UnknownHostException e) {
            System.err.println("Don't know about host:" + args[0]);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for "
                    + "the connection to:" + args[0]);
            System.exit(1);
        }
    }

    public void run() {
        updater = new Runnable() {
            @Override
            public void run() {
                Controller.printMessage();
            }
        };
/*
        Runnable clearer = new Runnable() {
            @Override
            public void run() {
                LOGGER.debug("Cleared");
                Controller.clearArea();
            }
        };

 */

        joinGroup("Global Chat");
        //joinGroup("Secondary Chat", updater);
        //joinGroup("Third Chat", updater);
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
                        StandardCharsets.UTF_8 // or some other charset
                );
                lastMsg = new Message(content); //todo : recuperer usename
                messageList.add(lastMsg);

                // update the UI
                Platform.runLater(updater);
            }
        } catch (Exception e) {
            LOGGER.error(e);
        }

        try {
            LOGGER.info("CLOSING ALL");
            //socOut.close();
            socIn.close();
            echoSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void doWrite(Message msg) throws IOException {
        msg.setContent(msg.getContent().replace("\n", "").replace("\r", ""));
        LOGGER.info("send message in Client " + msg.toString());
        try {

            if (msg.getContent().startsWith("/")) {

                if (msg.getContent().equals("/quit")) {
                    Controller.closeApp(); //todo : close socket
                } else if (msg.getContent().equals("/clear")) {
                    Controller.clearArea();
                } else if (msg.getContent().startsWith("/add")) {
                    addUserToGroup(msg);
                } else if (msg.getContent().startsWith("/join")) {
                    joinGroup(msg);
                } else if (msg.getContent().startsWith("/create")) {
                    createGroup(msg);
                } else if (msg.getContent().equals("/info")) {

                }
            } else {
                this.outputStream.writeObject(new ServerRequest("message", "-content:{" + msg.getContent() + "}-username:{" + username + "}"));
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
            LOGGER.debug("Connection requested to " + groupName);
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

                messageList.add(new Message("/clear", "server"));
                LOGGER.info("Sending a request to get the story");
                lastMsg = new Message("Connected as " + username, "server");
                messageList.add(lastMsg);

                Platform.runLater(updater);
                ServerRequest storyRequest = new ServerRequest("getStory", "");
                this.outputStream.writeObject(storyRequest);

                //while(true) {
                Vector<Message> storyList = (Vector<Message>) this.inputStream.readObject();
                if (storyList.size() != 0) {
                    for (Message message : storyList) {
                        this.lastMsg = new Message(message.getContent(), message.getUsername());
                        messageList.add(lastMsg);
                    }
                    Platform.runLater(updater);
                    //  break;
                }
                if (noGroupJoined) noGroupJoined = false;
                // }
            } else {
                LOGGER.debug(response.getContent());
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public boolean connectUser(User user) {
        try {
            LOGGER.info("Trying to connect user");
            ServerRequest request = new ServerRequest("login", "-username:{"+user.getUsername()+"}-password:{"+user.getPassword()+"}");
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

    public boolean registerUser(User user) {
        try {
            LOGGER.info("Trying to register user");
            ServerRequest request = new ServerRequest("register", "-username:{"+user.getUsername()+"}-password:{"+user.getPassword()+"}");
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
                ServerResponse response = (ServerResponse)this.inputStream.readObject();
                LOGGER.debug(response.getContent());
            } else {
                LOGGER.debug("Pas de nom entré pour l'ajout");
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
            LOGGER.debug("Pas de nom entré pour le changement de groupe");
        }
    }

    public void createGroup(Message msg) {
        try {
            Pattern patternGroup = Pattern.compile("/create (.+)");
            Matcher matcherGroup = patternGroup.matcher(msg.getContent());
            if (matcherGroup.matches()) {
                String group = matcherGroup.group(1);
                ServerRequest requestCreate = new ServerRequest("createGroup", "-groupName:{" + group + "}-username:{"+username+"}");
                outputStream.writeObject(requestCreate);
                ServerResponse response = (ServerResponse) inputStream.readObject();
                LOGGER.debug(response.getContent());
            } else {
                LOGGER.debug("Pas de nom entré pour la création de groupe");
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }


    }
}

